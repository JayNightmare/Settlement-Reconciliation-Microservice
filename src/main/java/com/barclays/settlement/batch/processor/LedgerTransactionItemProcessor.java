package com.barclays.settlement.batch.processor;

import com.barclays.settlement.batch.model.TransactionCsvRecord;
import com.barclays.settlement.domain.entity.LedgerTransaction;
import java.time.Instant;
import org.springframework.batch.item.ItemProcessor;

public class LedgerTransactionItemProcessor
    implements ItemProcessor<TransactionCsvRecord, LedgerTransaction> {
  @Override
  public LedgerTransaction process(TransactionCsvRecord item) {
    LedgerTransaction ledger = new LedgerTransaction();
    ledger.setTradeId(item.getTradeId());
    ledger.setAccountId(item.getAccountId());
    ledger.setCurrency(item.getCurrency());
    ledger.setAmount(item.getAmount());
    ledger.setTradeDate(item.getTradeDate());
    ledger.setSourceFile(item.getSourceFile());
    ledger.setIngestedAt(Instant.now());
    return ledger;
  }
}
