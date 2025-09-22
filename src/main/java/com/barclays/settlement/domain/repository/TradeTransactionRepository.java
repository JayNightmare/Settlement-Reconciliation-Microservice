package com.barclays.settlement.domain.repository;

import com.barclays.settlement.domain.entity.TradeTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {
  List<TradeTransaction> findByTradeDateBetween(LocalDate start, LocalDate end);

  Optional<TradeTransaction> findByTradeId(String tradeId);
}
