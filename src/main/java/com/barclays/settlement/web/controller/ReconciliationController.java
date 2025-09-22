package com.barclays.settlement.web.controller;

import com.barclays.settlement.domain.entity.ReconciliationResult;
import com.barclays.settlement.domain.model.MismatchType;
import com.barclays.settlement.domain.model.ReconciliationStatus;
import com.barclays.settlement.domain.repository.ReconciliationResultRepository;
import com.barclays.settlement.service.IngestionService;
import com.barclays.settlement.service.ReconciliationService;
import com.barclays.settlement.service.model.ReconciliationSummary;
import com.barclays.settlement.web.dto.MismatchResponse;
import com.barclays.settlement.web.dto.ReconciliationRequest;
import com.barclays.settlement.web.dto.ReconciliationSummaryResponse;
import com.barclays.settlement.web.dto.ResolveMismatchRequest;
import com.barclays.settlement.web.mapper.MismatchResponseMapper;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Validated
public class ReconciliationController {

  private final ReconciliationService reconciliationService;
  private final IngestionService ingestionService;
  private final ReconciliationResultRepository reconciliationResultRepository;
  private final MismatchResponseMapper mismatchResponseMapper;

  @PostMapping("/run")
  @PreAuthorize("hasAnyRole('OPS_ANALYST','ADMIN')")
  public ReconciliationSummaryResponse runReconciliation(
      @Valid @RequestBody ReconciliationRequest request) {
    ReconciliationSummary summary =
        reconciliationService.runReconciliation(
            request.getStartDate(), request.getEndDate(), request.getPortfolio());
    return toResponse(summary);
  }

  @PostMapping("/ingest")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasRole('ADMIN')")
  public void triggerIngestion() {
    ingestionService.runIngestionJob();
  }

  @GetMapping("/mismatches")
  @PreAuthorize("hasAnyRole('OPS_ANALYST','ADMIN')")
  public Page<MismatchResponse> listMismatches(
      @RequestParam(defaultValue = "UNRESOLVED") ReconciliationStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return reconciliationResultRepository
        .findByStatus(status, pageable)
        .map(mismatchResponseMapper::toDto);
  }

  @PostMapping("/mismatches/{id}/acknowledge")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('OPS_ANALYST','ADMIN')")
  public void acknowledgeMismatch(
      @PathVariable Long id,
      @Valid @RequestBody ResolveMismatchRequest request,
      Principal principal) {
    reconciliationService.acknowledgeMismatch(
        id, principal.getName(), request.getResolutionNotes());
  }

  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('OPS_ANALYST','ADMIN')")
  public ReconciliationSummaryResponse getSummary() {
    List<ReconciliationResult> results = reconciliationResultRepository.findAll();
    Map<MismatchType, Long> mismatchCounts =
        results.stream()
            .collect(
                Collectors.groupingBy(
                    ReconciliationResult::getMismatchType, Collectors.counting()));
    Map<ReconciliationStatus, Long> statusCounts =
        results.stream()
            .collect(Collectors.groupingBy(ReconciliationResult::getStatus, Collectors.counting()));
    ReconciliationSummary summary =
        ReconciliationSummary.builder()
            .totalProcessed(results.size())
            .mismatches(results.size())
            .mismatchesByType(mismatchCounts)
            .statusCounts(statusCounts)
            .build();
    return toResponse(summary);
  }

  private ReconciliationSummaryResponse toResponse(ReconciliationSummary summary) {
    return ReconciliationSummaryResponse.builder()
        .totalProcessed(summary.getTotalProcessed())
        .mismatches(summary.getMismatches())
        .mismatchesByType(
            summary.getMismatchesByType().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)))
        .statusCounts(
            summary.getStatusCounts().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)))
        .build();
  }
}
