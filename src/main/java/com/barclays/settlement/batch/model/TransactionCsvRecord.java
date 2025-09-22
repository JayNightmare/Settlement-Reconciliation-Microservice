package com.barclays.settlement.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionCsvRecord {
  String tradeId;
  String accountId;
  String currency;
  BigDecimal amount;
  LocalDate tradeDate;
  String sourceFile;
}
