package com.amit.test.github_mcp.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.amit.test.github_mcp.domain.Customer;

public class CustomerDaoImpl implements CustomerDao {

    private final Connection connection;

    public CustomerDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Customer findById(long customerId) {

        String sql = """
                SELECT customer_id, first_name, last_name, email
                FROM customers
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer", e);
        }

        return null;
    }

    @Override
    public List<Customer> findAll() {

        String sql = """
                SELECT customer_id, first_name, last_name, email
                FROM customers
                """;

        List<Customer> customers = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching customers", e);
        }

        return customers;
    }

    @Override
    public void save(Customer customer) {

        String sql = """
                INSERT INTO customers
                    (customer_id, first_name, last_name, email)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, customer.getCustomerId());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getLastName());
            ps.setString(4, customer.getEmail());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer", e);
        }
    }

    @Override
    public void update(Customer customer) {

        String sql = """
                UPDATE customers
                SET first_name = ?,
                    last_name = ?,
                    email = ?
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getEmail());
            ps.setLong(4, customer.getCustomerId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating customer", e);
        }
    }

    @Override
    public void delete(long customerId) {

        String sql = """
                DELETE FROM customers
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer", e);
        }
    }

    @Override
    public boolean exists(long customerId) {

        String sql = """
                SELECT 1
                FROM customers
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking customer", e);
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {

        Customer customer = new Customer();

        customer.setCustomerId(rs.getLong("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));

        return customer;
    }
}