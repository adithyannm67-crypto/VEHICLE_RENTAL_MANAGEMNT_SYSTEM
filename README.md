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

## How to Run This Project

Five steps, all run from the project root (the folder holding this README).

**1 — Create the database and load the schema**

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS vehicle_rental_db"
mysql -u root -p vehicle_rental_db < database/vehicle_rental.sql
```

**2 — Create a `.env` file next to this README**

```
DB_URL=jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=your_mysql_password
```

**3 — Check `lib/`** contains `mysql-connector-j-26.7.0.jar` **and**
`dotenv-java-3.2.0.jar`. Both ship with this project, and the commands below put
the whole folder on the classpath with the `lib/*` wildcard.

**4 — Compile**

```bash
javac -cp "lib/*" -d out -sourcepath src src/Main.java
```

**5 — Run**

```bash
java -cp "out:lib/*" Main     # Linux / macOS
java -cp "out;lib\*" Main     # Windows
```

On Windows you can skip steps 4–5 by double-clicking **`run_app.bat`**.

**Then log in**

- **Admin:** `admin@vrs.com` / `Admin@123` — created automatically the first time
  the app starts (see `UserDAO.ensureDefaultAdmin`).
- **Customer:** click *Create Account*; you are signed in straight away.

**Troubleshooting**

| Symptom | Cause |
|---|---|
| "Connection problem" dialog at startup | `.env` is missing, misspelled, or has the wrong values |
| `DB_URL, DB_USER, and DB_PASSWORD must be set in the .env file` | one of the three keys is absent from `.env` |
| `MySQL JDBC driver not found on classpath` | a jar is missing from `lib/`, or you named one jar instead of using `lib/*` |
| `Unknown database 'vehicle_rental_db'` | step 1 was skipped — the schema script contains only `USE`, it does not create the database |
| Nothing appears when you run it | the app needs a display; it cannot start in a headless shell |

## 1. Set up MySQL

1. Install MySQL if you don't have it, and start the server.
2. Create the database, then load the schema script into it:
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS vehicle_rental_db"
   mysql -u root -p vehicle_rental_db < database/vehicle_rental.sql
   ```
   The script contains only `USE vehicle_rental_db;`, so **the database must
   already exist before you run it** — otherwise MySQL reports
   `Unknown database 'vehicle_rental_db'`. It creates all 5 tables and inserts
   5 sample vehicles, and is safe to re-run (`CREATE TABLE IF NOT EXISTS` plus
   `ON DUPLICATE KEY UPDATE` on the seed rows). The default admin account is
   created automatically by the Java app the first time you run it (see below)
   — not by this script — so that its password is stored as a real hash.

## 2. Configure the database connection

`src/util/DBConnection.java` has no credentials in it — it reads them from a
`.env` file in the project root using dotenv-java:

```java
private static final Dotenv dotenv = Dotenv.configure().directory(".").ignoreIfMissing().load();
private static final String DB_URL = dotenv.get("DB_URL");
private static final String DB_USER = dotenv.get("DB_USER");
private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");
```

Create a file called `.env` next to this README (no quotes, no spaces around `=`):

```
DB_URL=jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=your_mysql_password
```

- All three keys are required. If any is missing the app still starts, but every
  database call fails with `DB_URL, DB_USER, and DB_PASSWORD must be set in the .env file`
  and you get the "Connection problem" dialog.
- `.env` and `.env.*` are in `.gitignore`, so your password is never committed.
- The values are read once, when `DBConnection` first loads — restart the app after
  editing the file.
- The working directory must be the project root (the `.bat`/`.sh` scripts `cd` there
  for you), otherwise `.` will not point at your `.env`.

## 3. The jars in `lib/`

Both jars the application needs are already included:

| Jar | Used for |
|---|---|
| `mysql-connector-j-26.7.0.jar` | the MySQL JDBC driver (`com.mysql.cj.jdbc.Driver`) |
| `dotenv-java-3.2.0.jar` | reading the `.env` file |

The commands in section 4 put the **whole folder** on the classpath with the
`lib/*` wildcard, so the exact filenames don't matter — but **both** jars must be
present, and you must use the wildcard rather than naming a single jar (naming only
the connector would break `DBConnection`, which imports `Dotenv`).

To upgrade the driver, download `mysql-connector-j` from
https://dev.mysql.com/downloads/connector/j/ and drop it into `lib/`.

## 4. Compile and run

Run these from the project's root folder (the folder containing this README).

```bash
# Compile into an "out" folder — note the -cp flag, which is required
javac -cp "lib/*" -d out -sourcepath src src/Main.java
```

```bash
# Run — Linux / macOS (colon separates classpath entries)
java -cp "out:lib/*" Main

# Run — Windows (semicolon separates classpath entries)
java -cp "out;lib\*" Main
```

**Windows shortcut:** double-click **`run_app.bat`**. It runs the two commands
above for you and stops with a clear message if `.env` or a jar is missing.

> **Note:** `run_app.sh` is currently out of date (it omits `-cp "lib/*"` when
> compiling and picks the wrong jar when running), so on Linux/macOS use the two
> commands above.

### Using an IDE instead (IntelliJ / Eclipse / NetBeans)

1. Create a new Java project and copy the `src` folder in as your source root.
2. Add **both** jars from `lib/` to the project's libraries/dependencies.
3. Set `Main.java` as the run configuration's main class.
4. Set the run configuration's **working directory** to the project root, so
   `Dotenv.configure().directory(".")` finds your `.env`.
5. Run it.

## 5. Log in

- **Admin:** `admin@vrs.com` / `Admin@123` — the account is created automatically
  on first run (see `UserDAO.ensureDefaultAdmin`) so its password is stored as a
  real SHA-256 hash. The app itself never displays these credentials; they appear
  only here and in the source.
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
