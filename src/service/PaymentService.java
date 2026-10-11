package service;

import dao.PaymentDAO;
import model.Payment;

import java.sql.SQLException;
import java.util.List;

/** Read-only access to payment records, so the UI never talks to the DAO directly. */
public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    public List<Payment> getAllPayments() throws SQLException {
        return paymentDAO.getAllPayments();
    }
}
