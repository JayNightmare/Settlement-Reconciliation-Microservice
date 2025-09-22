package com.barclays.settlement.batch.mapper;

import com.barclays.settlement.batch.model.TransactionCsvRecord;
import java.time.LocalDate;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class TransactionFieldSetMapper implements FieldSetMapper<TransactionCsvRecord> {

  @Override
  public TransactionCsvRecord mapFieldSet(FieldSet fieldSet) throws BindException {
    return TransactionCsvRecord.builder()
        .tradeId(fieldSet.readString("trade_id"))
        .accountId(fieldSet.readString("account_id"))
        .currency(fieldSet.readString("currency"))
        .amount(fieldSet.readBigDecimal("amount"))
        .tradeDate(LocalDate.parse(fieldSet.readString("trade_date")))
        .sourceFile(fieldSet.readString("source_file"))
        .build();
  }
}
