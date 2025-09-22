package com.barclays.settlement.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "reconciliation")
public class ReconciliationProperties {

  private Map<String, BigDecimal> tolerances = new HashMap<>();

  private Escalation escalation = new Escalation();

  @Data
  public static class Escalation {
    private Duration sla = Duration.ofHours(24);
    private String notificationEmail;
  }
}
