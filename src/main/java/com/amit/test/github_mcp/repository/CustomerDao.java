package com.amit.test.github_mcp.repository;

import com.amit.test.github_mcp.domain.Customer;

public interface CustomerDao {

    Customer findById(long customerId);

    List<Customer> findAll();

    void save(Customer customer);

    void update(Customer customer);

    void delete(long customerId);

    boolean exists(long customerId);
}