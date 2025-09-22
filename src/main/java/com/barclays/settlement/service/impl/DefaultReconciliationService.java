package com.barclays.settlement.service.impl;

import com.barclays.settlement.config.ReconciliationProperties;
import com.barclays.settlement.domain.entity.LedgerTransaction;
import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.domain.entity.TradeTransaction;
import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import com.barclays.settlement.domain.repository.LedgerTransactionRepository;
import com.barclays.settlement.domain.repository.ReconciliationResultRepository;
import com.barclays.settlement.domain.repository.TradeTransactionRepository;
import com.barclays.settlement.messaging.MismatchEventPublisher;
import com.barclays.settlement.service.ReconciliationService;
import com.barclays.settlement.service.model.ReconciliationSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultReconciliationService implements ReconciliationService {

  private final TradeTransactionRepository tradeTransactionRepository;
  private final LedgerTransactionRepository ledgerTransactionRepository;
  private final ReconciliationResultRepository reconciliationResultRepository;
  private final ReconciliationProperties reconciliationProperties;
  private final MismatchEventPublisher mismatchEventPublisher;
  private final MeterRegistry meterRegistry;

  @Override
  @Transactional
  public ReconciliationSummary runReconciliation(
      LocalDate startDate, LocalDate endDate, String portfolioId) {
    Timer.Sample timer = Timer.start(meterRegistry);
    log.info(
        "Running reconciliation between {} and {} for portfolio {}",
        startDate,
        endDate,
        portfolioId);

    var trades = tradeTransactionRepository.findByTradeDateBetween(startDate, endDate);
    var ledgers = ledgerTransactionRepository.findByTradeDateBetween(startDate, endDate);

    Map<String, LedgerTransaction> ledgerByTradeId =
        ledgers.stream()
            .collect(
                Collectors.toMap(LedgerTransaction::getTradeId, Function.identity(), (a, b) -> a));

    Map<MismatchType, Long> mismatchesByType = new EnumMap<>(MismatchType.class);
    Map<ReconciliationStatus, Long> statusCounts = new EnumMap<>(ReconciliationStatus.class);
    long mismatchCounter = 0;

    for (TradeTransaction trade : trades) {
      LedgerTransaction ledger = ledgerByTradeId.remove(trade.getTradeId());
      if (ledger == null) {
        mismatchCounter +=
            recordMismatch(
                trade,
                null,
                MismatchType.MISSING_LEDGER,
                "No ledger transaction found",
                mismatchesByType,
                statusCounts);
        continue;
      }

      if (!trade.getCurrency().equalsIgnoreCase(ledger.getCurrency())) {
        mismatchCounter +=
            recordMismatch(
                trade,
                ledger,
                MismatchType.CURRENCY_MISMATCH,
                "Currency mismatch",
                mismatchesByType,
                statusCounts);
        continue;
      }

      BigDecimal tolerance =
          reconciliationProperties
              .getTolerances()
              .getOrDefault(trade.getCurrency(), BigDecimal.ZERO);
      BigDecimal difference = trade.getAmount().subtract(ledger.getAmount()).abs();
      if (difference.compareTo(tolerance) > 0) {
        mismatchCounter +=
            recordMismatch(
                trade,
                ledger,
                MismatchType.AMOUNT_DELTA,
                String.format("Difference %s exceeds tolerance %s", difference, tolerance),
                mismatchesByType,
                statusCounts);
      }
    }

    // Remaining ledger transactions without matching trades.
    for (LedgerTransaction orphanLedger : ledgerByTradeId.values()) {
      mismatchCounter +=
          recordMismatch(
              null,
              orphanLedger,
              MismatchType.MISSING_TRADE,
              "No trade transaction found",
              mismatchesByType,
              statusCounts);
    }

    long totalProcessed = trades.size() + ledgers.size();
    timer.stop(meterRegistry.timer("reconciliation.run.duration"));
    meterRegistry.counter("reconciliation.run.count").increment();
    return ReconciliationSummary.builder()
        .totalProcessed(totalProcessed)
        .mismatches(mismatchCounter)
        .mismatchesByType(mismatchesByType)
        .statusCounts(statusCounts)
        .build();
  }

  @Override
  @Transactional
  public void acknowledgeMismatch(Long id, String acknowledgedBy, String notes) {
    ReconciliationResult result =
        reconciliationResultRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mismatch result not found: " + id));
    result.setStatus(ReconciliationStatus.RESOLVED);
    result.setAcknowledgedAt(Instant.now());
    result.setAcknowledgedBy(acknowledgedBy);
    result.setUpdatedAt(Instant.now());
    result.setMismatchReason(mergeResolutionNotes(result.getMismatchReason(), notes));
    reconciliationResultRepository.save(result);
    meterRegistry.counter("reconciliation.mismatch.resolved").increment();
    log.info("Mismatch {} acknowledged by {}", id, acknowledgedBy);
  }

  private long recordMismatch(
      TradeTransaction trade,
      LedgerTransaction ledger,
      MismatchType type,
      String reason,
      Map<MismatchType, Long> mismatchCount,
      Map<ReconciliationStatus, Long> statusCount) {
    ReconciliationResult result = new ReconciliationResult();
    Optional.ofNullable(trade)
        .ifPresent(
            t -> {
              result.setTradeId(t.getTradeId());
              result.setAccountId(t.getAccountId());
              result.setCurrency(t.getCurrency());
              result.setTradeAmount(t.getAmount());
            });
    Optional.ofNullable(ledger)
        .ifPresent(
            l -> {
              result.setTradeId(Optional.ofNullable(result.getTradeId()).orElse(l.getTradeId()));
              result.setAccountId(
                  Optional.ofNullable(result.getAccountId()).orElse(l.getAccountId()));
              result.setCurrency(Optional.ofNullable(result.getCurrency()).orElse(l.getCurrency()));
              result.setLedgerAmount(l.getAmount());
            });
    result.setMismatchType(type);
    result.setMismatchReason(reason);
    result.setStatus(ReconciliationStatus.UNRESOLVED);
    result.setLastReconciledAt(Instant.now());
    result.setUpdatedAt(Instant.now());

    reconciliationResultRepository.save(result);
    mismatchEventPublisher.publish(result);

    mismatchCount.merge(type, 1L, Long::sum);
    statusCount.merge(result.getStatus(), 1L, Long::sum);
    meterRegistry.counter("reconciliation.mismatch.count", "type", type.name()).increment();
    return 1L;
  }

  private String mergeResolutionNotes(String existing, String notes) {
    if (existing == null || existing.isBlank()) {
      return notes;
    }
    return existing + " | resolution: " + notes;
  }
}
