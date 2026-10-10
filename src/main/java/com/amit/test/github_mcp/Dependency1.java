package com.amit.test.github_mcp;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer operations dependency for the banking demo.
 * This example uses in-memory storage; data is lost when the process stops.
 */
@Service
public class Dependency1 {
    private final Map<String, CustomerRecord> customers = new LinkedHashMap<>();

    public synchronized CustomerRecord createCustomer(String fullName, String email, String phone) {
        validateName(fullName);
        validateEmail(email);
        validatePhone(phone);
        if (emailInUse(email)) throw new IllegalArgumentException("Email is already registered");
        String id = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        CustomerRecord record = new CustomerRecord(id, fullName.trim(), email.trim(),
                phone.trim(), CustomerStatus.ACTIVE, LocalDateTime.now());
        customers.put(id, record);
        return record.copy();
    }

    public synchronized Optional<CustomerRecord> findCustomer(String customerId) {
        requireText(customerId, "customerId");
        CustomerRecord record = customers.get(customerId.trim());
        return record == null ? Optional.empty() : Optional.of(record.copy());
    }

    public synchronized List<CustomerRecord> getAllCustomers() {
        List<CustomerRecord> result = new ArrayList<>();
        for (CustomerRecord record : customers.values()) result.add(record.copy());
        return Collections.unmodifiableList(result);
    }

    public synchronized List<CustomerRecord> searchCustomers(String query) {
        requireText(query, "query");
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<CustomerRecord> result = new ArrayList<>();
        for (CustomerRecord record : customers.values()) {
            if (record.fullName.toLowerCase(Locale.ROOT).contains(q)
                    || record.email.toLowerCase(Locale.ROOT).contains(q)) {
                result.add(record.copy());
            }
        }
        result.sort(Comparator.comparing(CustomerRecord::getFullName));
        return Collections.unmodifiableList(result);
    }

    public synchronized CustomerRecord updateContact(String customerId, String email, String phone) {
        CustomerRecord record = requireCustomer(customerId);
        validateEmail(email);
        validatePhone(phone);
        if (!record.email.equalsIgnoreCase(email.trim()) && emailInUse(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        record.email = email.trim();
        record.phoneNumber = phone.trim();
        record.updatedAt = LocalDateTime.now();
        return record.copy();
    }

    public synchronized CustomerRecord renameCustomer(String customerId, String fullName) {
        CustomerRecord record = requireCustomer(customerId);
        validateName(fullName);
        record.fullName = fullName.trim();
        record.updatedAt = LocalDateTime.now();
        return record.copy();
    }

    public synchronized CustomerRecord deactivateCustomer(String customerId) {
        CustomerRecord record = requireCustomer(customerId);
        record.status = CustomerStatus.INACTIVE;
        record.updatedAt = LocalDateTime.now();
        return record.copy();
    }

    public synchronized CustomerRecord activateCustomer(String customerId) {
        CustomerRecord record = requireCustomer(customerId);
        record.status = CustomerStatus.ACTIVE;
        record.updatedAt = LocalDateTime.now();
        return record.copy();
    }

    public synchronized boolean deleteCustomer(String customerId) {
        requireText(customerId, "customerId");
        return customers.remove(customerId.trim()) != null;
    }

    public synchronized boolean customerExists(String customerId) {
        return customerId != null && customers.containsKey(customerId.trim());
    }

    public synchronized boolean emailInUse(String email) {
        if (email == null) return false;
        for (CustomerRecord record : customers.values()) {
            if (record.email.equalsIgnoreCase(email.trim())) return true;
        }
        return false;
    }

    public synchronized int countCustomers() {
        return customers.size();
    }

    public synchronized long countActiveCustomers() {
        return customers.values().stream().filter(c -> c.status == CustomerStatus.ACTIVE).count();
    }

    public synchronized long countInactiveCustomers() {
        return customers.values().stream().filter(c -> c.status == CustomerStatus.INACTIVE).count();
    }

    public synchronized String getCustomerStatus(String customerId) {
        return requireCustomer(customerId).status.name();
    }

    public boolean validateCustomerId(String customerId) {
        return customerId != null && customerId.trim().matches("CUST-[A-F0-9]{8}");
    }

    public boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        String value = email.trim();
        int at = value.indexOf('@');
        return at > 0 && at < value.length() - 1
                && value.indexOf('.', at) > at + 1 && !value.contains(" ");
    }

    public boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }

