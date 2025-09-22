package com.barclays.settlement.service.impl;

import com.barclays.settlement.service.IngestionService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(JobRepository.class)
@RequiredArgsConstructor
@Slf4j
public class BatchIngestionService implements IngestionService {

  private final Job settlementIngestionJob;
  private final JobLauncher jobLauncher;

  @Override
  public void runIngestionJob() {
    try {
      JobParameters jobParameters =
          new JobParametersBuilder()
              .addLong("timestamp", Instant.now().toEpochMilli())
              .toJobParameters();
      jobLauncher.run(settlementIngestionJob, jobParameters);
      log.info("Settlement ingestion job triggered successfully");
    } catch (Exception ex) {
      log.error("Failed to launch settlement ingestion job", ex);
      throw new IllegalStateException("Failed to launch settlement ingestion job", ex);
    }
  }
}
