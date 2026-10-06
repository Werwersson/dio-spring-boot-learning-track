package dio.budgeting.domain;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    List<Transaction> findAllByCategory(Category category);

    /**
     * Busca transações por categoria (sem diferenciar maiúsculas/minúsculas)
     * dentro de um intervalo de datas (inclusive em ambas as pontas).
     */
    List<Transaction> findAllByCategoryIgnoreCaseAndDateBetween(String category, LocalDate startDate, LocalDate endDate);
}
