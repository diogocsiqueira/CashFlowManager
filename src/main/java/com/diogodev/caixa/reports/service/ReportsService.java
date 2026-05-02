package com.diogodev.caixa.reports.service;

import com.diogodev.caixa.fixedbills.repository.FixedBillRepository;
import com.diogodev.caixa.reports.dto.ReportFilterDTO;
import com.diogodev.caixa.reports.dto.ReportsOverviewDTO;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
public class ReportsService {

    private final TransactionRepository transactionRepository;
    private final FixedBillRepository fixedBillRepository;

    public ReportsService(
            TransactionRepository transactionRepository,
            FixedBillRepository fixedBillRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.fixedBillRepository = fixedBillRepository;
    }

    public ReportsOverviewDTO getOverview(Long userId, ReportFilterDTO filter) {
        var startDate = filter.startDate();
        var endDate = filter.endDate();
        var categoryId = filter.categoryId();

        var totalIncome = transactionRepository.sumByType(
                userId,
                TransactionType.INCOME,
                startDate,
                endDate,
                categoryId
        );

        var totalExpense = transactionRepository.sumByType(
                userId,
                TransactionType.EXPENSE,
                startDate,
                endDate,
                categoryId
        );

        var pendingAmount = fixedBillRepository.sumPendingActiveBills(
                userId,
                YearMonth.from(startDate),
                YearMonth.from(endDate),
                categoryId
        );

        var balance = totalIncome.subtract(totalExpense);

        var expensesByCategory = transactionRepository.expensesByCategory(
                userId,
                startDate,
                endDate,
                categoryId,
                TransactionType.EXPENSE
        );

        var monthlyBalance = transactionRepository.monthlyBalance(
                userId,
                startDate,
                endDate,
                categoryId,
                TransactionType.INCOME,
                TransactionType.EXPENSE
        );

        var incomeVsExpense = transactionRepository.incomeVsExpense(
                userId,
                startDate,
                endDate,
                categoryId,
                TransactionType.INCOME,
                TransactionType.EXPENSE
        );

        return new ReportsOverviewDTO(
                totalIncome,
                totalExpense,
                balance,
                pendingAmount,
                expensesByCategory,
                monthlyBalance,
                incomeVsExpense
        );
    }
}