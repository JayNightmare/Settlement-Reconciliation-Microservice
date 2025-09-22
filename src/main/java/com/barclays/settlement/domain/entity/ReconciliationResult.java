package com.barclays.settlement.domain.entity;

import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reconciliation_results")
@Getter
@Setter
@NoArgsConstructor
public class ReconciliationResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "trade_id", nullable = false, length = 64)
  private String tradeId;

  @Column(name = "account_id", nullable = false, length = 64)
  private String accountId;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "trade_amount", precision = 19, scale = 4)
  private BigDecimal tradeAmount;

  @Column(name = "ledger_amount", precision = 19, scale = 4)
  private BigDecimal ledgerAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "mismatch_type", nullable = false)
  private MismatchType mismatchType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReconciliationStatus status = ReconciliationStatus.UNRESOLVED;

  @Column(name = "mismatch_reason")
  private String mismatchReason;

  @Column(name = "last_reconciled_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant lastReconciledAt = Instant.now();

  @Column(name = "acknowledged_by", length = 128)
  private String acknowledgedBy;

  @Column(name = "acknowledged_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant acknowledgedAt;

  @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant updatedAt = Instant.now();
}
