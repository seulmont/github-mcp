package com.amit.test.github_mcp;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.amit.test.github_mcp.repository.CustomerDao;
import com.amit.test.github_mcp.repository.CustomerDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * =====================================================================================
 * MainFile.java
 * -------------------------------------------------------------------------------------
 * A comprehensive, self-contained Banking Application demonstrating core banking
 * operations: customer management, multiple account types, transactions, loans,
 * credit cards, interest calculation, auditing, and reporting.
 *
 * This file intentionally contains many classes in a single compilation unit for
 * portability and simplicity of demonstration. In a production system, each class
 * would live in its own file organized into packages such as:
 *   com.bank.model, com.bank.service, com.bank.exception, com.bank.util
 *
 * Contents at a glance:
 *   - Enumerations: AccountType, TransactionType, TransactionStatus, LoanStatus,
 *     CustomerType, CustomerStatus, StandingInstructionFrequency, TicketPriority,
 *     TicketStatus
 *   - Exceptions: BankingException and its subtypes covering insufficient funds,
 *     invalid accounts/amounts, duplicate accounts, authentication failures,
 *     loan approval issues, and credit limit violations
 *   - Utilities: IdGenerator, ValidationUtil, CurrencyFormatter, DateUtil
 *   - Core model: Address, Customer, Transaction, TransactionHistory
 *   - Account hierarchy: Account (abstract) with SavingsAccount, CheckingAccount,
 *     FixedDepositAccount, LoanAccount, CreditCardAccount, MoneyMarketAccount,
 *     StudentAccount
 *   - Services: CustomerService, AccountService, TransactionService, LoanService,
 *     InterestCalculator, AuditService, NotificationService, BeneficiaryService,
 *     StandingInstructionService, CurrencyExchangeService, SecurityService,
 *     ReportService, FraudDetectionService, SupportTicketService,
 *     StatementGenerator
 *   - Batch processing: BatchJob, MonthEndBatchJob, StandingInstructionBatchJob,
 *     BatchScheduler
 *   - Configuration and limits: BankConfiguration (with Builder), DailyLimitTracker
 *   - Simulated front end: MenuOption, ConsoleMenu, ConsoleMenuDemo
 *   - Aggregation facade: Bank
 *   - Demonstration entry points: MainFile.main, ExtendedBankingDemo
 *
 * Author: Banking Systems Team
 * =====================================================================================
 */
public class MainFile {

    @Autowired
    private Dependency1 dependency1;

    @Autowired
    private Dependency2 dependency2;

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("   WELCOME TO NATIONAL TRUST BANKING APPLICATION");
        System.out.println("=====================================================");

        Bank bank = new Bank("National Trust Bank", "NTB001");

        // ---------------------------------------------------------------
        // Customer onboarding
        // ---------------------------------------------------------------
        Address addr1 = new Address("221B Baker Street", "London", "Greater London", "NW1", "UK");
        Address addr2 = new Address("42 Wallaby Way", "Sydney", "NSW", "2000", "Australia");
        Address addr3 = new Address("1600 Amphitheatre Pkwy", "Mountain View", "CA", "94043", "USA");

        Customer alice = bank.getCustomerService().registerCustomer(
                "Alice Sherlock", addr1, "alice@example.com", "+44-20-7946-0958", CustomerType.INDIVIDUAL);
        Customer bob = bank.getCustomerService().registerCustomer(
                "Bob Marlin", addr2, "bob@example.com", "+61-2-9374-4000", CustomerType.INDIVIDUAL);
        Customer acme = bank.getCustomerService().registerCustomer(
                "Acme Corporation", addr3, "finance@acme.com", "+1-650-253-0000", CustomerType.BUSINESS);

        CustomerDao customerDao = new CustomerDaoImpl(connection);

        createCustomer(customerDao);
        // ---------------------------------------------------------------
        // Account creation
        // ---------------------------------------------------------------
        SavingsAccount aliceSavings = bank.getAccountService().openSavingsAccount(alice, new BigDecimal("5000.00"), new BigDecimal("0.035"));
        CheckingAccount aliceChecking = bank.getAccountService().openCheckingAccount(alice, new BigDecimal("1500.00"), new BigDecimal("500.00"));
        SavingsAccount bobSavings = bank.getAccountService().openSavingsAccount(bob, new BigDecimal("10000.00"), new BigDecimal("0.04"));
        CheckingAccount acmeChecking = bank.getAccountService().openCheckingAccount(acme, new BigDecimal("250000.00"), new BigDecimal("50000.00"));
        FixedDepositAccount bobFd = bank.getAccountService().openFixedDepositAccount(bob, new BigDecimal("20000.00"), 12, new BigDecimal("0.06"));
        CreditCardAccount aliceCredit = bank.getAccountService().openCreditCardAccount(alice, new BigDecimal("15000.00"), new BigDecimal("0.24"));

        System.out.println("\n--- Accounts Opened ---");
        System.out.println(aliceSavings);
        System.out.println(aliceChecking);
        System.out.println(bobSavings);
        System.out.println(acmeChecking);
        System.out.println(bobFd);
        System.out.println(aliceCredit);

        // ---------------------------------------------------------------
        // Transactions
        // ---------------------------------------------------------------
        TransactionService txService = bank.getTransactionService();
        try {
            txService.deposit(aliceSavings, new BigDecimal("1200.00"), "Salary deposit");
            txService.withdraw(aliceChecking, new BigDecimal("300.00"), "ATM withdrawal");
            txService.transfer(aliceSavings, bobSavings, new BigDecimal("500.00"), "Loan repayment to Bob");
            txService.deposit(acmeChecking, new BigDecimal("75000.00"), "Client invoice payment");
            txService.withdraw(bobSavings, new BigDecimal("2000.00"), "Rent payment");
        } catch (BankingException e) {
            System.out.println("Transaction error: " + e.getMessage());
        }

        // Attempt an invalid withdrawal to demonstrate exception handling
        try {
            txService.withdraw(aliceChecking, new BigDecimal("999999.00"), "Overdraft attempt");
        } catch (InsufficientFundsException e) {
            System.out.println("Expected error caught: " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Loans
        // ---------------------------------------------------------------
        LoanService loanService = bank.getLoanService();
        LoanAccount bobLoan = loanService.applyForLoan(bob, new BigDecimal("50000.00"), new BigDecimal("0.08"), 36);
        loanService.approveLoan(bobLoan);
        System.out.println("\n--- Loan Approved ---");
        System.out.println(bobLoan);
        loanService.makeRepayment(bobLoan, new BigDecimal("2000.00"));

        // ---------------------------------------------------------------
        // Interest calculation
        // ---------------------------------------------------------------
        InterestCalculator calculator = new InterestCalculator();
        System.out.println("\n--- Interest Projections ---");
        System.out.println("Alice Savings projected annual interest: " +
                calculator.calculateSimpleInterest(aliceSavings.getBalance(), aliceSavings.getInterestRate(), 1));
        System.out.println("Bob FD maturity value: " +
                calculator.calculateCompoundInterest(bobFd.getPrincipal(), bobFd.getInterestRate(), bobFd.getTermMonths()));

        // ---------------------------------------------------------------
        // Statements
        // ---------------------------------------------------------------
        System.out.println("\n--- Alice Savings Statement ---");
        bank.getTransactionService().printStatement(aliceSavings);

        System.out.println("\n--- Bank Summary Report ---");
        bank.printBankSummary();

        System.out.println("\n--- Audit Log (last 5 entries) ---");
        bank.getAuditService().printRecentEntries(5);

        System.out.println("\n=====================================================");
        System.out.println("   EXTENDED SERVICES DEMONSTRATION");
        System.out.println("=====================================================");
        ExtendedBankingDemo.run(bank);

        System.out.println("\n=====================================================");
        System.out.println("   SIMULATED ATM / TELLER CONSOLE SESSION");
        System.out.println("=====================================================");
        ConsoleMenuDemo.run(bank, alice, aliceChecking);

        System.out.println("\n=====================================================");
        System.out.println("   FINAL BANK-WIDE CONFIGURATION SNAPSHOT");
        System.out.println("=====================================================");
        BankConfiguration config = BankConfiguration.builder()
                .minSavingsBalance(new BigDecimal("250.00"))
                .maxDailyTransferLimit(new BigDecimal("50000.00"))
                .wireTransferFee(new BigDecimal("20.00"))
                .maxFailedLoginAttempts(5)
                .fraudDetectionEnabled(true)
                .build();
        System.out.println("Minimum Savings Balance: " + CurrencyFormatter.format(config.getMinSavingsBalance()));
        System.out.println("Max Daily Transfer Limit: " + CurrencyFormatter.format(config.getMaxDailyTransferLimit()));
        System.out.println("Wire Transfer Fee: " + CurrencyFormatter.format(config.getWireTransferFee()));
        System.out.println("Max Failed Login Attempts: " + config.getMaxFailedLoginAttempts());
        System.out.println("Fraud Detection Enabled: " + config.isFraudDetectionEnabled());

        DailyLimitTracker limitTracker = new DailyLimitTracker(config.getMaxDailyTransferLimit());
        limitTracker.recordTransfer(aliceChecking.getAccountNumber(), new BigDecimal("5000.00"));
        System.out.println("Remaining daily transfer limit for Alice's checking account: " +
                CurrencyFormatter.format(limitTracker.getRemainingLimit(aliceChecking.getAccountNumber())));

        System.out.println("\nThank you for using National Trust Bank Systems.");
    }
}

// =========================================================================================
// ENUMERATIONS
// =========================================================================================

enum AccountType {
    SAVINGS, CHECKING, FIXED_DEPOSIT, LOAN, CREDIT_CARD
}

enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST_CREDIT, FEE, LOAN_DISBURSEMENT, LOAN_REPAYMENT, CARD_PURCHASE, CARD_PAYMENT
}

enum TransactionStatus {
    PENDING, COMPLETED, FAILED, REVERSED
}

enum LoanStatus {
    APPLIED, APPROVED, REJECTED, ACTIVE, CLOSED, DEFAULTED
}

enum CustomerType {
    INDIVIDUAL, BUSINESS, JOINT
}

