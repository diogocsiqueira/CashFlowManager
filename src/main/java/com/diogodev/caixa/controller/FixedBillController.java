package com.diogodev.caixa.controller;

import com.diogodev.caixa.domain.dto.FixedBillCreateRequest;
import com.diogodev.caixa.domain.dto.FixedBillResponse;
import com.diogodev.caixa.service.FixedBillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-bills")
public class FixedBillController {

    private final FixedBillService fixedBillService;

    public FixedBillController(FixedBillService fixedBillService) {
        this.fixedBillService = fixedBillService;
    }

    @GetMapping
    public List<FixedBillResponse> listActive() {
        return fixedBillService.listActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FixedBillResponse create(@Valid @RequestBody FixedBillCreateRequest req) {
        return fixedBillService.create(req);
    }
}
