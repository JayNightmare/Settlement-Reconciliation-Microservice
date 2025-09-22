package com.barclays.settlement.web.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReconciliationSummaryResponse {
  long totalProcessed;
  long mismatches;
  Map<String, Long> mismatchesByType;
  Map<String, Long> statusCounts;
}