enum CustomerStatus {
    ACTIVE, SUSPENDED, CLOSED
}

// =========================================================================================
// EXCEPTIONS
// =========================================================================================

/**
 * Base class for all checked banking-domain exceptions.
 */
class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InsufficientFundsException extends BankingException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

class InvalidAccountException extends BankingException {
    public InvalidAccountException(String message) {
        super(message);
    }
}

class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class DuplicateAccountException extends BankingException {
    public DuplicateAccountException(String message) {
        super(message);
    }
}

class AuthenticationException extends BankingException {
    public AuthenticationException(String message) {
        super(message);
    }
}

class LoanApprovalException extends BankingException {
    public LoanApprovalException(String message) {
        super(message);
    }
}

class CreditLimitExceededException extends BankingException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}

// =========================================================================================
// UTILITY CLASSES
// =========================================================================================

/**
 * Generates unique, monotonically increasing identifiers for various banking entities.
 */
final class IdGenerator {
    private static final AtomicLong ACCOUNT_SEQUENCE = new AtomicLong(100000000L);
    private static final AtomicLong CUSTOMER_SEQUENCE = new AtomicLong(5000000L);
    private static final AtomicLong TRANSACTION_SEQUENCE = new AtomicLong(1L);
    private static final AtomicLong LOAN_SEQUENCE = new AtomicLong(90000L);

    private IdGenerator() {
        // utility class, no instantiation
    }

    public static String nextAccountNumber() {
        return "ACC" + ACCOUNT_SEQUENCE.getAndIncrement();
    }

    public static String nextCustomerId() {
        return "CUST" + CUSTOMER_SEQUENCE.getAndIncrement();
    }

    public static String nextTransactionId() {
        return "TXN" + String.format("%010d", TRANSACTION_SEQUENCE.getAndIncrement());
    }

    public static String nextLoanId() {
        return "LOAN" + LOAN_SEQUENCE.getAndIncrement();
    }
}

/**
 * Common validation routines shared across services.
 */
final class ValidationUtil {

    private ValidationUtil() {
    }

    public static void requirePositive(BigDecimal amount, String fieldName) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(fieldName + " must be a positive amount");
        }
    }

    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 7;
    }
}

/**
 * Formats currency amounts consistently for display and reporting.
 */
final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }
        return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

/**
 * Simple wrapper around common date/time formatting operations used throughout
 * the banking application.
 */
final class DateUtil {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {
    }

    public static String formatNow() {
        return LocalDateTime.now().format(DISPLAY_FORMAT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_ONLY_FORMAT);
    }
}

// =========================================================================================
// MODEL: ADDRESS
// =========================================================================================

class Address {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public Address(String street, String city, String state, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + postalCode + ", " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
                Objects.equals(city, address.city) &&
                Objects.equals(state, address.state) &&
                Objects.equals(postalCode, address.postalCode) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, country);
    }
}

// =========================================================================================
// MODEL: CUSTOMER
// =========================================================================================

class Customer {
    private final String customerId;
    private String fullName;
    private Address address;
    private String email;
    private String phoneNumber;
    private CustomerType customerType;
    private CustomerStatus status;
    private final LocalDateTime registeredOn;
    private final List<Account> accounts;
    private int creditScore;

    public Customer(String customerId, String fullName, Address address, String email,
                    String phoneNumber, CustomerType customerType) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.customerType = customerType;
        this.status = CustomerStatus.ACTIVE;
        this.registeredOn = LocalDateTime.now();
        this.accounts = new ArrayList<>();
        this.creditScore = 700;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public BigDecimal getTotalAssets() {
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accounts) {
            if (!(account instanceof LoanAccount) && !(account instanceof CreditCardAccount)) {
                total = total.add(account.getBalance());
            }
        }
        return total;
    }

    @Override
    public String toString() {
        return String.format("Customer[id=%s, name=%s, type=%s, status=%s, accounts=%d]",
                customerId, fullName, customerType, status, accounts.size());
    }
}

// =========================================================================================
// MODEL: TRANSACTION
// =========================================================================================

class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;
    private TransactionStatus status;
    private String relatedAccountNumber;

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String description) {
        this.transactionId = IdGenerator.nextTransactionId();
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.COMPLETED;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getRelatedAccountNumber() {
        return relatedAccountNumber;
    }

    public void setRelatedAccountNumber(String relatedAccountNumber) {
        this.relatedAccountNumber = relatedAccountNumber;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %-15s | %-10s | Amount: %-12s | Balance After: %-12s | %s",
                DateUtil.format(timestamp), transactionId, type, status,
                CurrencyFormatter.format(amount), CurrencyFormatter.format(balanceAfter), description);
    }
}

/**
 * Maintains the ordered history of transactions for a single account.
 */
class TransactionHistory {
    private final List<Transaction> transactions;

    public TransactionHistory() {
        this.transactions = new ArrayList<>();
    }

    public void add(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getRecent(int count) {
        int size = transactions.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(transactions.subList(fromIndex, size));
    }

    public BigDecimal sumByType(TransactionType type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == type) {
                sum = sum.add(t.getAmount());
            }
        }
        return sum;
    }

    public int size() {
        return transactions.size();
    }
}

// =========================================================================================
// MODEL: ACCOUNT HIERARCHY
// =========================================================================================

/**
 * Abstract base class representing a generic bank account. Concrete subclasses
 * implement product-specific behavior such as interest accrual, overdraft rules,
 * or credit limits.
 */
abstract class Account {
    protected final String accountNumber;
    protected final Customer owner;
    protected BigDecimal balance;
    protected final AccountType accountType;
    protected final LocalDateTime openedOn;
    protected boolean active;
    protected final TransactionHistory history;

