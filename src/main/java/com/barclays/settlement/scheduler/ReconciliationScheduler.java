package com.barclays.settlement.scheduler;

import com.barclays.settlement.config.ReconciliationProperties;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import com.barclays.settlement.domain.repository.ReconciliationResultRepository;
import com.barclays.settlement.service.IngestionService;
import com.barclays.settlement.service.NotificationService;
import com.barclays.settlement.service.ReconciliationService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(ReconciliationResultRepository.class)
@RequiredArgsConstructor
@Slf4j
public class ReconciliationScheduler {

  private final IngestionService ingestionService;
  private final ReconciliationService reconciliationService;
  private final NotificationService notificationService;
  private final ReconciliationResultRepository reconciliationResultRepository;
  private final ReconciliationProperties reconciliationProperties;

  @Scheduled(cron = "0 0 1 * * *")
  public void nightlyReconciliation() {
    log.info("Starting nightly ingestion and reconciliation");
    ingestionService.runIngestionJob();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    reconciliationService.runReconciliation(today.minusDays(1), today, null);
  }

  @Scheduled(cron = "0 0 6 * * *")
  public void escalateUnresolved() {
    Instant threshold = Instant.now().minus(reconciliationProperties.getEscalation().getSla());
    var stale =
        reconciliationResultRepository.findStaleWithStatus(
            ReconciliationStatus.UNRESOLVED, threshold);
    notificationService.notifyUnresolvedMismatches(stale);
  }
}
