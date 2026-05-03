package com.diogodev.caixa.goal.controller;

import com.diogodev.caixa.goal.dto.*;
import com.diogodev.caixa.goal.service.GoalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public GoalResponse create(@RequestBody GoalCreateRequest request) {
        return goalService.create(request);
    }

    @GetMapping
    public List<GoalResponse> findAll() {
        return goalService.findAll();
    }

    @GetMapping("/{id}")
    public GoalResponse findById(@PathVariable Long id) {
        return goalService.findById(id);
    }

    @PutMapping("/{id}")
    public GoalResponse update(
            @PathVariable Long id,
            @RequestBody GoalUpdateRequest request
    ) {
        return goalService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        goalService.delete(id);
    }

    @PostMapping("/{goalId}/contributions")
    public GoalContributionResponse createContribution(
            @PathVariable Long goalId,
            @RequestBody GoalContributionCreateRequest request
    ) {
        return goalService.createContribution(goalId, request);
    }

    @GetMapping("/{goalId}/contributions")
    public List<GoalContributionResponse> findContributions(@PathVariable Long goalId) {
        return goalService.findContributions(goalId);
    }
}