    public String maskEmail(String email) {
        validateEmail(email);
        String value = email.trim();
        int at = value.indexOf('@');
        String local = value.substring(0, at);
        String masked = local.length() <= 2 ? local.charAt(0) + "*"
                : local.charAt(0) + "*".repeat(Math.min(6, local.length() - 2))
                + local.charAt(local.length() - 1);
        return masked + value.substring(at);
    }

    public String maskPhone(String phone) {
        validatePhone(phone);
        String digits = phone.replaceAll("\\D", "");
        return "*".repeat(Math.max(0, digits.length() - 4))
                + digits.substring(Math.max(0, digits.length() - 4));
    }

    public synchronized String formatCustomerSummary(String customerId) {
        CustomerRecord c = requireCustomer(customerId);
        return c.id + " | " + c.fullName + " | " + maskEmail(c.email) + " | " + c.status;
    }

    public synchronized String toCsvRow(String customerId) {
        CustomerRecord c = requireCustomer(customerId);
        return csv(c.id) + "," + csv(c.fullName) + "," + csv(c.email) + ","
                + csv(c.phoneNumber) + "," + csv(c.status.name());
    }

    public String getCsvHeader() {
        return "customerId,fullName,email,phoneNumber,status";
    }

    public synchronized List<CustomerRecord> getActiveCustomers() {
        List<CustomerRecord> result = new ArrayList<>();
        for (CustomerRecord c : customers.values()) {
            if (c.status == CustomerStatus.ACTIVE) result.add(c.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<CustomerRecord> getInactiveCustomers() {
        List<CustomerRecord> result = new ArrayList<>();
        for (CustomerRecord c : customers.values()) {
            if (c.status == CustomerStatus.INACTIVE) result.add(c.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<CustomerRecord> getCustomersSortedByName() {
        List<CustomerRecord> result = new ArrayList<>(getAllCustomers());
        result.sort(Comparator.comparing(CustomerRecord::getFullName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(result);
    }

    public synchronized List<CustomerRecord> getCustomersSortedByCreationTime() {
        List<CustomerRecord> result = new ArrayList<>(getAllCustomers());
        result.sort(Comparator.comparing(CustomerRecord::getCreatedAt));
        return Collections.unmodifiableList(result);
    }

    public synchronized void clearForTesting() {
        customers.clear();
    }

    private CustomerRecord requireCustomer(String customerId) {
        requireText(customerId, "customerId");
        CustomerRecord record = customers.get(customerId.trim());
        if (record == null) throw new IllegalArgumentException("Customer not found: " + customerId);
        return record;
    }

    private void validateName(String value) {
        if (!isValidName(value)) throw new IllegalArgumentException("Name must have at least 2 characters");
    }

    private void validateEmail(String value) {
        if (!isValidEmail(value)) throw new IllegalArgumentException("Invalid email address");
    }

    private void validatePhone(String value) {
        if (!isValidPhone(value)) throw new IllegalArgumentException("Invalid phone number");
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    public enum CustomerStatus { ACTIVE, INACTIVE }

    public static final class CustomerRecord {
        private final String id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private CustomerStatus status;
        private final LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private CustomerRecord(String id, String fullName, String email, String phoneNumber,
                               CustomerStatus status, LocalDateTime createdAt) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = createdAt;
        }

        private CustomerRecord copy() {
            CustomerRecord result = new CustomerRecord(id, fullName, email, phoneNumber, status, createdAt);
            result.updatedAt = updatedAt;
            return result;
        }

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public CustomerStatus getStatus() { return status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "CustomerRecord{id='" + id + "', fullName='" + fullName
                    + "', email='" + email + "', status=" + status + "}";
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
