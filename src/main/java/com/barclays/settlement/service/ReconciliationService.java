package com.barclays.settlement.service;

import com.barclays.settlement.service.model.ReconciliationSummary;
import java.time.LocalDate;

public interface ReconciliationService {
  ReconciliationSummary runReconciliation(
      LocalDate startDate, LocalDate endDate, String portfolioId);

  void acknowledgeMismatch(Long id, String acknowledgedBy, String notes);
}
