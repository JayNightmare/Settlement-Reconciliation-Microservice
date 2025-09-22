package com.barclays.settlement.domain.repository;

import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {
  Page<ReconciliationResult> findByStatus(ReconciliationStatus status, Pageable pageable);

  @Query(
      "select r from ReconciliationResult r where r.status = :status and r.lastReconciledAt < :threshold")
  List<ReconciliationResult> findStaleWithStatus(ReconciliationStatus status, Instant threshold);
}
