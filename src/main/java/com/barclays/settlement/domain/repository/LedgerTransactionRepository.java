package com.barclays.settlement.domain.repository;

import com.barclays.settlement.domain.entity.LedgerTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {
  List<LedgerTransaction> findByTradeDateBetween(LocalDate start, LocalDate end);

  Optional<LedgerTransaction> findByTradeId(String tradeId);
}
