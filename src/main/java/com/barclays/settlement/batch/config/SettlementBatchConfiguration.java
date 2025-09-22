package com.barclays.settlement.batch.config;

import com.barclays.settlement.batch.IngestionSourceProvider;
import com.barclays.settlement.batch.mapper.TransactionFieldSetMapper;
import com.barclays.settlement.batch.model.TransactionCsvRecord;
import com.barclays.settlement.batch.processor.LedgerTransactionItemProcessor;
import com.barclays.settlement.batch.processor.TradeTransactionItemProcessor;
import com.barclays.settlement.domain.entity.LedgerTransaction;
import com.barclays.settlement.domain.entity.TradeTransaction;
import com.barclays.settlement.domain.repository.LedgerTransactionRepository;
import com.barclays.settlement.domain.repository.TradeTransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnBean(JobRepository.class)
public class SettlementBatchConfiguration {

  @Bean
  public Job settlementIngestionJob(
      JobRepository jobRepository,
      @Qualifier("tradeIngestionStep") Step tradeIngestionStep,
      @Qualifier("ledgerIngestionStep") Step ledgerIngestionStep) {
    return new JobBuilder("settlementIngestionJob", jobRepository)
        .start(tradeIngestionStep)
        .next(ledgerIngestionStep)
        .build();
  }

  @Bean
  public Step tradeIngestionStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      @Qualifier("tradeCsvReader") ItemReader<TransactionCsvRecord> tradeCsvReader,
      @Qualifier("tradeProcessor")
          ItemProcessor<TransactionCsvRecord, TradeTransaction> tradeProcessor,
      @Qualifier("tradeWriter") ItemWriter<TradeTransaction> tradeWriter) {
    return new StepBuilder("tradeIngestionStep", jobRepository)
        .<TransactionCsvRecord, TradeTransaction>chunk(500, transactionManager)
        .reader(tradeCsvReader)
        .processor(tradeProcessor)
        .writer(tradeWriter)
        .build();
  }

  @Bean
  public Step ledgerIngestionStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      @Qualifier("ledgerCsvReader") ItemReader<TransactionCsvRecord> ledgerCsvReader,
      @Qualifier("ledgerProcessor")
          ItemProcessor<TransactionCsvRecord, LedgerTransaction> ledgerProcessor,
      @Qualifier("ledgerWriter") ItemWriter<LedgerTransaction> ledgerWriter) {
    return new StepBuilder("ledgerIngestionStep", jobRepository)
        .<TransactionCsvRecord, LedgerTransaction>chunk(500, transactionManager)
        .reader(ledgerCsvReader)
        .processor(ledgerProcessor)
        .writer(ledgerWriter)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<TransactionCsvRecord> tradeCsvReader(
      IngestionSourceProvider sourceProvider) {
    return csvReader(sourceProvider.latestTradeResource());
  }

  @Bean
  @StepScope
  public FlatFileItemReader<TransactionCsvRecord> ledgerCsvReader(
      IngestionSourceProvider sourceProvider) {
    return csvReader(sourceProvider.latestLedgerResource());
  }

  private FlatFileItemReader<TransactionCsvRecord> csvReader(Resource resource) {
    return new FlatFileItemReaderBuilder<TransactionCsvRecord>()
        .name("transactionCsvReader")
        .resource(resource)
        .linesToSkip(1)
        .lineMapper(lineMapper())
        .build();
  }

  private DefaultLineMapper<TransactionCsvRecord> lineMapper() {
    DefaultLineMapper<TransactionCsvRecord> mapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setNames("trade_id", "account_id", "currency", "amount", "trade_date", "source_file");
    tokenizer.setIncludedFields(0, 1, 2, 3, 4, 5);
    tokenizer.setStrict(false);
    mapper.setLineTokenizer(tokenizer);
    mapper.setFieldSetMapper(new TransactionFieldSetMapper());
    return mapper;
  }

  @Bean
  public ItemProcessor<TransactionCsvRecord, TradeTransaction> tradeProcessor() {
    return new TradeTransactionItemProcessor();
  }

  @Bean
  public ItemProcessor<TransactionCsvRecord, LedgerTransaction> ledgerProcessor() {
    return new LedgerTransactionItemProcessor();
  }

  @Bean
  public ItemWriter<TradeTransaction> tradeWriter(TradeTransactionRepository repository) {
    return repository::saveAll;
  }

  @Bean
  public ItemWriter<LedgerTransaction> ledgerWriter(LedgerTransactionRepository repository) {
    return repository::saveAll;
  }
}
