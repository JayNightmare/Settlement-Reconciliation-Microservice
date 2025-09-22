package com.barclays.settlement.batch;

import org.springframework.core.io.Resource;

public interface IngestionSourceProvider {
  Resource latestTradeResource();

  Resource latestLedgerResource();
}
