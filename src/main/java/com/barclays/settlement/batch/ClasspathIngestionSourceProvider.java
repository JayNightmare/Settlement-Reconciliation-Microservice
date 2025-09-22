package com.barclays.settlement.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ClasspathIngestionSourceProvider implements IngestionSourceProvider {

  private final Resource tradeResource;
  private final Resource ledgerResource;

  public ClasspathIngestionSourceProvider(
      ResourceLoader resourceLoader,
      @Value("${ingestion.trade-resource:classpath:samples/trades.csv}") String tradePath,
      @Value("${ingestion.ledger-resource:classpath:samples/ledger.csv}") String ledgerPath) {
    this.tradeResource = resourceLoader.getResource(tradePath);
    this.ledgerResource = resourceLoader.getResource(ledgerPath);
  }

  @Override
  public Resource latestTradeResource() {
    return tradeResource;
  }

  @Override
  public Resource latestLedgerResource() {
    return ledgerResource;
  }
}
