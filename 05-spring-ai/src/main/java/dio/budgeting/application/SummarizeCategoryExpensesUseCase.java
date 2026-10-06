package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class SummarizeCategoryExpensesUseCase {

    private final TransactionRepository transactionRepository;

    public SummarizeCategoryExpensesUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-category-expenses",
            description = "Retorna o total gasto e a quantidade de transações de uma categoria em um período de datas")
    public CategorySummaryOutput execute(
            @ToolParam(description = "Nome da categoria (ex: GROCERIES, PHARMA, AUTO)") String category,
            @ToolParam(description = "Data inicial do período no formato yyyy-MM-dd (inclusive)") String startDate,
            @ToolParam(description = "Data final do período no formato yyyy-MM-dd (inclusive)") String endDate) {

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("A categoria não pode ser vazia.");
        }

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Datas inválidas. Use o formato yyyy-MM-dd (ex: 2025-10-01).");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final.");
        }

        var transactions = transactionRepository
                .findAllByCategoryIgnoreCaseAndDateBetween(category, start, end);

        // amount é armazenado em centavos — divide por 100 para reais
        BigDecimal total = transactions.stream()
                .map(t -> BigDecimal.valueOf(t.getAmount())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CategorySummaryOutput(category.toUpperCase(), total, transactions.size());
    }
}
