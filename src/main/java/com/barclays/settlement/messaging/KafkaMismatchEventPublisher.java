package com.barclays.settlement.messaging;

import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.event.MismatchEvent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMismatchEventPublisher implements MismatchEventPublisher {

  private final KafkaTemplate<String, MismatchEvent> kafkaTemplate;

  @Value("${reconciliation.kafka.mismatch-topic:settlement.mismatches}")
  private String topicName;

  @Override
  public void publish(ReconciliationResult result) {
    MismatchEvent event =
        MismatchEvent.builder()
            .tradeId(result.getTradeId())
            .accountId(result.getAccountId())
            .currency(result.getCurrency())
            .tradeAmount(result.getTradeAmount())
            .ledgerAmount(result.getLedgerAmount())
            .mismatchType(result.getMismatchType())
            .status(result.getStatus())
            .mismatchReason(result.getMismatchReason())
            .eventTime(Instant.now())
            .build();
    kafkaTemplate.send(topicName, result.getTradeId(), event);
  }
}
