package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "persist-transaction",
            description = "Persiste uma nova transação financeira com descrição, valor, categoria e data")
    public TransactionOutput execute(PersistTransactionInput input) {
        if (input.description() == null || input.description().isBlank()) {
            throw new IllegalArgumentException("A descrição da transação não pode ser vazia.");
        }
        if (input.amount() <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        var date = input.date() != null ? input.date() : java.time.LocalDate.now();

        var transaction = transactionRepository.save(
                new Transaction(input.description(), input.amount(), input.category(), date));

        return TransactionOutput.from(transaction);
    }
}
