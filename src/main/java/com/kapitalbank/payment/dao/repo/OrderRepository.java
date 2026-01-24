package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
