package com.barclays.settlement.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ReconciliationRequest {

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private String portfolio;
}
