package com.barclays.settlement.event;

import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MismatchEvent {
  String tradeId;
  String accountId;
  String currency;
  BigDecimal tradeAmount;
  BigDecimal ledgerAmount;
  MismatchType mismatchType;
  ReconciliationStatus status;
  String mismatchReason;
  Instant eventTime;
}
