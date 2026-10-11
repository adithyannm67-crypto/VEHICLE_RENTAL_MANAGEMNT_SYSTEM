package service;

import dao.PaymentDAO;
import dao.RentalDAO;
import dao.VehicleDAO;
import model.Payment;
import model.Rental;
import model.Vehicle;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Business logic for booking, cancelling and returning vehicles.
 * This is where "the backend calculates the final amount" rule from the
 * project brief is enforced: the frontend never sends a total, only dates.
 */
public class RentalService {

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Small holder returned after a successful booking, used for the confirmation screen. */
    public static class BookingConfirmation {
        public Rental rental;
        public Payment payment;
        public Vehicle vehicle;
    }

    public double previewTotal(Vehicle vehicle, LocalDate pickup, LocalDate returnDate) {
        int days = (int) ChronoUnit.DAYS.between(pickup, returnDate);
        return days * vehicle.getPricePerDay() + vehicle.calculateExtraCharges(days);
    }

    public ServiceResult<BookingConfirmation> bookVehicle(int userId, int vehicleId,
                                                           LocalDate pickup, LocalDate returnDate,
                                                           PaymentMethodProcessor paymentMethod) {
        if (!ValidationUtil.isValidDateRange(pickup, returnDate)) {
            return ServiceResult.fail("Pickup date cannot be in the past, and return date must be after pickup date.");
        }
        try {
            Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);
            if (vehicle == null) return ServiceResult.fail("Vehicle not found.");
            if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
                return ServiceResult.fail("This vehicle is not available for booking right now (status: " + vehicle.getStatus() + ").");
            }
            if (rentalDAO.hasConflict(vehicleId, pickup, returnDate)) {
                return ServiceResult.fail("This vehicle is already booked for an overlapping date range.");
            }

            int days = (int) ChronoUnit.DAYS.between(pickup, returnDate);
            double baseAmount = days * vehicle.getPricePerDay();
            double extra = vehicle.calculateExtraCharges(days);
            double total = baseAmount + extra;

            // 1. create the rental record
            Rental rental = new Rental();
            rental.setUserId(userId);
            rental.setVehicleId(vehicleId);
            rental.setPickupDate(pickup);
            rental.setReturnDate(returnDate);
            rental.setTotalDays(days);
            rental.setTotalAmount(total);
            rental.setRentalStatus("CONFIRMED");
            int rentalId = rentalDAO.createRental(rental);
            if (rentalId <= 0) return ServiceResult.fail("Could not create the booking. Please try again.");
            rental.setRentalId(rentalId);

            // 2. process the (simulated) payment
            String status = paymentMethod.process(total);
            Payment payment = new Payment();
            payment.setRentalId(rentalId);
            payment.setAmount(total);
            payment.setPaymentMethod(paymentMethod.getMethodName());
            payment.setPaymentStatus(status);
            paymentDAO.createPayment(payment);

            // 3. mark the vehicle as rented so it cannot be double-booked
            vehicleDAO.updateVehicleStatus(vehicleId, "RENTED");

            BookingConfirmation confirmation = new BookingConfirmation();
            confirmation.rental = rental;
            confirmation.payment = payment;
            confirmation.vehicle = vehicle;
            return ServiceResult.ok("Booking confirmed!", confirmation);

        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to complete the booking. Please try again.");
        }
    }

    public ServiceResult<Void> cancelBooking(int rentalId) {
        try {
            Rental rental = rentalDAO.getRentalById(rentalId);
            if (rental == null) return ServiceResult.fail("Booking not found.");
            if (!"CONFIRMED".equalsIgnoreCase(rental.getRentalStatus())) {
                return ServiceResult.fail("Only a confirmed, active booking can be cancelled.");
            }
            rentalDAO.updateRentalStatus(rentalId, "CANCELLED");
            vehicleDAO.updateVehicleStatus(rental.getVehicleId(), "AVAILABLE");
            return ServiceResult.ok("Booking cancelled. The vehicle is now available again.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to cancel the booking. Please try again.");
        }
    }

    public ServiceResult<Void> returnVehicle(int rentalId) {
        try {
            Rental rental = rentalDAO.getRentalById(rentalId);
            if (rental == null) return ServiceResult.fail("Booking not found.");
            if (!"CONFIRMED".equalsIgnoreCase(rental.getRentalStatus())) {
                return ServiceResult.fail("Only an active (confirmed) rental can be returned.");
            }
            rentalDAO.updateRentalStatus(rentalId, "COMPLETED");
            vehicleDAO.updateVehicleStatus(rental.getVehicleId(), "AVAILABLE");
            return ServiceResult.ok("Vehicle returned successfully. Thank you!", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to return the vehicle. Please try again.");
        }
    }

    public List<Rental> getRentalsByUser(int userId) throws SQLException {
        return rentalDAO.getRentalsByUser(userId);
    }

    public List<Rental> getAllRentals() throws SQLException {
        return rentalDAO.getAllRentals();
    }
}
