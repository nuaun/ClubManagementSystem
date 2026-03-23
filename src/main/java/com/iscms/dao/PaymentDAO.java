package com.iscms.dao;

import com.iscms.model.Payment;
import java.util.List;

public interface PaymentDAO {
    void insert(Payment payment);
    void updateStatus(int paymentId, String status);
    List<Payment> findAllByMemberId(int memberId);
    List<Payment> findByStatus(String status);
    List<Payment> findAll();
}