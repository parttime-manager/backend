package com.ptmanager.backend.workplace;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.Workplace;
import com.ptmanager.backend.labor.LaborCostReport;
import com.ptmanager.backend.labor.LaborCostService;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/workplaces")
public class WorkplaceController {

    private final WorkplaceService workplaceService;
    private final LaborCostService laborCostService;

    public WorkplaceController(WorkplaceService workplaceService, LaborCostService laborCostService) {
        this.workplaceService = workplaceService;
        this.laborCostService = laborCostService;
    }

    @GetMapping
    public List<Workplace> findWorkplaces() {
        return workplaceService.findWorkplaces();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Workplace createWorkplace(@Valid @RequestBody CreateWorkplaceRequest request) {
        return workplaceService.createWorkplace(request.name(), request.address());
    }

    @GetMapping("/{id}/labor-cost")
    public LaborCostReport laborCost(
        @PathVariable long id,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return laborCostService.calculate(id, from, to);
    }

    public record CreateWorkplaceRequest(
        @NotBlank String name,
        String address
    ) {
    }
}
