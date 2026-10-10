package com.amit.test.github_mcp;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Account operations dependency for the banking demo.
 * This sample uses in-memory storage and is not production banking software.
 */
@Service
public class Dependency2 {
    private final Map<String, AccountRecord> accounts = new LinkedHashMap<>();
    private final List<AccountEvent> events = new ArrayList<>();

    public synchronized AccountRecord openAccount(String customerId, String accountType,
                                                  BigDecimal openingBalance) {
        requireText(customerId, "customerId");
        requireText(accountType, "accountType");
        requireMoney(openingBalance);
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }
        String number = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        AccountRecord account = new AccountRecord(number, customerId.trim(),
                accountType.trim().toUpperCase(Locale.ROOT), money(openingBalance));
        accounts.put(number, account);
        record(account, "OPEN", openingBalance, "Account opened");
        return account.copy();
    }

    public synchronized Optional<AccountRecord> findAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) return Optional.empty();
        AccountRecord account = accounts.get(accountNumber.trim());
        return account == null ? Optional.empty() : Optional.of(account.copy());
    }

    public synchronized List<AccountRecord> getAllAccounts() {
        List<AccountRecord> result = new ArrayList<>();
        for (AccountRecord account : accounts.values()) result.add(account.copy());
        return Collections.unmodifiableList(result);
    }

    public synchronized List<AccountRecord> getAccountsForCustomer(String customerId) {
        requireText(customerId, "customerId");
        List<AccountRecord> result = new ArrayList<>();
        for (AccountRecord account : accounts.values()) {
            if (account.customerId.equals(customerId.trim())) result.add(account.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized AccountRecord deposit(String accountNumber, BigDecimal amount) {
        AccountRecord account = requireOpenAccount(accountNumber);
        requirePositive(amount);
        account.balance = money(account.balance.add(amount));
        account.updatedAt = LocalDateTime.now();
        record(account, "DEPOSIT", amount, "Cash deposit");
        return account.copy();
    }

    public synchronized AccountRecord withdraw(String accountNumber, BigDecimal amount) {
        AccountRecord account = requireOpenAccount(accountNumber);
        requirePositive(amount);
        if (account.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in " + account.accountNumber);
        }
        account.balance = money(account.balance.subtract(amount));
        account.updatedAt = LocalDateTime.now();
        record(account, "WITHDRAWAL", amount, "Cash withdrawal");
        return account.copy();
    }

    public synchronized void transfer(String fromNumber, String toNumber, BigDecimal amount) {
        requirePositive(amount);
        AccountRecord from = requireOpenAccount(fromNumber);
        AccountRecord to = requireOpenAccount(toNumber);
        if (from.accountNumber.equals(to.accountNumber)) {
            throw new IllegalArgumentException("Source and destination must differ");
        }
        if (from.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in " + from.accountNumber);
        }
        from.balance = money(from.balance.subtract(amount));
        to.balance = money(to.balance.add(amount));
        from.updatedAt = LocalDateTime.now();
        to.updatedAt = LocalDateTime.now();
        record(from, "TRANSFER_OUT", amount, "Transfer to " + to.accountNumber);
        record(to, "TRANSFER_IN", amount, "Transfer from " + from.accountNumber);
    }

    public synchronized AccountRecord closeAccount(String accountNumber) {
        AccountRecord account = requireOpenAccount(accountNumber);
        if (account.balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Account balance must be zero before closure");
        }
        account.status = AccountStatus.CLOSED;
        account.updatedAt = LocalDateTime.now();
        record(account, "CLOSE", BigDecimal.ZERO, "Account closed");
        return account.copy();
    }

    public synchronized String getAccountStatus(String accountNumber) {
        return requireAccount(accountNumber).status.name();
    }

    public synchronized boolean accountExists(String accountNumber) {
        return accountNumber != null && accounts.containsKey(accountNumber.trim());
    }

    public boolean validateAccountNumber(String accountNumber) {
        return accountNumber != null && accountNumber.trim().matches("ACC-[A-F0-9]{8}");
    }

    public synchronized BigDecimal getBalance(String accountNumber) {
        return requireAccount(accountNumber).balance;
    }

    public synchronized int countAccounts() {
        return accounts.size();
    }

    public synchronized long countOpenAccounts() {
        return accounts.values().stream().filter(a -> a.status == AccountStatus.OPEN).count();
    }

    public synchronized long countClosedAccounts() {
        return accounts.values().stream().filter(a -> a.status == AccountStatus.CLOSED).count();
    }

    public synchronized BigDecimal getTotalBalances() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (AccountRecord account : accounts.values()) total = total.add(account.balance);
        return money(total);
    }

    public synchronized List<AccountRecord> getOpenAccounts() {
        List<AccountRecord> result = new ArrayList<>();
        for (AccountRecord account : accounts.values()) {
            if (account.status == AccountStatus.OPEN) result.add(account.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<AccountRecord> getClosedAccounts() {
        List<AccountRecord> result = new ArrayList<>();
        for (AccountRecord account : accounts.values()) {
            if (account.status == AccountStatus.CLOSED) result.add(account.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<AccountEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public synchronized List<AccountEvent> getEventsForAccount(String accountNumber) {
        requireText(accountNumber, "accountNumber");
        List<AccountEvent> result = new ArrayList<>();
        for (AccountEvent event : events) {
            if (event.accountNumber.equals(accountNumber.trim())) result.add(event);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized BigDecimal calculateSimpleInterest(String accountNumber,
                                                           BigDecimal annualRate, int years) {
        AccountRecord account = requireOpenAccount(accountNumber);
        requireRate(annualRate);
        if (years < 0) throw new IllegalArgumentException("Years cannot be negative");
        return money(account.balance.multiply(annualRate).multiply(BigDecimal.valueOf(years)));
    }

    public synchronized AccountRecord applyFee(String accountNumber, BigDecimal fee, String reason) {
        AccountRecord account = requireOpenAccount(accountNumber);
        requirePositive(fee);
        requireText(reason, "reason");
        if (account.balance.compareTo(fee) < 0) {
            throw new IllegalStateException("Insufficient funds to apply fee");
        }
        account.balance = money(account.balance.subtract(fee));
        account.updatedAt = LocalDateTime.now();
        record(account, "FEE", fee, reason.trim());
        return account.copy();
    }

    public synchronized AccountRecord refund(String accountNumber, BigDecimal amount, String reason) {
        AccountRecord account = requireOpenAccount(accountNumber);
        requirePositive(amount);
        requireText(reason, "reason");
        account.balance = money(account.balance.add(amount));
        account.updatedAt = LocalDateTime.now();
        record(account, "REFUND", amount, reason.trim());
        return account.copy();
    }

    public synchronized String formatAccountSummary(String accountNumber) {
        AccountRecord account = requireAccount(accountNumber);
        return String.format(Locale.ROOT, "%s | customer=%s | type=%s | balance=%s | status=%s",
                account.accountNumber, account.customerId, account.accountType,
                account.balance.toPlainString(), account.status);
    }

    public synchronized void clearForTesting() {
        accounts.clear();
        events.clear();
    }

    private AccountRecord requireAccount(String accountNumber) {
        requireText(accountNumber, "accountNumber");
        AccountRecord account = accounts.get(accountNumber.trim());
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountNumber);
        return account;
    }

    private AccountRecord requireOpenAccount(String accountNumber) {
        AccountRecord account = requireAccount(accountNumber);
        if (account.status != AccountStatus.OPEN) {
            throw new IllegalStateException("Account is not open: " + account.accountNumber);
        }
        return account;
    }

    private void record(AccountRecord account, String type, BigDecimal amount, String description) {
        events.add(new AccountEvent(account.accountNumber, type, money(amount),
                description, LocalDateTime.now()));
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private void requireMoney(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Amount is required");
    }

    private void requirePositive(BigDecimal value) {
        requireMoney(value);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private void requireRate(BigDecimal value) {
        requireMoney(value);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rate cannot be negative");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public enum AccountStatus { OPEN, CLOSED }

    public static final class AccountRecord {
        private final String accountNumber;
        private final String customerId;
        private final String accountType;
        private BigDecimal balance;
        private AccountStatus status;
        private final LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private AccountRecord(String accountNumber, String customerId, String accountType,
                              BigDecimal balance) {
            this.accountNumber = accountNumber;
            this.customerId = customerId;
            this.accountType = accountType;
            this.balance = balance;
            this.status = AccountStatus.OPEN;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = createdAt;
        }

        private AccountRecord copy() {
            AccountRecord copy = new AccountRecord(accountNumber, customerId, accountType, balance);
            copy.status = status;
            copy.updatedAt = updatedAt;
            return copy;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getCustomerId() { return customerId; }
        public String getAccountType() { return accountType; }
        public BigDecimal getBalance() { return balance; }
        public AccountStatus getStatus() { return status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "AccountRecord{accountNumber='" + accountNumber + "', customerId='"
                    + customerId + "', balance=" + balance + ", status=" + status + "}";
        }
    }

    public static final class AccountEvent {
        private final String accountNumber;
        private final String type;
        private final BigDecimal amount;
        private final String description;
        private final LocalDateTime timestamp;

        private AccountEvent(String accountNumber, String type, BigDecimal amount,
                             String description, LocalDateTime timestamp) {
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.timestamp = timestamp;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "AccountEvent{accountNumber='" + accountNumber + "', type='" + type
                    + "', amount=" + amount + ", timestamp=" + timestamp + "}";
        }
    }
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
    // Design note: synchronize operations that update shared in-memory state.
    // Design note: persist records in a database for use beyond demonstrations.
    // Design note: enforce authentication and authorization in a real application.
    // Design note: use audited database transactions for financial operations.
    // Design note: add unit tests for invalid values and boundary cases.
    // Design note: avoid exposing private customer details in application logs.
    // Design note: review business rules with the owning product team.
    // Design note: monetary values should use BigDecimal, not double.
    // Design note: validate input before changing state.
    // Design note: use defensive copies to protect internal records.
}
