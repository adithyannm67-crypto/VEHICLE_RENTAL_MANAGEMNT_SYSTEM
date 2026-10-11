package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/** Business logic + validation sitting on top of UserDAO. */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public ServiceResult<User> register(String name, String email, String phone,
                                         String password, String address) {
        if (!ValidationUtil.isNotEmpty(name)) return ServiceResult.fail("Name is required.");
        if (!ValidationUtil.isValidEmail(email)) return ServiceResult.fail("Please enter a valid email address.");
        if (!ValidationUtil.isValidPhone(phone)) return ServiceResult.fail("Phone number must be exactly 10 digits.");
        if (!ValidationUtil.isValidPassword(password))
            return ServiceResult.fail("Password must be at least 6 characters and contain a letter and a number.");

        try {
            if (userDAO.emailExists(email)) {
                return ServiceResult.fail("An account with this email already exists.");
            }
            User user = new User(0, name.trim(), email.trim(), phone.trim(),
                    PasswordUtil.hash(password), address == null ? "" : address.trim(), "CUSTOMER");
            boolean ok = userDAO.registerUser(user);
            return ok ? ServiceResult.ok("Registration successful!", user)
                      : ServiceResult.fail("Registration failed. Please try again.");
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to create your account. Please try again.");
        }
    }

    public ServiceResult<User> login(String email, String password) {
        if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(password)) {
            return ServiceResult.fail("Please enter your email and password.");
        }
        try {
            User user = userDAO.login(email.trim(), password);
            return user != null ? ServiceResult.ok("Login successful.", user)
                                 : ServiceResult.fail("Login failed. Please check your email and password.");
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to sign in right now. Please try again.");
        }
    }

    /** Lets a signed-in user change their own name / phone / address (email stays fixed). */
    public ServiceResult<Void> updateProfile(int userId, String name, String phone, String address) {
        if (!ValidationUtil.isNotEmpty(name)) return ServiceResult.fail("Name is required.");
        if (!ValidationUtil.isValidPhone(phone)) return ServiceResult.fail("Phone number must be exactly 10 digits.");
        try {
            userDAO.updateProfile(userId, name.trim(), phone.trim(), address == null ? "" : address.trim());
            return ServiceResult.ok("Profile updated.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to update your profile. Please try again.");
        }
    }

    public List<User> getAllCustomers() throws SQLException {
        return userDAO.getAllCustomers();
    }

    public void ensureDefaultAdmin() throws SQLException {
        userDAO.ensureDefaultAdmin();
    }
}
