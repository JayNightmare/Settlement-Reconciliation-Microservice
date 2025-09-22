package com.barclays.settlement.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ledger_transactions")
@Getter
@Setter
@NoArgsConstructor
public class LedgerTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "trade_id", nullable = false, length = 64)
  private String tradeId;

  @Column(name = "account_id", nullable = false, length = 64)
  private String accountId;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(name = "trade_date", nullable = false)
  private LocalDate tradeDate;

  @Column(name = "source_file", length = 256)
  private String sourceFile;

  @Column(name = "ingested_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant ingestedAt = Instant.now();
}
