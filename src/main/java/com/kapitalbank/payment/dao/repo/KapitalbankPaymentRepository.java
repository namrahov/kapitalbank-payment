package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.KapitalbankPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KapitalbankPaymentRepository
        extends JpaRepository<KapitalbankPayment, Long> {

    List<KapitalbankPayment> findByStatusIn(
            Collection<String> statuses);

    List<KapitalbankPayment> findByStatus(String status);

    List<KapitalbankPayment> findByUserId(Long userId);

    Optional<KapitalbankPayment> findByOrderId(Long orderId);

    default List<KapitalbankPayment> findSuccessful() {
        return findByStatusIn(
                List.of("FullyPaid", "PartiallyPaid", "Approved"));
    }

    default List<KapitalbankPayment> findPending() {
        return findByStatus("Preparing");
    }
}
