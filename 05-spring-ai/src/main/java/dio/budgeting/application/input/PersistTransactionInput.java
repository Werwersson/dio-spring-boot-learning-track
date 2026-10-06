package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

public record PersistTransactionInput(
        @ToolParam(description = "Descrição do gasto") String description,
        @ToolParam(description = "Valor do gasto (em centavos, deve ser maior que zero)") long amount,
        @ToolParam(description = "Categoria de uma transação") Category category,
        @ToolParam(description = "Data da transação no formato yyyy-MM-dd (padrão: hoje)") LocalDate date) {

    /** Construtor de conveniência sem data — usa a data de hoje. */
    public PersistTransactionInput(String description, long amount, Category category) {
        this(description, amount, category, LocalDate.now());
    }
}