    protected Account(Customer owner, BigDecimal initialBalance, AccountType accountType) {
        this.accountNumber = IdGenerator.nextAccountNumber();
        this.owner = owner;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.openedOn = LocalDateTime.now();
        this.active = true;
        this.history = new TransactionHistory();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public LocalDateTime getOpenedOn() {
        return openedOn;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public TransactionHistory getHistory() {
        return history;
    }

    /**
     * Applies a credit (increase) to the account balance.
     */
    protected void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * Applies a debit (decrease) to the account balance. Subclasses may override
     * to allow overdrafts or enforce credit limits.
     */
    protected void debit(BigDecimal amount) throws InsufficientFundsException {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds in account " + accountNumber + ". Balance: " +
                            CurrencyFormatter.format(balance) + ", Requested: " + CurrencyFormatter.format(amount));
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Indicates whether a withdrawal of the given amount is permitted under this
     * account's specific rules (overdraft, credit limit, etc).
     */
    public abstract boolean canWithdraw(BigDecimal amount);

    public abstract String getProductDescription();

    @Override
    public String toString() {
        return String.format("%s [%s] Owner: %s | Balance: %s | Status: %s",
                accountType, accountNumber, owner.getFullName(),
                CurrencyFormatter.format(balance), active ? "ACTIVE" : "INACTIVE");
    }
}

/**
 * A standard interest-bearing savings account with a minimum balance requirement.
 */
class SavingsAccount extends Account {
    private BigDecimal interestRate;
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("100.00");
    private int withdrawalsThisMonth;
    private static final int FREE_WITHDRAWALS_PER_MONTH = 6;
    private static final BigDecimal EXCESS_WITHDRAWAL_FEE = new BigDecimal("5.00");

    public SavingsAccount(Customer owner, BigDecimal initialBalance, BigDecimal interestRate) {
        super(owner, initialBalance, AccountType.SAVINGS);
        this.interestRate = interestRate;
        this.withdrawalsThisMonth = 0;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public int getWithdrawalsThisMonth() {
        return withdrawalsThisMonth;
    }

    public void recordWithdrawal() {
        withdrawalsThisMonth++;
    }

    public void resetMonthlyCounters() {
        withdrawalsThisMonth = 0;
    }

    public boolean isExcessWithdrawalFeeApplicable() {
        return withdrawalsThisMonth > FREE_WITHDRAWALS_PER_MONTH;
    }

    public static BigDecimal getExcessWithdrawalFee() {
        return EXCESS_WITHDRAWAL_FEE;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(MINIMUM_BALANCE) >= 0;
    }

    @Override
    public String getProductDescription() {
        return "High-Yield Savings Account with " + interestRate.multiply(new BigDecimal("100")) + "% APY";
    }
}

/**
 * A checking / current account permitting an overdraft up to a configured limit.
 */
class CheckingAccount extends Account {
    private BigDecimal overdraftLimit;
    private static final BigDecimal MONTHLY_MAINTENANCE_FEE = new BigDecimal("10.00");

    public CheckingAccount(Customer owner, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        super(owner, initialBalance, AccountType.CHECKING);
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public static BigDecimal getMonthlyMaintenanceFee() {
        return MONTHLY_MAINTENANCE_FEE;
    }

    @Override
    protected void debit(BigDecimal amount) throws InsufficientFundsException {
        BigDecimal projectedBalance = this.balance.subtract(amount);
        if (projectedBalance.compareTo(overdraftLimit.negate()) < 0) {
            throw new InsufficientFundsException(
                    "Withdrawal exceeds overdraft limit on account " + accountNumber +
                            ". Limit: " + CurrencyFormatter.format(overdraftLimit));
        }
        this.balance = projectedBalance;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(overdraftLimit.negate()) >= 0;
    }

    @Override
    public String getProductDescription() {
        return "Everyday Checking Account with overdraft protection up to " +
                CurrencyFormatter.format(overdraftLimit);
    }
}

/**
 * A time-locked fixed deposit account that accrues compound interest until maturity.
 */
class FixedDepositAccount extends Account {
    private final BigDecimal principal;
    private final int termMonths;
    private final BigDecimal interestRate;
    private final LocalDate maturityDate;
    private boolean matured;

    public FixedDepositAccount(Customer owner, BigDecimal principal, int termMonths, BigDecimal interestRate) {
        super(owner, principal, AccountType.FIXED_DEPOSIT);
        this.principal = principal;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.maturityDate = LocalDate.now().plusMonths(termMonths);
        this.matured = false;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public boolean isMatured() {
        return matured;
    }

    public void markMatured() {
        this.matured = true;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        // Fixed deposits cannot be withdrawn from before maturity except via closure.
        return matured && balance.compareTo(amount) >= 0;
    }

    @Override
    public String getProductDescription() {
        return String.format("%d-Month Fixed Deposit at %s%% maturing on %s",
                termMonths, interestRate.multiply(new BigDecimal("100")), DateUtil.formatDate(maturityDate));
    }
}

/**
 * Represents an installment loan account with amortized repayments.
 */
class LoanAccount extends Account {
    private final BigDecimal originalPrincipal;
    private BigDecimal outstandingPrincipal;
    private final BigDecimal interestRate;
    private final int termMonths;
    private LoanStatus loanStatus;
    private final List<BigDecimal> repaymentHistory;

    public LoanAccount(Customer owner, BigDecimal principal, BigDecimal interestRate, int termMonths) {
        super(owner, BigDecimal.ZERO, AccountType.LOAN);
        this.originalPrincipal = principal;
        this.outstandingPrincipal = principal;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.loanStatus = LoanStatus.APPLIED;
        this.repaymentHistory = new ArrayList<>();
        // For a loan account, "balance" represents the outstanding amount owed.
        this.balance = principal;
    }

    public BigDecimal getOriginalPrincipal() {
        return originalPrincipal;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public LoanStatus getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
    }

    public void applyRepayment(BigDecimal amount) {
        this.outstandingPrincipal = this.outstandingPrincipal.subtract(amount);
        if (this.outstandingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            this.outstandingPrincipal = BigDecimal.ZERO;
        }
        this.balance = this.outstandingPrincipal;
        repaymentHistory.add(amount);
        if (this.outstandingPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            this.loanStatus = LoanStatus.CLOSED;
        }
    }

    public BigDecimal getMonthlyInstallment() {
        // Simple amortization formula: A = P * r(1+r)^n / ((1+r)^n - 1)
        BigDecimal monthlyRate = interestRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return originalPrincipal.divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
        }
        double r = monthlyRate.doubleValue();
        double n = termMonths;
        double p = originalPrincipal.doubleValue();
        double numerator = p * r * Math.pow(1 + r, n);
        double denominator = Math.pow(1 + r, n) - 1;
        double installment = numerator / denominator;
        return new BigDecimal(installment).setScale(2, RoundingMode.HALF_UP);
    }

    public List<BigDecimal> getRepaymentHistory() {
        return Collections.unmodifiableList(repaymentHistory);
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return false; // Loan accounts do not support direct withdrawals
    }

    @Override
    public String getProductDescription() {
        return String.format("Installment Loan of %s at %s%% over %d months (status: %s)",
                CurrencyFormatter.format(originalPrincipal), interestRate.multiply(new BigDecimal("100")),
                termMonths, loanStatus);
    }

    @Override
    public String toString() {
        return String.format("LoanAccount[%s] Owner: %s | Outstanding: %s | Status: %s | Installment: %s/mo",
                accountNumber, owner.getFullName(), CurrencyFormatter.format(outstandingPrincipal),
                loanStatus, CurrencyFormatter.format(getMonthlyInstallment()));
    }
}

/**
 * Represents a revolving credit card account with a credit limit and available balance.
 */
class CreditCardAccount extends Account {
    private final BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private final BigDecimal annualPercentageRate;
    private static final BigDecimal MINIMUM_PAYMENT_PERCENT = new BigDecimal("0.02");

    public CreditCardAccount(Customer owner, BigDecimal creditLimit, BigDecimal annualPercentageRate) {
        super(owner, BigDecimal.ZERO, AccountType.CREDIT_CARD);
        this.creditLimit = creditLimit;
        this.outstandingBalance = BigDecimal.ZERO;
        this.annualPercentageRate = annualPercentageRate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public BigDecimal getAvailableCredit() {
        return creditLimit.subtract(outstandingBalance);
    }

    public BigDecimal getAnnualPercentageRate() {
        return annualPercentageRate;
    }

    public BigDecimal getMinimumPaymentDue() {
        return outstandingBalance.multiply(MINIMUM_PAYMENT_PERCENT).setScale(2, RoundingMode.HALF_UP);
    }

    public void charge(BigDecimal amount) throws CreditLimitExceededException {
        BigDecimal projected = outstandingBalance.add(amount);
        if (projected.compareTo(creditLimit) > 0) {
            throw new CreditLimitExceededException(
                    "Charge of " + CurrencyFormatter.format(amount) + " exceeds available credit on card " + accountNumber);
        }
        outstandingBalance = projected;
        this.balance = outstandingBalance;
    }

    public void makePayment(BigDecimal amount) {
        outstandingBalance = outstandingBalance.subtract(amount);
        if (outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
            outstandingBalance = BigDecimal.ZERO;
        }
        this.balance = outstandingBalance;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return getAvailableCredit().compareTo(amount) >= 0;
    }

    @Override
    public String getProductDescription() {
        return String.format("Credit Card with limit %s at %s%% APR",
                CurrencyFormatter.format(creditLimit), annualPercentageRate.multiply(new BigDecimal("100")));
    }

    @Override
    public String toString() {
        return String.format("CreditCardAccount[%s] Owner: %s | Outstanding: %s | Available: %s",
                accountNumber, owner.getFullName(), CurrencyFormatter.format(outstandingBalance),
                CurrencyFormatter.format(getAvailableCredit()));
    }
}

// =========================================================================================
// AUDIT LOGGING
// =========================================================================================

class AuditEntry {
    private final LocalDateTime timestamp;
    private final String actor;
    private final String action;
    private final String details;

    public AuditEntry(String actor, String action, String details) {
        this.timestamp = LocalDateTime.now();
        this.actor = actor;
        this.action = action;
        this.details = details;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s performed %s -- %s", DateUtil.format(timestamp), actor, action, details);
    }
}

/**
 * Central audit trail service. In production this would persist to durable,
 * append-only storage; here we retain entries in memory for demonstration.
 */
class AuditService {
    private final List<AuditEntry> entries;

    public AuditService() {
        this.entries = new ArrayList<>();
    }

    public void log(String actor, String action, String details) {
        entries.add(new AuditEntry(actor, action, details));
    }

    public List<AuditEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void printRecentEntries(int count) {
        int fromIndex = Math.max(0, entries.size() - count);
        for (AuditEntry entry : entries.subList(fromIndex, entries.size())) {
            System.out.println(entry);
        }
    }
}

// =========================================================================================
// NOTIFICATION SERVICE
// =========================================================================================

/**
 * Simulates sending notifications (email/SMS) to customers about account activity.
 * In a real system this would integrate with an external messaging gateway.
 */
class NotificationService {
    private final List<String> sentNotifications;

    public NotificationService() {
        this.sentNotifications = new ArrayList<>();
    }

    public void notifyCustomer(Customer customer, String subject, String message) {
        String record = String.format("To: %s <%s> | Subject: %s | Message: %s",
                customer.getFullName(), customer.getEmail(), subject, message);
        sentNotifications.add(record);
        // In production this would call an email/SMS gateway API.
    }

    public List<String> getSentNotifications() {
        return Collections.unmodifiableList(sentNotifications);
    }
}

// =========================================================================================
// INTEREST CALCULATOR
// =========================================================================================

/**
 * Provides simple and compound interest calculations used across savings,
 * fixed deposit, and loan products.
 */
class InterestCalculator {

    public BigDecimal calculateSimpleInterest(BigDecimal principal, BigDecimal annualRate, int years) {
        return principal.multiply(annualRate).multiply(new BigDecimal(years)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateCompoundInterest(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        double p = principal.doubleValue();
        double r = annualRate.doubleValue();
        double n = 12; // compounded monthly
        double t = termMonths / 12.0;
        double amount = p * Math.pow(1 + (r / n), n * t);
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMonthlyAccrual(BigDecimal balance, BigDecimal annualRate) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        return balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }
}

// =========================================================================================
// SERVICES
// =========================================================================================

/**
 * Manages customer lifecycle: registration, lookup, and status changes.
 */
class CustomerService {
    private final Map<String, Customer> customersById;
    private final AuditService auditService;

    public CustomerService(AuditService auditService) {
        this.customersById = new LinkedHashMap<>();
        this.auditService = auditService;
    }

    public Customer registerCustomer(String fullName, Address address, String email,
                                     String phoneNumber, CustomerType type) {
        ValidationUtil.requireNonNull(fullName, "fullName");
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
        String customerId = IdGenerator.nextCustomerId();
        Customer customer = new Customer(customerId, fullName, address, email, phoneNumber, type);
        customersById.put(customerId, customer);
        auditService.log("CustomerService", "REGISTER_CUSTOMER", "Registered " + customer);
        return customer;
    }

    public Customer findById(String customerId) throws AccountNotFoundException {
        Customer customer = customersById.get(customerId);
        if (customer == null) {
            throw new AccountNotFoundException("No customer found with ID: " + customerId);
        }
        return customer;
    }

    public Collection<Customer> getAllCustomers() {
        return Collections.unmodifiableCollection(customersById.values());
    }

    public void suspendCustomer(Customer customer) {
        customer.setStatus(CustomerStatus.SUSPENDED);
        auditService.log("CustomerService", "SUSPEND_CUSTOMER", "Suspended " + customer.getCustomerId());
    }

    public void reactivateCustomer(Customer customer) {
        customer.setStatus(CustomerStatus.ACTIVE);
        auditService.log("CustomerService", "REACTIVATE_CUSTOMER", "Reactivated " + customer.getCustomerId());
    }
}

/**
 * Manages account creation and lookup across all product types.
 */
class AccountService {
    private final Map<String, Account> accountsByNumber;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AccountService(AuditService auditService, NotificationService notificationService) {
        this.accountsByNumber = new LinkedHashMap<>();
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public SavingsAccount openSavingsAccount(Customer owner, BigDecimal initialBalance, BigDecimal interestRate) {
        SavingsAccount account = new SavingsAccount(owner, initialBalance, interestRate);
        registerAccount(owner, account);
        return account;
    }

    public CheckingAccount openCheckingAccount(Customer owner, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        CheckingAccount account = new CheckingAccount(owner, initialBalance, overdraftLimit);
        registerAccount(owner, account);
        return account;
    }

    public FixedDepositAccount openFixedDepositAccount(Customer owner, BigDecimal principal, int termMonths, BigDecimal interestRate) {
        FixedDepositAccount account = new FixedDepositAccount(owner, principal, termMonths, interestRate);
        registerAccount(owner, account);
        return account;
    }

    public CreditCardAccount openCreditCardAccount(Customer owner, BigDecimal creditLimit, BigDecimal apr) {
        CreditCardAccount account = new CreditCardAccount(owner, creditLimit, apr);
        registerAccount(owner, account);
        return account;
    }

    public LoanAccount createLoanAccount(Customer owner, BigDecimal principal, BigDecimal rate, int termMonths) {
        LoanAccount account = new LoanAccount(owner, principal, rate, termMonths);
        registerAccount(owner, account);
        return account;
    }

    private void registerAccount(Customer owner, Account account) {
        accountsByNumber.put(account.getAccountNumber(), account);
        owner.addAccount(account);
        auditService.log("AccountService", "OPEN_ACCOUNT",
                "Opened " + account.getAccountType() + " account " + account.getAccountNumber() +
                        " for customer " + owner.getCustomerId());
        notificationService.notifyCustomer(owner, "Account Opened",
                "Your new " + account.getAccountType() + " account " + account.getAccountNumber() + " is now active.");
    }

    public Account findByAccountNumber(String accountNumber) throws AccountNotFoundException {
        Account account = accountsByNumber.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("No account found with number: " + accountNumber);
        }
        return account;
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accountsByNumber.values());
    }

    public BigDecimal getTotalDeposits() {
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accountsByNumber.values()) {
            if (account instanceof SavingsAccount || account instanceof CheckingAccount || account instanceof FixedDepositAccount) {
                total = total.add(account.getBalance());
            }
        }
        return total;
    }

    public BigDecimal getTotalOutstandingLoans() {
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accountsByNumber.values()) {
            if (account instanceof LoanAccount) {
                total = total.add(((LoanAccount) account).getOutstandingPrincipal());
            }
        }
        return total;
    }
}

/**
 * Handles the execution of monetary transactions: deposits, withdrawals, and
 * transfers between accounts, with full audit logging and statement printing.
 */
class TransactionService {
    private final AuditService auditService;
    private final NotificationService notificationService;

    public TransactionService(AuditService auditService, NotificationService notificationService) {
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public Transaction deposit(Account account, BigDecimal amount, String description) throws InvalidAmountException {
        ValidationUtil.requirePositive(amount, "deposit amount");
        account.credit(amount);
        Transaction txn = new Transaction(account.getAccountNumber(), TransactionType.DEPOSIT, amount, account.getBalance(), description);
        account.getHistory().add(txn);
        auditService.log("TransactionService", "DEPOSIT", txn.toString());
        notificationService.notifyCustomer(account.getOwner(), "Deposit Received",
                "A deposit of " + CurrencyFormatter.format(amount) + " was credited to account " + account.getAccountNumber());
        return txn;
    }

    public Transaction withdraw(Account account, BigDecimal amount, String description)
            throws InvalidAmountException, InsufficientFundsException {
        ValidationUtil.requirePositive(amount, "withdrawal amount");
        if (!account.canWithdraw(amount)) {
            throw new InsufficientFundsException(
                    "Withdrawal of " + CurrencyFormatter.format(amount) + " not permitted on account " + account.getAccountNumber());
        }
        account.debit(amount);
        if (account instanceof SavingsAccount) {
            ((SavingsAccount) account).recordWithdrawal();
        }
        Transaction txn = new Transaction(account.getAccountNumber(), TransactionType.WITHDRAWAL, amount, account.getBalance(), description);
        account.getHistory().add(txn);
        auditService.log("TransactionService", "WITHDRAWAL", txn.toString());
        notificationService.notifyCustomer(account.getOwner(), "Withdrawal Processed",
                "A withdrawal of " + CurrencyFormatter.format(amount) + " was made from account " + account.getAccountNumber());
        return txn;
    }

    public void transfer(Account from, Account to, BigDecimal amount, String description)
            throws InvalidAmountException, InsufficientFundsException {
        ValidationUtil.requirePositive(amount, "transfer amount");
        if (!from.canWithdraw(amount)) {
            throw new InsufficientFundsException(
                    "Transfer of " + CurrencyFormatter.format(amount) + " not permitted from account " + from.getAccountNumber());
        }
        from.debit(amount);
        to.credit(amount);

        Transaction outTxn = new Transaction(from.getAccountNumber(), TransactionType.TRANSFER_OUT, amount, from.getBalance(), description);
        outTxn.setRelatedAccountNumber(to.getAccountNumber());
        from.getHistory().add(outTxn);

        Transaction inTxn = new Transaction(to.getAccountNumber(), TransactionType.TRANSFER_IN, amount, to.getBalance(), description);
        inTxn.setRelatedAccountNumber(from.getAccountNumber());
        to.getHistory().add(inTxn);

        auditService.log("TransactionService", "TRANSFER",
                "Transferred " + CurrencyFormatter.format(amount) + " from " + from.getAccountNumber() + " to " + to.getAccountNumber());

        notificationService.notifyCustomer(from.getOwner(), "Transfer Sent",
                "You sent " + CurrencyFormatter.format(amount) + " to account " + to.getAccountNumber());
        notificationService.notifyCustomer(to.getOwner(), "Transfer Received",
                "You received " + CurrencyFormatter.format(amount) + " from account " + from.getAccountNumber());
    }

    public void applyMonthlyInterest(SavingsAccount account, InterestCalculator calculator) {
        BigDecimal interest = calculator.calculateMonthlyAccrual(account.getBalance(), account.getInterestRate());
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.credit(interest);
            Transaction txn = new Transaction(account.getAccountNumber(), TransactionType.INTEREST_CREDIT, interest, account.getBalance(), "Monthly interest credit");
            account.getHistory().add(txn);
            auditService.log("TransactionService", "INTEREST_CREDIT", txn.toString());
        }
    }

    public void printStatement(Account account) {
        System.out.println("Statement for account: " + account.getAccountNumber() + " (" + account.getOwner().getFullName() + ")");
        System.out.println("Product: " + account.getProductDescription());
        System.out.println("Current Balance: " + CurrencyFormatter.format(account.getBalance()));
        System.out.println("-------------------------------------------------------------------------------------");
        for (Transaction txn : account.getHistory().getAll()) {
            System.out.println(txn);
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }
}

/**
 * Manages the full lifecycle of loans: application, approval, disbursement, and repayment.
 */
class LoanService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final List<LoanAccount> loanApplications;
    private static final int MINIMUM_CREDIT_SCORE_FOR_APPROVAL = 600;

    public LoanService(AccountService accountService, TransactionService transactionService,
                       AuditService auditService, NotificationService notificationService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.loanApplications = new ArrayList<>();
    }

    public LoanAccount applyForLoan(Customer customer, BigDecimal principal, BigDecimal rate, int termMonths) {
        LoanAccount loan = accountService.createLoanAccount(customer, principal, rate, termMonths);
        loanApplications.add(loan);
        auditService.log("LoanService", "LOAN_APPLICATION",
                "Customer " + customer.getCustomerId() + " applied for loan " + loan.getAccountNumber() +
                        " of " + CurrencyFormatter.format(principal));
        return loan;
    }

    public void approveLoan(LoanAccount loan) {
        Customer customer = loan.getOwner();
        if (customer.getCreditScore() < MINIMUM_CREDIT_SCORE_FOR_APPROVAL) {
            loan.setLoanStatus(LoanStatus.REJECTED);
            auditService.log("LoanService", "LOAN_REJECTED", "Loan " + loan.getAccountNumber() + " rejected due to low credit score");
            return;
        }
        loan.setLoanStatus(LoanStatus.ACTIVE);
        auditService.log("LoanService", "LOAN_APPROVED", "Loan " + loan.getAccountNumber() + " approved and activated");
        notificationService.notifyCustomer(customer, "Loan Approved",
                "Your loan " + loan.getAccountNumber() + " of " + CurrencyFormatter.format(loan.getOriginalPrincipal()) + " has been approved.");
    }

    public void rejectLoan(LoanAccount loan, String reason) {
        loan.setLoanStatus(LoanStatus.REJECTED);
        auditService.log("LoanService", "LOAN_REJECTED", "Loan " + loan.getAccountNumber() + " rejected: " + reason);
    }

    public void makeRepayment(LoanAccount loan, BigDecimal amount) {
        loan.applyRepayment(amount);
        Transaction txn = new Transaction(loan.getAccountNumber(), TransactionType.LOAN_REPAYMENT, amount, loan.getOutstandingPrincipal(), "Loan repayment");
        loan.getHistory().add(txn);
        auditService.log("LoanService", "LOAN_REPAYMENT",
                "Repayment of " + CurrencyFormatter.format(amount) + " on loan " + loan.getAccountNumber() +
                        ". Remaining: " + CurrencyFormatter.format(loan.getOutstandingPrincipal()));
        if (loan.getLoanStatus() == LoanStatus.CLOSED) {
            notificationService.notifyCustomer(loan.getOwner(), "Loan Fully Repaid",
                    "Congratulations! Loan " + loan.getAccountNumber() + " has been fully repaid.");
        }
    }

    public List<LoanAccount> getAllApplications() {
        return Collections.unmodifiableList(loanApplications);
    }
}

// =========================================================================================
// BANK - Central Aggregator
// =========================================================================================

/**
 * The Bank class acts as the central facade tying together all services:
 * customer management, account management, transactions, loans, auditing,
 * and notifications. Client code typically interacts only with this class
 * and the domain objects it returns.
 */
class Bank {
    private final String bankName;
    private final String bankCode;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public Bank(String bankName, String bankCode) {
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.auditService = new AuditService();
        this.notificationService = new NotificationService();
        this.customerService = new CustomerService(auditService);
        this.accountService = new AccountService(auditService, notificationService);
        this.transactionService = new TransactionService(auditService, notificationService);
        this.loanService = new LoanService(accountService, transactionService, auditService, notificationService);
        auditService.log("Bank", "BANK_INITIALIZED", "Bank " + bankName + " (" + bankCode + ") initialized");
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public LoanService getLoanService() {
        return loanService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void printBankSummary() {
        System.out.println("Bank: " + bankName + " (" + bankCode + ")");
        System.out.println("Total Customers: " + customerService.getAllCustomers().size());
        System.out.println("Total Accounts: " + accountService.getAllAccounts().size());
        System.out.println("Total Deposits: " + CurrencyFormatter.format(accountService.getTotalDeposits()));
        System.out.println("Total Outstanding Loans: " + CurrencyFormatter.format(accountService.getTotalOutstandingLoans()));

        Map<AccountType, Integer> countByType = new EnumMap<>(AccountType.class);
        for (Account account : accountService.getAllAccounts()) {
            countByType.merge(account.getAccountType(), 1, Integer::sum);
        }
        System.out.println("Account Breakdown:");
        for (Map.Entry<AccountType, Integer> entry : countByType.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public void runMonthEndProcessing(InterestCalculator calculator) {
        for (Account account : accountService.getAllAccounts()) {
            if (account instanceof SavingsAccount) {
                SavingsAccount savings = (SavingsAccount) account;
                transactionService.applyMonthlyInterest(savings, calculator);
                if (savings.isExcessWithdrawalFeeApplicable()) {
                    try {
                        transactionService.withdraw(savings, SavingsAccount.getExcessWithdrawalFee(), "Excess withdrawal fee");
                    } catch (BankingException e) {
                        auditService.log("Bank", "FEE_APPLICATION_FAILED", e.getMessage());
                    }
                }
                savings.resetMonthlyCounters();
            } else if (account instanceof CheckingAccount) {
                try {
                    transactionService.withdraw(account, CheckingAccount.getMonthlyMaintenanceFee(), "Monthly maintenance fee");
                } catch (BankingException e) {
                    auditService.log("Bank", "FEE_APPLICATION_FAILED", e.getMessage());
                }
            }
        }
        auditService.log("Bank", "MONTH_END_PROCESSING", "Completed month-end processing for all accounts");
    }
}

// =========================================================================================
// ADDITIONAL ACCOUNT PRODUCTS
// =========================================================================================

/**
 * A tiered-interest money market account offering better rates for larger balances,
 * with limited monthly transaction allowances similar to a savings account.
 */
class MoneyMarketAccount extends Account {
    private static final BigDecimal TIER_1_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal TIER_2_THRESHOLD = new BigDecimal("50000.00");
    private static final BigDecimal TIER_1_RATE = new BigDecimal("0.015");
    private static final BigDecimal TIER_2_RATE = new BigDecimal("0.025");
    private static final BigDecimal TIER_3_RATE = new BigDecimal("0.035");
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("2500.00");
    private int transactionsThisMonth;
    private static final int FREE_TRANSACTIONS_PER_MONTH = 3;

    public MoneyMarketAccount(Customer owner, BigDecimal initialBalance) {
        super(owner, initialBalance, AccountType.SAVINGS);
        this.transactionsThisMonth = 0;
    }

    public BigDecimal getCurrentRate() {
        if (balance.compareTo(TIER_2_THRESHOLD) >= 0) {
            return TIER_3_RATE;
        } else if (balance.compareTo(TIER_1_THRESHOLD) >= 0) {
            return TIER_2_RATE;
        }
        return TIER_1_RATE;
    }

    public void recordTransaction() {
        transactionsThisMonth++;
    }

    public void resetMonthlyCounters() {
        transactionsThisMonth = 0;
    }

    public boolean isOverTransactionLimit() {
        return transactionsThisMonth > FREE_TRANSACTIONS_PER_MONTH;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(MINIMUM_BALANCE) >= 0;
    }

    @Override
    public String getProductDescription() {
        return "Money Market Account, current tier rate: " + getCurrentRate().multiply(new BigDecimal("100")) + "%";
    }
}

/**
 * A no-fee, no-minimum-balance account designed for students, with a reduced
 * overdraft allowance and no monthly maintenance charges.
 */
class StudentAccount extends Account {
    private static final BigDecimal OVERDRAFT_ALLOWANCE = new BigDecimal("50.00");
    private final int graduationYear;

    public StudentAccount(Customer owner, BigDecimal initialBalance, int graduationYear) {
        super(owner, initialBalance, AccountType.CHECKING);
        this.graduationYear = graduationYear;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public boolean isEligibleForUpgrade() {
        return LocalDate.now().getYear() >= graduationYear;
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(OVERDRAFT_ALLOWANCE.negate()) >= 0;
    }

    @Override
    public String getProductDescription() {
        return "Student Checking Account (no fees), expected graduation " + graduationYear;
    }
}

// =========================================================================================
// BENEFICIARIES AND STANDING INSTRUCTIONS
// =========================================================================================

/**
 * Represents a saved payee/beneficiary for quick future transfers.
 */
class Beneficiary {
    private final String beneficiaryId;
    private final String nickname;
    private final String accountNumber;
    private final String bankCode;
    private final LocalDateTime addedOn;
    private boolean verified;

    public Beneficiary(String beneficiaryId, String nickname, String accountNumber, String bankCode) {
        this.beneficiaryId = beneficiaryId;
        this.nickname = nickname;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.addedOn = LocalDateTime.now();
        this.verified = false;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public boolean isVerified() {
        return verified;
    }

    public void markVerified() {
        this.verified = true;
    }

    @Override
    public String toString() {
        return String.format("Beneficiary[%s] %s -> %s @ %s (%s)",
                beneficiaryId, nickname, accountNumber, bankCode, verified ? "verified" : "unverified");
    }
}

/**
 * Manages the list of beneficiaries associated with each customer.
 */
class BeneficiaryService {
    private final Map<String, List<Beneficiary>> beneficiariesByCustomer;
    private final AtomicLong beneficiarySequence;
    private final AuditService auditService;

    public BeneficiaryService(AuditService auditService) {
        this.beneficiariesByCustomer = new HashMap<>();
        this.beneficiarySequence = new AtomicLong(1);
        this.auditService = auditService;
    }

    public Beneficiary addBeneficiary(Customer customer, String nickname, String accountNumber, String bankCode) {
        String id = "BEN" + beneficiarySequence.getAndIncrement();
        Beneficiary beneficiary = new Beneficiary(id, nickname, accountNumber, bankCode);
        beneficiariesByCustomer.computeIfAbsent(customer.getCustomerId(), k -> new ArrayList<>()).add(beneficiary);
        auditService.log("BeneficiaryService", "ADD_BENEFICIARY",
                "Customer " + customer.getCustomerId() + " added beneficiary " + nickname);
        return beneficiary;
    }

    public List<Beneficiary> getBeneficiaries(Customer customer) {
        return Collections.unmodifiableList(
                beneficiariesByCustomer.getOrDefault(customer.getCustomerId(), Collections.emptyList()));
    }

    public void removeBeneficiary(Customer customer, String beneficiaryId) {
        List<Beneficiary> list = beneficiariesByCustomer.get(customer.getCustomerId());
        if (list != null) {
            list.removeIf(b -> b.getBeneficiaryId().equals(beneficiaryId));
            auditService.log("BeneficiaryService", "REMOVE_BENEFICIARY",
                    "Customer " + customer.getCustomerId() + " removed beneficiary " + beneficiaryId);
        }
    }
}

enum StandingInstructionFrequency {
    WEEKLY, MONTHLY, QUARTERLY, ANNUALLY
}

/**
 * A recurring, scheduled payment instruction from a source account to a destination
 * account (e.g., rent, subscription, loan installment).
 */
class StandingInstruction {
    private final String instructionId;
    private final Account sourceAccount;
    private final Account destinationAccount;
    private final BigDecimal amount;
    private final StandingInstructionFrequency frequency;
    private LocalDate nextExecutionDate;
    private boolean active;
    private final String description;
    private int executionCount;

    public StandingInstruction(String instructionId, Account sourceAccount, Account destinationAccount,
                               BigDecimal amount, StandingInstructionFrequency frequency,
                               LocalDate startDate, String description) {
        this.instructionId = instructionId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.frequency = frequency;
        this.nextExecutionDate = startDate;
        this.active = true;
        this.description = description;
        this.executionCount = 0;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public StandingInstructionFrequency getFrequency() {
        return frequency;
    }

    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }

    public boolean isActive() {
        return active;
    }

    public void cancel() {
        this.active = false;
    }

    public String getDescription() {
        return description;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void advanceNextExecutionDate() {
        executionCount++;
        switch (frequency) {
            case WEEKLY:
                nextExecutionDate = nextExecutionDate.plusWeeks(1);
                break;
            case MONTHLY:
                nextExecutionDate = nextExecutionDate.plusMonths(1);
                break;
            case QUARTERLY:
                nextExecutionDate = nextExecutionDate.plusMonths(3);
                break;
            case ANNUALLY:
                nextExecutionDate = nextExecutionDate.plusYears(1);
                break;
        }
    }

    @Override
    public String toString() {
        return String.format("StandingInstruction[%s] %s -> %s | Amount: %s | Frequency: %s | Next: %s | %s",
                instructionId, sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(),
                CurrencyFormatter.format(amount), frequency, DateUtil.formatDate(nextExecutionDate),
                active ? "ACTIVE" : "CANCELLED");
    }
}

/**
 * Manages the creation, execution, and cancellation of standing instructions
 * (recurring transfers) across the bank.
 */
class StandingInstructionService {
    private final List<StandingInstruction> instructions;
    private final AtomicLong instructionSequence;
    private final TransactionService transactionService;
    private final AuditService auditService;

    public StandingInstructionService(TransactionService transactionService, AuditService auditService) {
        this.instructions = new ArrayList<>();
        this.instructionSequence = new AtomicLong(1);
        this.transactionService = transactionService;
        this.auditService = auditService;
    }

    public StandingInstruction createInstruction(Account source, Account destination, BigDecimal amount,
                                                 StandingInstructionFrequency frequency, LocalDate startDate,
                                                 String description) {
        String id = "SI" + instructionSequence.getAndIncrement();
        StandingInstruction instruction = new StandingInstruction(id, source, destination, amount, frequency, startDate, description);
        instructions.add(instruction);
        auditService.log("StandingInstructionService", "CREATE_INSTRUCTION", instruction.toString());
        return instruction;
    }

    public void cancelInstruction(String instructionId) {
        for (StandingInstruction instruction : instructions) {
            if (instruction.getInstructionId().equals(instructionId)) {
                instruction.cancel();
                auditService.log("StandingInstructionService", "CANCEL_INSTRUCTION", "Cancelled " + instructionId);
                return;
            }
        }
    }

    public void executeDueInstructions(LocalDate asOfDate) {
        for (StandingInstruction instruction : instructions) {
            if (instruction.isActive() && !instruction.getNextExecutionDate().isAfter(asOfDate)) {
                try {
                    transactionService.transfer(instruction.getSourceAccount(), instruction.getDestinationAccount(),
                            instruction.getAmount(), "Standing instruction: " + instruction.getDescription());
                    instruction.advanceNextExecutionDate();
                    auditService.log("StandingInstructionService", "EXECUTE_INSTRUCTION",
                            "Executed " + instruction.getInstructionId());
                } catch (BankingException e) {
                    auditService.log("StandingInstructionService", "EXECUTION_FAILED",
                            instruction.getInstructionId() + ": " + e.getMessage());
                }
            }
        }
    }

    public List<StandingInstruction> getAllInstructions() {
        return Collections.unmodifiableList(instructions);
    }
}

// =========================================================================================
// CURRENCY EXCHANGE
// =========================================================================================

/**
 * Represents a currency pair and its current exchange rate.
 */
class ExchangeRate {
    private final String baseCurrency;
    private final String quoteCurrency;
    private BigDecimal rate;
    private LocalDateTime lastUpdated;

    public ExchangeRate(String baseCurrency, String quoteCurrency, BigDecimal rate) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.rate = rate;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void updateRate(BigDecimal newRate) {
        this.rate = newRate;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getPairKey() {
        return baseCurrency + "/" + quoteCurrency;
    }

    @Override
    public String toString() {
        return getPairKey() + " = " + rate;
    }
}

/**
 * Provides currency conversion services using a maintained table of exchange rates.
 */
class CurrencyExchangeService {
    private final Map<String, ExchangeRate> rates;
    private final AuditService auditService;

    public CurrencyExchangeService(AuditService auditService) {
        this.rates = new HashMap<>();
        this.auditService = auditService;
        seedDefaultRates();
    }

    private void seedDefaultRates() {
        addOrUpdateRate("USD", "EUR", new BigDecimal("0.92"));
        addOrUpdateRate("USD", "GBP", new BigDecimal("0.79"));
        addOrUpdateRate("USD", "AUD", new BigDecimal("1.52"));
        addOrUpdateRate("USD", "INR", new BigDecimal("83.10"));
        addOrUpdateRate("USD", "JPY", new BigDecimal("149.50"));
    }

    public void addOrUpdateRate(String base, String quote, BigDecimal rate) {
        String key = base + "/" + quote;
        ExchangeRate existing = rates.get(key);
        if (existing != null) {
            existing.updateRate(rate);
        } else {
            rates.put(key, new ExchangeRate(base, quote, rate));
        }
        auditService.log("CurrencyExchangeService", "RATE_UPDATE", key + " = " + rate);
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) throws InvalidAmountException {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        String directKey = fromCurrency + "/" + toCurrency;
        if (rates.containsKey(directKey)) {
            return amount.multiply(rates.get(directKey).getRate()).setScale(2, RoundingMode.HALF_UP);
        }
        String inverseKey = toCurrency + "/" + fromCurrency;
        if (rates.containsKey(inverseKey)) {
            BigDecimal inverseRate = BigDecimal.ONE.divide(rates.get(inverseKey).getRate(), 10, RoundingMode.HALF_UP);
            return amount.multiply(inverseRate).setScale(2, RoundingMode.HALF_UP);
        }
        throw new InvalidAmountException("No exchange rate available for " + fromCurrency + " to " + toCurrency);
    }

    public Collection<ExchangeRate> getAllRates() {
        return Collections.unmodifiableCollection(rates.values());
    }
}

// =========================================================================================
// SECURITY SERVICE
// =========================================================================================

/**
 * Represents login credentials associated with a customer, including a simple
 * hashed PIN/password and failed-attempt tracking used for lockout enforcement.
 */
class SecurityCredential {
    private final String customerId;
    private int hashedPin;
    private int failedAttempts;
    private boolean locked;
    private LocalDateTime lastLoginAt;

    public SecurityCredential(String customerId, String pin) {
        this.customerId = customerId;
        this.hashedPin = simpleHash(pin);
        this.failedAttempts = 0;
        this.locked = false;
    }

    private int simpleHash(String input) {
        // NOTE: A simplistic, non-cryptographic hash used purely for demonstration.
        // Production systems must use a vetted password-hashing algorithm (e.g. bcrypt, Argon2).
        int hash = 17;
        for (char c : input.toCharArray()) {
            hash = hash * 31 + c;
        }
        return hash;
    }

    public boolean verifyPin(String pin) {
        return simpleHash(pin) == hashedPin;
    }

    public void changePin(String newPin) {
        this.hashedPin = simpleHash(newPin);
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void recordFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= 3) {
            locked = true;
        }
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
        failedAttempts = 0;
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        resetFailedAttempts();
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}

/**
 * Manages customer authentication, PIN verification, and account lockout policy.
 */
class SecurityService {
    private final Map<String, SecurityCredential> credentials;
    private final AuditService auditService;

    public SecurityService(AuditService auditService) {
        this.credentials = new HashMap<>();
        this.auditService = auditService;
    }

    public void enrollCustomer(Customer customer, String pin) {
        credentials.put(customer.getCustomerId(), new SecurityCredential(customer.getCustomerId(), pin));
        auditService.log("SecurityService", "ENROLL_CUSTOMER", "Enrolled security credentials for " + customer.getCustomerId());
    }

    public boolean authenticate(Customer customer, String pin) throws AuthenticationException {
        SecurityCredential credential = credentials.get(customer.getCustomerId());
        if (credential == null) {
            throw new AuthenticationException("No credentials enrolled for customer " + customer.getCustomerId());
        }
        if (credential.isLocked()) {
            throw new AuthenticationException("Account is locked due to repeated failed login attempts");
        }
        if (credential.verifyPin(pin)) {
            credential.recordSuccessfulLogin();
            auditService.log("SecurityService", "LOGIN_SUCCESS", customer.getCustomerId() + " logged in successfully");
            return true;
        } else {
            credential.recordFailedAttempt();
            auditService.log("SecurityService", "LOGIN_FAILED", customer.getCustomerId() + " failed login attempt " + credential.getFailedAttempts());
            if (credential.isLocked()) {
                throw new AuthenticationException("Account locked after too many failed attempts");
            }
            return false;
        }
    }

    public void unlockAccount(Customer customer) {
        SecurityCredential credential = credentials.get(customer.getCustomerId());
        if (credential != null) {
            credential.unlock();
            auditService.log("SecurityService", "UNLOCK_ACCOUNT", "Unlocked account for " + customer.getCustomerId());
        }
    }

    public void changePin(Customer customer, String oldPin, String newPin) throws AuthenticationException {
        SecurityCredential credential = credentials.get(customer.getCustomerId());
        if (credential == null || !credential.verifyPin(oldPin)) {
            throw new AuthenticationException("Old PIN verification failed for customer " + customer.getCustomerId());
        }
        credential.changePin(newPin);
        auditService.log("SecurityService", "PIN_CHANGED", "PIN changed for customer " + customer.getCustomerId());
    }
}

// =========================================================================================
// REPORTING
// =========================================================================================

/**
 * A single line item in a generated financial report.
 */
class ReportLine {
    private final String label;
    private final BigDecimal value;

    public ReportLine(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("  %-40s %s", label, CurrencyFormatter.format(value));
    }
}

/**
 * A generic container for a titled report composed of multiple report lines,
 * with an optional grand total.
 */
class Report {
    private final String title;
    private final LocalDateTime generatedAt;
    private final List<ReportLine> lines;

    public Report(String title) {
        this.title = title;
        this.generatedAt = LocalDateTime.now();
        this.lines = new ArrayList<>();
    }

    public void addLine(String label, BigDecimal value) {
        lines.add(new ReportLine(label, value));
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReportLine line : lines) {
            total = total.add(line.getValue());
        }
        return total;
    }

    public void print() {
        System.out.println("=== " + title + " ===");
        System.out.println("Generated: " + DateUtil.format(generatedAt));
        for (ReportLine line : lines) {
            System.out.println(line);
        }
        System.out.println("  " + "-".repeat(50));
        System.out.println(String.format("  %-40s %s", "TOTAL", CurrencyFormatter.format(getTotal())));
    }
}

/**
 * Generates various analytical and regulatory reports based on the current
 * state of accounts, customers, and transactions in the bank.
 */
class ReportService {
    private final AccountService accountService;
    private final CustomerService customerService;

    public ReportService(AccountService accountService, CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    public Report generateDepositsByProductReport() {
        Report report = new Report("Deposits by Product Type");
        Map<AccountType, BigDecimal> totals = new EnumMap<>(AccountType.class);
        for (Account account : accountService.getAllAccounts()) {
            totals.merge(account.getAccountType(), account.getBalance(), BigDecimal::add);
        }
        for (Map.Entry<AccountType, BigDecimal> entry : totals.entrySet()) {
            report.addLine(entry.getKey().toString(), entry.getValue());
        }
        return report;
    }

    public Report generateTopCustomersByAssetsReport(int topN) {
        Report report = new Report("Top " + topN + " Customers by Total Assets");
        List<Customer> sorted = new ArrayList<>(customerService.getAllCustomers());
        sorted.sort((a, b) -> b.getTotalAssets().compareTo(a.getTotalAssets()));
        int limit = Math.min(topN, sorted.size());
        for (int i = 0; i < limit; i++) {
            Customer customer = sorted.get(i);
            report.addLine(customer.getFullName() + " (" + customer.getCustomerId() + ")", customer.getTotalAssets());
        }
        return report;
    }

    public Report generateLoanPortfolioReport() {
        Report report = new Report("Active Loan Portfolio");
        for (Account account : accountService.getAllAccounts()) {
            if (account instanceof LoanAccount) {
                LoanAccount loan = (LoanAccount) account;
                if (loan.getLoanStatus() == LoanStatus.ACTIVE) {
                    report.addLine(loan.getAccountNumber() + " (" + loan.getOwner().getFullName() + ")",
                            loan.getOutstandingPrincipal());
                }
            }
        }
        return report;
    }

    public Report generateCreditCardExposureReport() {
        Report report = new Report("Credit Card Outstanding Exposure");
        for (Account account : accountService.getAllAccounts()) {
            if (account instanceof CreditCardAccount) {
                CreditCardAccount card = (CreditCardAccount) account;
                report.addLine(card.getAccountNumber() + " (" + card.getOwner().getFullName() + ")",
                        card.getOutstandingBalance());
            }
        }
        return report;
    }
}

// =========================================================================================
// BATCH SCHEDULING SIMULATION
// =========================================================================================

interface BatchJob {
    String getName();
    void execute();
}

/**
 * A simple job that applies monthly interest and fees across all accounts.
 */
class MonthEndBatchJob implements BatchJob {
    private final Bank bank;
    private final InterestCalculator calculator;

    public MonthEndBatchJob(Bank bank, InterestCalculator calculator) {
        this.bank = bank;
        this.calculator = calculator;
    }

    @Override
    public String getName() {
        return "MonthEndProcessing";
    }

    @Override
    public void execute() {
        bank.runMonthEndProcessing(calculator);
    }
}

/**
 * A job that executes all standing instructions that are due as of today.
 */
class StandingInstructionBatchJob implements BatchJob {
    private final StandingInstructionService standingInstructionService;

    public StandingInstructionBatchJob(StandingInstructionService standingInstructionService) {
        this.standingInstructionService = standingInstructionService;
    }

    @Override
    public String getName() {
        return "StandingInstructionExecution";
    }

    @Override
    public void execute() {
        standingInstructionService.executeDueInstructions(LocalDate.now());
    }
}

/**
 * A minimal batch scheduler that runs a sequence of registered jobs in order,
 * logging start/completion times and capturing any failures without halting
 * the remaining pipeline.
 */
class BatchScheduler {
    private final List<BatchJob> jobs;
    private final AuditService auditService;

    public BatchScheduler(AuditService auditService) {
        this.jobs = new ArrayList<>();
        this.auditService = auditService;
    }

    public void registerJob(BatchJob job) {
        jobs.add(job);
    }

    public void runAll() {
        for (BatchJob job : jobs) {
            auditService.log("BatchScheduler", "JOB_STARTED", job.getName());
            try {
                job.execute();
                auditService.log("BatchScheduler", "JOB_COMPLETED", job.getName());
            } catch (Exception e) {
                auditService.log("BatchScheduler", "JOB_FAILED", job.getName() + ": " + e.getMessage());
            }
        }
    }
}

// =========================================================================================
// FRAUD DETECTION (SIMPLE RULES ENGINE)
// =========================================================================================

/**
 * A minimal rules-based fraud detection engine that flags suspicious transactions
 * based on simple heuristics such as unusually large amounts or rapid repeated
 * transfers. Production systems would use ML-based scoring and richer signals.
 */
class FraudDetectionService {
    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000.00");
    private final Map<String, Integer> transactionCountInWindow;
    private final AuditService auditService;

    public FraudDetectionService(AuditService auditService) {
        this.transactionCountInWindow = new HashMap<>();
        this.auditService = auditService;
    }

    public boolean isSuspicious(Account account, BigDecimal amount) {
        boolean flagged = false;
        if (amount.compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            flagged = true;
            auditService.log("FraudDetectionService", "FLAG_LARGE_TRANSACTION",
                    "Large transaction of " + CurrencyFormatter.format(amount) + " on account " + account.getAccountNumber());
        }
        int count = transactionCountInWindow.merge(account.getAccountNumber(), 1, Integer::sum);
        if (count > 10) {
            flagged = true;
            auditService.log("FraudDetectionService", "FLAG_VELOCITY",
                    "High transaction velocity on account " + account.getAccountNumber() + ": " + count + " transactions");
        }
        return flagged;
    }

    public void resetWindow() {
        transactionCountInWindow.clear();
    }
}

// =========================================================================================
// STATEMENT AND DOCUMENT GENERATION
// =========================================================================================

/**
 * Builds formatted monthly statement documents combining account details,
 * transaction history, and summary totals, suitable for printing or export.
 */
class StatementGenerator {

    public String generateMonthlyStatement(Account account, LocalDate periodStart, LocalDate periodEnd) {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("MONTHLY ACCOUNT STATEMENT\n");
        sb.append("=====================================================\n");
        sb.append("Account Number: ").append(account.getAccountNumber()).append("\n");
        sb.append("Account Holder: ").append(account.getOwner().getFullName()).append("\n");
        sb.append("Product: ").append(account.getProductDescription()).append("\n");
        sb.append("Statement Period: ").append(DateUtil.formatDate(periodStart)).append(" to ").append(DateUtil.formatDate(periodEnd)).append("\n");
        sb.append("-----------------------------------------------------\n");

        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        for (Transaction txn : account.getHistory().getAll()) {
            LocalDate txnDate = txn.getTimestamp().toLocalDate();
            if (!txnDate.isBefore(periodStart) && !txnDate.isAfter(periodEnd)) {
                sb.append(txn.toString()).append("\n");
                if (txn.getType() == TransactionType.DEPOSIT || txn.getType() == TransactionType.TRANSFER_IN
                        || txn.getType() == TransactionType.INTEREST_CREDIT) {
                    totalDeposits = totalDeposits.add(txn.getAmount());
                } else if (txn.getType() == TransactionType.WITHDRAWAL || txn.getType() == TransactionType.TRANSFER_OUT
                        || txn.getType() == TransactionType.FEE) {
                    totalWithdrawals = totalWithdrawals.add(txn.getAmount());
                }
            }
        }

        sb.append("-----------------------------------------------------\n");
        sb.append("Total Deposits: ").append(CurrencyFormatter.format(totalDeposits)).append("\n");
        sb.append("Total Withdrawals: ").append(CurrencyFormatter.format(totalWithdrawals)).append("\n");
        sb.append("Closing Balance: ").append(CurrencyFormatter.format(account.getBalance())).append("\n");
        sb.append("=====================================================\n");
        return sb.toString();
    }
}

// =========================================================================================
// CUSTOMER SUPPORT / TICKETING
// =========================================================================================

enum TicketPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

/**
 * Represents a customer support ticket raised for account or transaction disputes,
 * general inquiries, or service requests.
 */
class SupportTicket {
    private final String ticketId;
    private final Customer customer;
    private final String subject;
    private final String description;
    private TicketPriority priority;
    private TicketStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private final List<String> comments;

    public SupportTicket(String ticketId, Customer customer, String subject, String description, TicketPriority priority) {
        this.ticketId = ticketId;
        this.customer = customer;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.comments = new ArrayList<>();
    }

    public String getTicketId() {
        return ticketId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public void resolve() {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = TicketStatus.CLOSED;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    @Override
    public String toString() {
        return String.format("Ticket[%s] %s | Priority: %s | Status: %s | Customer: %s",
                ticketId, subject, priority, status, customer.getFullName());
    }
}

/**
 * Manages the lifecycle of customer support tickets: creation, updates, and resolution.
 */
class SupportTicketService {
    private final Map<String, SupportTicket> tickets;
    private final AtomicLong ticketSequence;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public SupportTicketService(AuditService auditService, NotificationService notificationService) {
        this.tickets = new LinkedHashMap<>();
        this.ticketSequence = new AtomicLong(1);
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public SupportTicket createTicket(Customer customer, String subject, String description, TicketPriority priority) {
        String id = "TICKET" + ticketSequence.getAndIncrement();
        SupportTicket ticket = new SupportTicket(id, customer, subject, description, priority);
        tickets.put(id, ticket);
        auditService.log("SupportTicketService", "CREATE_TICKET", ticket.toString());
        notificationService.notifyCustomer(customer, "Support Ticket Created",
                "Your ticket " + id + " has been created and is being reviewed.");
        return ticket;
    }

    public void addComment(String ticketId, String comment) {
        SupportTicket ticket = tickets.get(ticketId);
        if (ticket != null) {
            ticket.addComment(comment);
            auditService.log("SupportTicketService", "ADD_COMMENT", ticketId + ": " + comment);
        }
    }

    public void resolveTicket(String ticketId) {
        SupportTicket ticket = tickets.get(ticketId);
        if (ticket != null) {
            ticket.resolve();
            auditService.log("SupportTicketService", "RESOLVE_TICKET", "Resolved " + ticketId);
            notificationService.notifyCustomer(ticket.getCustomer(), "Ticket Resolved",
                    "Your ticket " + ticketId + " has been resolved.");
        }
    }

    public List<SupportTicket> getTicketsForCustomer(Customer customer) {
        List<SupportTicket> result = new ArrayList<>();
        for (SupportTicket ticket : tickets.values()) {
            if (ticket.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                result.add(ticket);
            }
        }
        return result;
    }

    public Collection<SupportTicket> getAllTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }
}

// =========================================================================================
// EXTENDED DEMONSTRATION ENTRY POINT
// =========================================================================================

/**
 * A secondary demonstration class showing usage of the extended services
 * (beneficiaries, standing instructions, currency exchange, security, reporting,
 * batch jobs, fraud detection, and support tickets) that complement the core
 * banking operations demonstrated in MainFile's main method.
 */
class ExtendedBankingDemo {

    public static void run(Bank bank) {
        AuditService auditService = bank.getAuditService();
        NotificationService notificationService = bank.getNotificationService();

        BeneficiaryService beneficiaryService = new BeneficiaryService(auditService);
        StandingInstructionService standingInstructionService =
                new StandingInstructionService(bank.getTransactionService(), auditService);
        CurrencyExchangeService currencyExchangeService = new CurrencyExchangeService(auditService);
        SecurityService securityService = new SecurityService(auditService);
        ReportService reportService = new ReportService(bank.getAccountService(), bank.getCustomerService());
        FraudDetectionService fraudDetectionService = new FraudDetectionService(auditService);
        SupportTicketService supportTicketService = new SupportTicketService(auditService, notificationService);
        StatementGenerator statementGenerator = new StatementGenerator();

        Collection<Customer> customers = bank.getCustomerService().getAllCustomers();
        Iterator<Customer> it = customers.iterator();
        if (!it.hasNext()) {
            return;
        }
        Customer firstCustomer = it.next();

        // Security enrollment and authentication demo
        securityService.enrollCustomer(firstCustomer, "1234");
        try {
            securityService.authenticate(firstCustomer, "1234");
            System.out.println("Authentication succeeded for " + firstCustomer.getFullName());
        } catch (AuthenticationException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }

        // Beneficiary demo
        beneficiaryService.addBeneficiary(firstCustomer, "Landlord", "ACC900000001", "NTB001");

        // Currency exchange demo
        try {
            BigDecimal converted = currencyExchangeService.convert(new BigDecimal("1000.00"), "USD", "EUR");
            System.out.println("1000 USD converts to " + converted + " EUR");
        } catch (InvalidAmountException e) {
            System.out.println("Currency conversion error: " + e.getMessage());
        }

        // Reports
        System.out.println();
        reportService.generateDepositsByProductReport().print();
        System.out.println();
        reportService.generateTopCustomersByAssetsReport(3).print();

        // Support ticket demo
        SupportTicket ticket = supportTicketService.createTicket(firstCustomer, "Dispute on transaction",
                "I do not recognize a recent withdrawal on my checking account.", TicketPriority.MEDIUM);
        supportTicketService.addComment(ticket.getTicketId(), "Investigating with fraud team.");
        supportTicketService.resolveTicket(ticket.getTicketId());

        // Batch job demo
        BatchScheduler scheduler = new BatchScheduler(auditService);
        scheduler.registerJob(new StandingInstructionBatchJob(standingInstructionService));
        scheduler.registerJob(new MonthEndBatchJob(bank, new InterestCalculator()));
        scheduler.runAll();

        // Fraud detection demo
        boolean suspicious = fraudDetectionService.isSuspicious(firstCustomer.getAccounts().get(0), new BigDecimal("15000.00"));
        System.out.println("Large transaction flagged as suspicious: " + suspicious);
    }
}

// =========================================================================================
// CONFIGURATION
// =========================================================================================

/**
 * Immutable configuration values controlling bank-wide policies such as fee
 * amounts, interest rate floors/ceilings, and transaction limits. Built using
 * the builder pattern for readability when many optional values are involved.
 */
final class BankConfiguration {
    private final BigDecimal minSavingsBalance;
    private final BigDecimal maxDailyTransferLimit;
    private final BigDecimal wireTransferFee;
    private final int maxFailedLoginAttempts;
    private final boolean fraudDetectionEnabled;

    private BankConfiguration(Builder builder) {
        this.minSavingsBalance = builder.minSavingsBalance;
        this.maxDailyTransferLimit = builder.maxDailyTransferLimit;
        this.wireTransferFee = builder.wireTransferFee;
        this.maxFailedLoginAttempts = builder.maxFailedLoginAttempts;
        this.fraudDetectionEnabled = builder.fraudDetectionEnabled;
    }

    public BigDecimal getMinSavingsBalance() {
        return minSavingsBalance;
    }

    public BigDecimal getMaxDailyTransferLimit() {
        return maxDailyTransferLimit;
    }

    public BigDecimal getWireTransferFee() {
        return wireTransferFee;
    }

    public int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    public boolean isFraudDetectionEnabled() {
        return fraudDetectionEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BankConfiguration}, allowing selective overriding of
     * default policy values.
     */
    public static class Builder {
        private BigDecimal minSavingsBalance = new BigDecimal("100.00");
        private BigDecimal maxDailyTransferLimit = new BigDecimal("25000.00");
        private BigDecimal wireTransferFee = new BigDecimal("15.00");
        private int maxFailedLoginAttempts = 3;
        private boolean fraudDetectionEnabled = true;

        public Builder minSavingsBalance(BigDecimal value) {
            this.minSavingsBalance = value;
            return this;
        }

        public Builder maxDailyTransferLimit(BigDecimal value) {
            this.maxDailyTransferLimit = value;
            return this;
        }

        public Builder wireTransferFee(BigDecimal value) {
            this.wireTransferFee = value;
            return this;
        }

        public Builder maxFailedLoginAttempts(int value) {
            this.maxFailedLoginAttempts = value;
            return this;
        }

        public Builder fraudDetectionEnabled(boolean value) {
            this.fraudDetectionEnabled = value;
            return this;
        }

        public BankConfiguration build() {
            return new BankConfiguration(this);
        }
    }
}

// =========================================================================================
// DAILY TRANSFER LIMIT ENFORCEMENT
// =========================================================================================

/**
 * Tracks cumulative transfer amounts per account per calendar day and enforces
 * a configurable daily limit, resetting automatically when the date rolls over.
 */
class DailyLimitTracker {
    private final Map<String, BigDecimal> dailyTotals;
    private final Map<String, LocalDate> lastResetDate;
    private final BigDecimal dailyLimit;

    public DailyLimitTracker(BigDecimal dailyLimit) {
        this.dailyTotals = new HashMap<>();
        this.lastResetDate = new HashMap<>();
        this.dailyLimit = dailyLimit;
    }

    private void resetIfNewDay(String accountNumber) {
        LocalDate today = LocalDate.now();
        LocalDate lastReset = lastResetDate.get(accountNumber);
        if (lastReset == null || !lastReset.equals(today)) {
            dailyTotals.put(accountNumber, BigDecimal.ZERO);
            lastResetDate.put(accountNumber, today);
        }
    }

    public boolean canTransfer(String accountNumber, BigDecimal amount) {
        resetIfNewDay(accountNumber);
        BigDecimal currentTotal = dailyTotals.getOrDefault(accountNumber, BigDecimal.ZERO);
        return currentTotal.add(amount).compareTo(dailyLimit) <= 0;
    }

    public void recordTransfer(String accountNumber, BigDecimal amount) {
        resetIfNewDay(accountNumber);
        dailyTotals.merge(accountNumber, amount, BigDecimal::add);
    }

    public BigDecimal getRemainingLimit(String accountNumber) {
        resetIfNewDay(accountNumber);
        BigDecimal used = dailyTotals.getOrDefault(accountNumber, BigDecimal.ZERO);
        return dailyLimit.subtract(used);
    }
}

// =========================================================================================
// CONSOLE MENU SIMULATION (CLI)
// =========================================================================================

/**
 * Represents a single menu option in the simulated console banking interface,
 * pairing a display label with an action to execute when selected.
 */
class MenuOption {
    private final String label;
    private final Runnable action;

    public MenuOption(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public void invoke() {
        action.run();
    }
}

/**
 * A minimal simulated console menu system that demonstrates how a text-based
 * front end might be wired to the underlying banking services. Rather than
 * blocking on real standard input (which would be inappropriate for an
 * automated demonstration), this class exposes a scripted sequence of
 * selections and executes the corresponding actions in order.
 */
class ConsoleMenu {
    private final String title;
    private final List<MenuOption> options;

    public ConsoleMenu(String title) {
        this.title = title;
        this.options = new ArrayList<>();
    }

    public void addOption(String label, Runnable action) {
        options.add(new MenuOption(label, action));
    }

    public void render() {
        System.out.println("=== " + title + " ===");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i).getLabel());
        }
    }

    public void select(int optionNumber) {
        int index = optionNumber - 1;
        if (index >= 0 && index < options.size()) {
            options.get(index).invoke();
        } else {
            System.out.println("Invalid selection: " + optionNumber);
        }
    }

    public void runScriptedSequence(int... selections) {
        render();
        for (int selection : selections) {
            System.out.println("\n> Selecting option " + selection + ": " +
                    (selection - 1 >= 0 && selection - 1 < options.size() ? options.get(selection - 1).getLabel() : "unknown"));
            select(selection);
        }
    }

    public void createCustomer(CustomerDao customerDao) {

        Customer customer = new Customer(
                1001L,
                "John",
                "Doe",
                "john.doe@example.com"
        );

        customerDao.save(customer);
    }
}

/**
 * Builds a scripted, non-interactive console menu wired to a given bank and
 * customer, demonstrating the kind of navigation a teller or ATM front end
 * might expose: balance inquiry, deposit, withdrawal, and mini-statement.
 */
class ConsoleMenuDemo {

    public static void run(Bank bank, Customer customer, Account primaryAccount) {
        ConsoleMenu menu = new ConsoleMenu("Account Services for " + customer.getFullName());

        menu.addOption("Check Balance", () ->
                System.out.println("Current balance: " + CurrencyFormatter.format(primaryAccount.getBalance())));

        menu.addOption("Deposit Funds", () -> {
            try {
                bank.getTransactionService().deposit(primaryAccount, new BigDecimal("100.00"), "ATM deposit");
                System.out.println("Deposit successful. New balance: " + CurrencyFormatter.format(primaryAccount.getBalance()));
            } catch (InvalidAmountException e) {
                System.out.println("Deposit failed: " + e.getMessage());
            }
        });

        menu.addOption("Withdraw Funds", () -> {
            try {
                bank.getTransactionService().withdraw(primaryAccount, new BigDecimal("50.00"), "ATM withdrawal");
                System.out.println("Withdrawal successful. New balance: " + CurrencyFormatter.format(primaryAccount.getBalance()));
            } catch (BankingException e) {
                System.out.println("Withdrawal failed: " + e.getMessage());
            }
        });

        menu.addOption("Print Mini Statement", () ->
                bank.getTransactionService().printStatement(primaryAccount));

        menu.addOption("Exit", () -> System.out.println("Session ended for " + customer.getFullName()));

        menu.runScriptedSequence(1, 2, 3, 4, 5);
    }
}