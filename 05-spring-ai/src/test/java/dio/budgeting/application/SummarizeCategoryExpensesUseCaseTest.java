package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do SummarizeCategoryExpensesUseCase.
 * Não dependem de banco de dados nem de chave de API.
 */
@ExtendWith(MockitoExtension.class)
class SummarizeCategoryExpensesUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private SummarizeCategoryExpensesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SummarizeCategoryExpensesUseCase(transactionRepository);
    }

    @Test
    void shouldReturnCorrectSumAndCount_whenTransactionsExist() {
        // amount é em centavos: 1500 = R$ 15,00 | 3000 = R$ 30,00
        var t1 = new Transaction("Pão", 1500L, Category.GROCERIES, LocalDate.of(2025, 10, 1));
        var t2 = new Transaction("Leite", 3000L, Category.GROCERIES, LocalDate.of(2025, 10, 3));

        when(transactionRepository.findAllByCategoryIgnoreCaseAndDateBetween(
                "GROCERIES", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31)))
                .thenReturn(List.of(t1, t2));

        CategorySummaryOutput result = useCase.execute("GROCERIES", "2025-10-01", "2025-10-31");

        assertThat(result.transactionCount()).isEqualTo(2);
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(result.category()).isEqualTo("GROCERIES");
    }

    @Test
    void shouldReturnZero_whenNoTransactionsFound() {
        when(transactionRepository.findAllByCategoryIgnoreCaseAndDateBetween(
                "PHARMA", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31)))
                .thenReturn(List.of());

        CategorySummaryOutput result = useCase.execute("PHARMA", "2025-10-01", "2025-10-31");

        assertThat(result.transactionCount()).isZero();
        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldThrow_whenStartDateIsAfterEndDate() {
        assertThatThrownBy(() -> useCase.execute("GROCERIES", "2025-10-31", "2025-10-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data inicial não pode ser posterior");
    }

    @Test
    void shouldThrow_whenCategoryIsBlank() {
        assertThatThrownBy(() -> useCase.execute("  ", "2025-10-01", "2025-10-31"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria não pode ser vazia");
    }

    @Test
    void shouldThrow_whenCategoryIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, "2025-10-01", "2025-10-31"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria não pode ser vazia");
    }

    @Test
    void shouldThrow_whenDateFormatIsInvalid() {
        assertThatThrownBy(() -> useCase.execute("GROCERIES", "01/10/2025", "31/10/2025"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato yyyy-MM-dd");
    }
}
