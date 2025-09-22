package com.barclays.settlement.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveMismatchRequest {
  @NotBlank private String resolutionNotes;
}
