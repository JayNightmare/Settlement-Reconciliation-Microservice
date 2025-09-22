package com.barclays.settlement.messaging;

import com.barclays.settlement.domain.entity.ReconciliationResult;

public interface MismatchEventPublisher {
  void publish(ReconciliationResult result);
}
