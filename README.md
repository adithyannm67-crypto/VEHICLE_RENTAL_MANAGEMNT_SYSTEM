# Vehicle Rental Management System

A complete Java Swing desktop application with MySQL + JDBC backend, built as a
BTech CSE college project.

## What's included

- **model/** — Vehicle (abstract) → Car, Bike (inheritance + polymorphism), User, Rental, Payment, Maintenance
- **dao/** — JDBC data-access classes (PreparedStatement, no string-concatenated SQL)
- **service/** — business logic, validation, and the PaymentMethodProcessor interface
  (UPI / Card / Cash implementations — demonstrates interfaces + polymorphism;
  the UI books with the Cash processor, no online payment)
- **ui/** — ONE main window (`AppFrame`, CardLayout) holding every screen as a panel:
  Welcome, Login, Register, Customer Dashboard, Browse Vehicles (live filter),
  Rent + Booking Confirmation, My Bookings (cancel/return), Profile,
  Admin Dashboard, Manage Vehicles, Customers, Rentals, Payments, Maintenance.
  Shared look & feel lives in `Theme.java` and `Widgets.java`.
- **util/** — DBConnection, PasswordUtil (SHA-256 hashing), ValidationUtil
- **database/vehicle_rental.sql** — full schema + sample vehicles

The whole project has already been compiled successfully with `javac` (JDK 21)
to confirm there are no errors.

## How the pieces fit together

```
Swing UI (ui/)
     ↓
Service layer (service/)   <-- validation, business rules, calculates totals
     ↓
DAO layer (dao/)           <-- JDBC, PreparedStatement, SQL
     ↓
MySQL (database/vehicle_rental.sql)
```

## 1. Set up MySQL

1. Install MySQL if you don't have it, and start the server.
2. Run the schema script:
   ```
   mysql -u root -p < database/vehicle_rental.sql
   ```
   This creates the `vehicle_rental_db` database, all 5 tables, and inserts
   5 sample vehicles. The default admin account is created automatically by
   the Java app the first time you run it (see below) — not by this script —
   so that its password is stored as a real hash.

## 2. Configure the database connection

Open `src/util/DBConnection.java` and update these three lines to match your
MySQL setup:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root"; // <-- your MySQL password
```

## 3. Download the MySQL JDBC driver

Download `mysql-connector-j` (the `.jar` file) from:
https://dev.mysql.com/downloads/connector/j/

Save it as `lib/mysql-connector-j.jar` inside this project folder.

## 4. Compile and run

Easiest: double-click **`run_app.bat`** (Windows) or run `./run_app.sh` (Linux/macOS).
It compiles the project and starts it with the connector jar from `lib/`.

Manual way, from the project's root folder (where this README is):

```bash
# Compile everything into an "out" folder
javac -d out $(find src -name "*.java")

# Run the application (adjust the jar filename if different)
java -cp "out:lib/mysql-connector-j.jar" Main
```

On Windows, use a semicolon instead of a colon in `-cp`:
```
java -cp "out;lib\mysql-connector-j.jar" Main
```

### Using an IDE instead (IntelliJ / Eclipse / NetBeans)

1. Create a new Java project and copy the `src` folder in as your source root.
2. Add `mysql-connector-j.jar` to the project's libraries/dependencies.
3. Set `Main.java` as the run configuration's main class.
4. Run it.

## 5. Log in

- **Admin:** an admin account is created automatically on first run
  (see `UserDAO.ensureDefaultAdmin`). Credentials are intentionally not shown in the app.
- **Customer:** choose *Create Account* on the login screen; you are signed in automatically.

## Demonstration flow

Everything happens inside one window; dialogs are used only for confirmations.

**Customer:** Welcome → Get Started → Login / Create Account → Dashboard → Browse Vehicles
(type to search, filter by Type / Status) → select an available vehicle → Rent Vehicle →
pick dates (price updates live) → Confirm Booking → Booking Confirmed → My Bookings →
Return Vehicle or Cancel Booking → Profile (Edit Profile) → Logout.

**Admin:** Login → Admin Dashboard (statistics load automatically) → Manage Vehicles
(Add / Edit / Deactivate / Activate) → Customers (search) → Rentals (search) → Payments →
Maintenance (add a record → vehicle becomes MAINTENANCE; Mark Completed → AVAILABLE) → Logout.

## Key business rules implemented

- The **backend always recalculates the rental total** from
  `days × price_per_day + vehicle-specific extra charge` — the frontend
  never sends an amount directly.
- A vehicle cannot be double-booked: `RentalDAO.hasConflict()` checks for any
  overlapping CONFIRMED booking before a new one is created.
- Passwords are never stored or displayed in plain text (SHA-256 hash).
- Admin screens and customer screens are separate — `AppFrame.navigate` refuses to show
  a screen that the signed-in role may not see (and shows nothing when logged out).
- Vehicles are never hard-deleted from the UI: **Deactivate** sets status `INACTIVE`
  (hidden from customers, history kept). A vehicle that is RENTED or in MAINTENANCE
  cannot be deactivated; maintenance cannot be added to a RENTED/INACTIVE vehicle.

## Notes

- Payments are not part of this project: bookings are recorded with a Cash payment
  record (pay at pickup) and the Payments screen only lists those records.
- Database errors are logged to the console only; the user sees short friendly messages.
