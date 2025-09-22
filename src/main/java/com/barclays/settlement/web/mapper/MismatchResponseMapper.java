package com.barclays.settlement.web.mapper;

import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.web.dto.MismatchResponse;
import org.springframework.stereotype.Component;

@Component
public class MismatchResponseMapper {

  public MismatchResponse toDto(ReconciliationResult result) {
    return MismatchResponse.builder()
        .id(result.getId())
        .tradeId(result.getTradeId())
        .accountId(result.getAccountId())
        .currency(result.getCurrency())
        .tradeAmount(result.getTradeAmount())
        .ledgerAmount(result.getLedgerAmount())
        .mismatchType(result.getMismatchType())
        .status(result.getStatus())
        .mismatchReason(result.getMismatchReason())
        .lastReconciledAt(result.getLastReconciledAt())
        .acknowledgedAt(result.getAcknowledgedAt())
        .acknowledgedBy(result.getAcknowledgedBy())
        .build();
  }
}
