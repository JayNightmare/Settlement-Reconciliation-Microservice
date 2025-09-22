package com.barclays.settlement.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.barclays.settlement.config.TestSecurityConfig;
import com.barclays.settlement.domain.entity.LedgerTransaction;
import com.barclays.settlement.domain.entity.TradeTransaction;
import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import com.barclays.settlement.domain.repository.LedgerTransactionRepository;
import com.barclays.settlement.domain.repository.ReconciliationResultRepository;
import com.barclays.settlement.domain.repository.TradeTransactionRepository;
import com.barclays.settlement.messaging.MismatchEventPublisher;
import com.barclays.settlement.service.model.ReconciliationSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Import(TestSecurityConfig.class)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class ReconciliationServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    postgres.start();
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private ReconciliationService reconciliationService;

  @Autowired private TradeTransactionRepository tradeTransactionRepository;

  @Autowired private LedgerTransactionRepository ledgerTransactionRepository;

  @Autowired private ReconciliationResultRepository reconciliationResultRepository;

  @MockBean private MismatchEventPublisher mismatchEventPublisher;

  @BeforeEach
  void setUp() {
    reconciliationResultRepository.deleteAll();
    ledgerTransactionRepository.deleteAll();
    tradeTransactionRepository.deleteAll();
  }

  @Test
  void runReconciliation_detectsAmountDeltaAndMissingTrade() {
    TradeTransaction trade = new TradeTransaction();
    trade.setTradeId("TRD-1");
    trade.setAccountId("ACC-1");
    trade.setCurrency("USD");
    trade.setAmount(new BigDecimal("1000.00"));
    trade.setTradeDate(LocalDate.of(2024, 6, 1));
    tradeTransactionRepository.save(trade);

    LedgerTransaction ledger = new LedgerTransaction();
    ledger.setTradeId("TRD-1");
    ledger.setAccountId("ACC-1");
    ledger.setCurrency("USD");
    ledger.setAmount(new BigDecimal("1005.00"));
    ledger.setTradeDate(LocalDate.of(2024, 6, 1));
    ledgerTransactionRepository.save(ledger);

    LedgerTransaction orphanLedger = new LedgerTransaction();
    orphanLedger.setTradeId("TRD-2");
    orphanLedger.setAccountId("ACC-2");
    orphanLedger.setCurrency("USD");
    orphanLedger.setAmount(new BigDecimal("500.00"));
    orphanLedger.setTradeDate(LocalDate.of(2024, 6, 1));
    ledgerTransactionRepository.save(orphanLedger);

    ReconciliationSummary summary =
        reconciliationService.runReconciliation(
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 1), null);

    assertThat(summary.getMismatches()).isEqualTo(2);
    assertThat(summary.getMismatchesByType()).containsEntry(MismatchType.AMOUNT_DELTA, 1L);
    assertThat(summary.getMismatchesByType()).containsEntry(MismatchType.MISSING_TRADE, 1L);

    var persistedResults = reconciliationResultRepository.findAll();
    assertThat(persistedResults).hasSize(2);
    assertThat(persistedResults)
        .anyMatch(
            result ->
                result.getMismatchType() == MismatchType.AMOUNT_DELTA
                    && result.getStatus() == ReconciliationStatus.UNRESOLVED);
    Mockito.verify(mismatchEventPublisher, Mockito.times(2)).publish(Mockito.any());
  }
}
