# Projeto Final Spring AI — API de Orçamento por Voz

## O que o projeto faz

Este projeto é uma **API de controle de gastos pessoais controlada por voz**, desenvolvida como projeto final do módulo Spring AI da DIO.

Você envia um áudio com um comando em português (por exemplo: *"registre 30 reais de mercado hoje"* ou *"quanto gastei com farmácia este mês?"*), e a API:

1. **Transcreve o áudio** para texto usando o Whisper da OpenAI.
2. **Interpreta o comando** com o GPT-4o-mini, que escolhe a ferramenta correta.
3. **Executa a ação** (salvar ou consultar transações no banco de dados MySQL).
4. **Responde em voz** — gera um áudio MP3 com a resposta.

Há também endpoints REST convencionais para criar e listar transações sem usar voz.

---

## Como executar

### Pré-requisitos
- Java 21+
- Docker (para subir o MySQL via Docker Compose automaticamente)
- Chave de API da OpenAI

### 1. Configure a chave da OpenAI

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY = "sk-..."
```

**Linux / Mac:**
```bash
export OPENAI_API_KEY="sk-..."
```

> ⚠️ **Nunca coloque a chave diretamente em `application.properties` ou faça commit dela.**

### 2. Execute a aplicação

```bash
./gradlew bootRun
```

O Spring Boot vai subir o MySQL automaticamente via Docker Compose.  
A API ficará disponível em `http://localhost:8080`.

### 3. Execute os testes

```bash
./gradlew test
```

Os **testes unitários** (sem banco nem chave de API) rodam sempre.  
Os **testes de integração** (`*IT`) só rodam se a variável `OPENAI_API_KEY` estiver definida.

---

## Tecnologias utilizadas

| Tecnologia | Versão | Papel no projeto |
|---|---|---|
| Spring Boot | 4.0.5 | Framework principal |
| Spring AI | 2.0.0-M4 | Integração com modelos de IA |
| Spring Web | — | Endpoints REST |
| Spring Data JPA | — | Persistência no banco |
| MySQL | 9.6 (Docker) | Banco de dados |
| OpenAI Whisper | `whisper-1` | Transcrição de áudio para texto |
| OpenAI GPT-4o-mini | — | Interpretação de comandos e tool calling |
| OpenAI TTS | `gpt-4o-mini-tts` | Geração de resposta em áudio |
| Lombok | 9.2.0 | Redução de boilerplate |
| JUnit 5 + Mockito | — | Testes unitários |

---

## Arquitetura

O projeto segue **DDD em camadas**, sem frameworks misturados no domínio:

```
domain/          → modelo de negócio (Transaction, Category) e contrato do repositório
application/     → use cases (Persist, List, Summarize) com validações de negócio
infrastructure/  → controllers HTTP, entidades JPA, adaptadores e integração com Spring AI
```

### Tools registradas no ChatClient

| Tool | Use case | O que faz |
|---|---|---|
| `persist-transaction` | `PersistTransactionUseCase` | Salva uma transação (com validações) |
| `list-transactions-by-category` | `ListTransactionsByCategoryUseCase` | Lista transações de uma categoria |
| `summarize-category-expenses` | `SummarizeCategoryExpensesUseCase` | Retorna total gasto e quantidade por categoria e período |

---

## Melhoria implementada — Resumo de gastos por categoria e período

### O que foi adicionado

Foi implementada a feature **"resumo de gastos por categoria e período"**, que permite perguntas como *"quanto gastei com mercado este mês?"*.

**Mudanças realizadas (do menor ao maior impacto):**

1. **`Transaction` + `TransactionEntity`** — adicionado campo `date` (LocalDate) com valor padrão de hoje.
2. **`TransactionRepository`** — novo método `findAllByCategoryIgnoreCaseAndDateBetween`.
3. **`TransactionEntityRepository`** — novo query method do Spring Data com busca por categoria (case-insensitive) e intervalo de datas.
4. **`JpaTransactionRepository`** — implementação do novo método do contrato de domínio.
5. **`PersistTransactionInput`** — adicionado campo `date` (opcional, padrão = hoje).
6. **`PersistTransactionUseCase`** — adicionadas validações: descrição não pode ser vazia e valor deve ser maior que zero.
7. **`CategorySummaryOutput`** — novo DTO de saída com `category`, `totalAmount` (BigDecimal) e `transactionCount`.
8. **`SummarizeCategoryExpensesUseCase`** — novo use case com validações de período e conversão de centavos para reais.
9. **`TransactionController`** — registra o novo use case como tool e injeta a data de hoje no prompt de sistema a cada requisição.
10. **`system-message.st`** — adicionado `{today}` para a IA entender "hoje", "ontem" e "este mês".

---

## Como testar o fluxo principal (via voz)

Grave um áudio `.mp3` com o comando e envie para `POST /transactions/ai`:

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@meu_audio.mp3" \
  --output resposta.mp3
```

### Exemplos de comandos e respostas esperadas

| Comando de voz | O que acontece | Resposta esperada |
|---|---|---|
| *"Registre 30 reais de mercado hoje"* | Salva transação GROCERIES = 3000 centavos | *"Transação registrada: R$ 30,00 em GROCERIES."* |
| *"Paguei 50 reais de farmácia ontem"* | Salva transação PHARMA com a data de ontem | *"Transação registrada: R$ 50,00 em PHARMA."* |
| *"Quanto gastei com mercado este mês?"* | Consulta GROCERIES no período do mês atual | *"Você gastou R$ 30,00 em 1 transação de GROCERIES este mês."* |
| *"Liste minhas transações de farmácia"* | Lista todas as transações PHARMA | *"Encontrei 1 transação de PHARMA..."* |

### Via REST (sem voz)

**Criar transação:**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Supermercado","category":"GROCERIES","amount":4500}'
```

**Listar por categoria:**
```bash
curl http://localhost:8080/transactions/GROCERIES
```

---

## O que aprendi

> *Esta seção é para você completar com suas próprias palavras após a entrega.* 🙂
>
> Algumas sugestões do que pode mencionar:
> - Como o Spring AI conecta modelos de IA com o código Java usando `@Tool`
> - A diferença entre `ChatModel` (chamada direta) e `ChatClient` (com contexto e ferramentas)
> - Por que separar a lógica de negócio em use cases facilita os testes
> - Como o Whisper converte voz para texto e o TTS faz o caminho inverso
> - O que é tool calling e como o modelo decide qual ferramenta usar

---

## Referências

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)
- [ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Tools API](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Audio Transcriptions API](https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html)
- [Audio Speech API](https://docs.spring.io/spring-ai/reference/api/audio/speech.html)
- [Arquitetura compartilhada do repositório](../README.md)
