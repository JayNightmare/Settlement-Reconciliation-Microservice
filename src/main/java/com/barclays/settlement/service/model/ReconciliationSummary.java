package com.barclays.settlement.service.model;

import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReconciliationSummary {
  long totalProcessed;
  long mismatches;
  Map<MismatchType, Long> mismatchesByType;
  Map<ReconciliationStatus, Long> statusCounts;
}
