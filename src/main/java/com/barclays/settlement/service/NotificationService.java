package com.barclays.settlement.service;

import com.barclays.settlement.domain.entity.ReconciliationResult;
import java.util.List;

public interface NotificationService {
  void notifyUnresolvedMismatches(List<ReconciliationResult> results);
}
