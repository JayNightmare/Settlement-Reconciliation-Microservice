package com.barclays.settlement.batch.processor;

import com.barclays.settlement.batch.model.TransactionCsvRecord;
import com.barclays.settlement.domain.entity.TradeTransaction;
import java.time.Instant;
import org.springframework.batch.item.ItemProcessor;

public class TradeTransactionItemProcessor
    implements ItemProcessor<TransactionCsvRecord, TradeTransaction> {
  @Override
  public TradeTransaction process(TransactionCsvRecord item) {
    TradeTransaction trade = new TradeTransaction();
    trade.setTradeId(item.getTradeId());
    trade.setAccountId(item.getAccountId());
    trade.setCurrency(item.getCurrency());
    trade.setAmount(item.getAmount());
    trade.setTradeDate(item.getTradeDate());
    trade.setSourceFile(item.getSourceFile());
    trade.setIngestedAt(Instant.now());
    return trade;
  }
}
