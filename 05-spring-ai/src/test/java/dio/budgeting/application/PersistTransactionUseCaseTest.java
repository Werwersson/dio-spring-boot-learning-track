package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários das validações do PersistTransactionUseCase.
 * Não dependem de banco de dados nem de chave de API.
 */
@ExtendWith(MockitoExtension.class)
class PersistTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PersistTransactionUseCase(transactionRepository);
    }

    @Test
    void shouldThrow_whenAmountIsZero() {
        var input = new PersistTransactionInput("Mercado", 0L, Category.GROCERIES, LocalDate.now());
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void shouldThrow_whenAmountIsNegative() {
        var input = new PersistTransactionInput("Uber", -500L, Category.AUTO, LocalDate.now());
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void shouldThrow_whenDescriptionIsBlank() {
        var input = new PersistTransactionInput("   ", 1000L, Category.GROCERIES, LocalDate.now());
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void shouldThrow_whenDescriptionIsNull() {
        var input = new PersistTransactionInput(null, 1000L, Category.GROCERIES, LocalDate.now());
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição");
    }
}
