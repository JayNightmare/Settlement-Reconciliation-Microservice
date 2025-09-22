package com.barclays.settlement.service.impl;

import com.barclays.settlement.service.IngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(name = "batchIngestionService")
@Slf4j
public class NoOpIngestionService implements IngestionService {

  @Override
  public void runIngestionJob() {
    log.info("No-op ingestion service - ingestion job disabled");
  }
}