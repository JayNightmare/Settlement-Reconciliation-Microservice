package com.barclays.settlement.web.dto;

import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MismatchResponse {
  Long id;
  String tradeId;
  String accountId;
  String currency;
  BigDecimal tradeAmount;
  BigDecimal ledgerAmount;
  MismatchType mismatchType;
  ReconciliationStatus status;
  String mismatchReason;
  Instant lastReconciledAt;
  Instant acknowledgedAt;
  String acknowledgedBy;
}
