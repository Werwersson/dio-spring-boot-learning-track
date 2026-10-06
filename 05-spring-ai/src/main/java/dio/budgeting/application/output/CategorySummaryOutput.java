package dio.budgeting.application.output;

import java.math.BigDecimal;

/**
 * Resultado do resumo de gastos por categoria e período.
 *
 * @param category           nome da categoria consultada
 * @param totalAmount        valor total gasto (em reais, já convertido de centavos)
 * @param transactionCount   quantidade de transações encontradas no período
 */
public record CategorySummaryOutput(String category, BigDecimal totalAmount, int transactionCount) {
}
