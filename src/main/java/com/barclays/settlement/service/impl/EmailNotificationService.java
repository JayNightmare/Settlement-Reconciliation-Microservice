package com.barclays.settlement.service.impl;

import com.barclays.settlement.config.ReconciliationProperties;
import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

  private final JavaMailSender mailSender;
  private final ReconciliationProperties reconciliationProperties;

  @Override
  public void notifyUnresolvedMismatches(List<ReconciliationResult> results) {
    if (results.isEmpty()) {
      return;
    }
    String recipient = reconciliationProperties.getEscalation().getNotificationEmail();
    log.info("Escalating {} unresolved mismatches to {}", results.size(), recipient);
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(recipient);
    message.setSubject("Settlement reconciliation mismatches pending");
    message.setText(buildBody(results));
    try {
      mailSender.send(message);
    } catch (MailException mailException) {
      log.warn("Failed to send escalation email", mailException);
    }
  }

  private String buildBody(List<ReconciliationResult> results) {
    StringBuilder builder = new StringBuilder();
    builder.append("The following mismatches breached SLA:\n");
    results.forEach(
        result ->
            builder
                .append("Trade ")
                .append(result.getTradeId())
                .append(" (reason: ")
                .append(result.getMismatchReason())
                .append(")\n"));
    return builder.toString();
  }
}
