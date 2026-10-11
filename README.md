# Vehicle Rental Management System

A Java Swing desktop application for managing vehicle rentals, built as a B.Tech Computer Science and Engineering college project using **Java, JDBC, and MySQL**.

## Features

### Customer
- Register and log in securely.
- Browse available cars and bikes.
- Search and filter vehicles by type and availability.
- Book vehicles by selecting rental dates.
- View booking confirmations and rental history.
- Cancel bookings and return vehicles.
- View and edit profile information.

### Administrator
- View dashboard statistics.
- Add, edit, activate, and deactivate vehicles.
- Search and manage customer records.
- View and search rental records.
- View payment records.
- Record vehicle maintenance and mark maintenance as completed.

### Technical Highlights
- Object-oriented design using inheritance, abstraction, interfaces, and polymorphism.
- Layered architecture separating the UI, business logic, and database operations.
- JDBC database access using `PreparedStatement`.
- Environment-based database configuration.
- Password hashing and role-based screen access.
- Rental date validation and vehicle availability checks.
- Transactional database operations where implemented.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java (JDK 21) | Application development |
| Java Swing | Desktop user interface |
| MySQL | Relational database |
| JDBC | Database connectivity |
| MySQL Connector/J | MySQL JDBC driver |
| dotenv-java | Loading environment variables from `.env` |
| Aiven | Hosted MySQL database, if configured |

## Project Architecture

The application follows a layered architecture:

```text
Vehicle Rental Management System
│
├── ui/                 Swing screens and UI components
├── service/            Business logic and validation
├── dao/                Database access operations
├── model/              Domain models
├── util/               Database, password, and validation utilities
├── database/
│   └── vehicle_rental.sql
├── lib/                External JAR dependencies
├── src/
│   └── Main.java
├── .env                Database configuration (not committed)
├── .gitignore
├── run_app.bat
├── run_app.sh
└── README.md
```

The package directories are located under the source tree. The structure above summarizes their responsibilities; the exact physical paths depend on the repository layout.

### Application Flow

```text
Swing UI
   ↓
Service Layer
   ↓
DAO Layer
   ↓
MySQL Database
```

- **UI layer:** Displays screens and collects user input.
- **Service layer:** Enforces business rules, validates requests, and calculates rental charges.
- **DAO layer:** Executes SQL queries using JDBC and `PreparedStatement`.
- **Database:** Stores users, vehicles, rentals, payments, and maintenance records.

## Object-Oriented Programming

The project demonstrates several core OOP concepts.

- **Abstraction:** `Vehicle` is an abstract class.
- **Inheritance:** `Car` and `Bike` extend `Vehicle`.
- **Polymorphism:** Vehicle subclasses and payment processors provide specialized implementations.
- **Interfaces:** `PaymentMethodProcessor` defines the payment-processing contract, with UPI, Card, and Cash implementations.
- **Encapsulation:** Domain models and service classes organize application data and behavior.

## Prerequisites

Before running the application, install or prepare:

1. JDK 21.
2. MySQL Server or a compatible hosted MySQL instance.
3. MySQL Connector/J.
4. dotenv-java.
5. A terminal such as PowerShell or the VS Code integrated terminal.

## Installation and Setup

### 1. Clone or download the repository

Open a terminal in the project directory—the folder containing this README.

### 2. Create the database and import the schema

Run the following commands in a terminal where the MySQL command-line client is available:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS vehicle_rental_db"
mysql -u root -p vehicle_rental_db < database/vehicle_rental.sql
```

Enter your MySQL password when prompted.

If you use Aiven or another hosted MySQL provider, connect using that provider's credentials and import the schema into the appropriate database instead.

**Important:** Confirm that `database/vehicle_rental.sql` is compatible with the database name you use. The commands above assume that the script's SQL statements target `vehicle_rental_db`.

### 3. Configure the JAR dependencies

Place both dependency JAR files inside the project's `lib/` directory.

```text
lib/
├── mysql-connector-j-26.7.0.jar
└── dotenv-java-3.2.0.jar
```

#### MySQL Connector/J

Download it from the official MySQL website:

https://dev.mysql.com/downloads/connector/j/

Select the intended version and the Platform Independent archive. Extract it and copy the connector JAR into `lib/`.

#### dotenv-java

Download the JAR directly:

https://repo.maven.apache.org/maven2/io/github/cdimascio/dotenv-java/3.2.0/dotenv-java-3.2.0.jar

**Note:** The filenames above must match the files actually included in your repository. If you use different dependency versions, update the documentation and filenames accordingly.

### 4. Configure environment variables

Create a file named `.env` in the project root, alongside `README.md`.

```env
DB_HOST=your-aiven-host
DB_PORT=your-aiven-port
DB_NAME=vehicle_rental_db
DB_USER=your-database-username
DB_PASSWORD=your-database-password
DB_URL=jdbc:mysql://your-aiven-host:your-aiven-port/vehicle_rental_db?sslMode=REQUIRED
```

Replace the placeholder values with your actual database configuration.

For a local MySQL installation, use the corresponding local host, port, username, password, and connection URL. Adjust SSL options to match your database provider's requirements.

The application's database connection code determines which environment variables are actually required. Ensure the values match the keys read by `DBConnection`.

**Security requirements**

- Never commit `.env` to GitHub.
- Ensure `.env` is listed in `.gitignore`.
- Do not publish database credentials in screenshots, source code, or documentation.
- If credentials have already been exposed publicly, rotate them.

### 5. Compile the application

Run this command from the project root:

```bash
javac -cp "lib/*" -d out -sourcepath src src/Main.java
```

This compiles `Main.java` and its referenced source files, places the compiled classes in `out/`, and makes the JAR dependencies available during compilation.

### 6. Run the application

**Windows — PowerShell or Command Prompt**

```powershell
java -cp "out;lib/*" Main
```

**Linux or macOS**

```bash
java -cp "out:lib/*" Main
```

The classpath separator is `;` on Windows and `:` on Linux/macOS.

**Windows shortcut:** If `run_app.bat` is correctly configured, you can double-click it or run it from the terminal:

```powershell
.\run_app.bat
```

The script should execute from the project root and use the correct dependency classpath.

> The provided `run_app.sh` may require correction if it does not include `lib/*` during compilation and execution. Until fixed, use the manual Linux/macOS commands above.

## Login

### Administrator

According to the application implementation, the default administrator is created automatically on first startup through `UserDAO.ensureDefaultAdmin`.

Change the default administrator password before using the application in a real environment. Do not deploy the published demonstration credentials to a production system.

### Customer

1. Open the application.
2. Select **Create Account**.
3. Register a new customer account.
4. Continue to the customer dashboard.

## Demonstration Workflow

### Customer workflow

```text
Welcome
  → Login / Register
  → Customer Dashboard
  → Browse Vehicles
  → Select Vehicle
  → Choose Rental Dates
  → Confirm Booking
  → My Bookings
  → Cancel / Return Vehicle
  → Profile
  → Logout
```

### Administrator workflow

```text
Admin Login
  → Admin Dashboard
  → Manage Vehicles
  → Customers
  → Rentals
  → Payments
  → Maintenance
  → Logout
```

The application uses one main Swing window, `AppFrame`, with `CardLayout` to display different screens as panels. Confirmation dialogs are used for actions requiring user approval.

## Key Business Rules

### Rental calculations

The backend recalculates the rental total using the rental duration, daily vehicle price, and applicable vehicle-specific additional charges. The frontend does not directly determine the final amount.

Conceptually:

```text
Rental Total = Rental Days × Daily Rate
             + Applicable Extra Charges
```

The exact calculation depends on the implemented pricing rules.

### Booking conflicts

`RentalDAO.hasConflict()` checks for overlapping confirmed bookings before creating a new reservation. This is intended to prevent double-booking.

For reliable concurrency protection, the database operation must also handle simultaneous booking attempts safely.

### Password handling

The application uses SHA-256 hashing according to the current implementation. Hashing prevents passwords from being stored as plain text, but **plain SHA-256 is not recommended for production password storage**. A dedicated password-hashing algorithm such as Argon2id or bcrypt with appropriate parameters is preferable.

### Role-based access

Customer and administrator screens are separated by role. `AppFrame.navigate` checks whether the signed-in role is allowed to access a screen.

The same permissions must also be enforced in service or database-access operations wherever authorization is required; hiding a screen alone is not sufficient security.

### Vehicle lifecycle

- Vehicles can be deactivated instead of permanently deleted, preserving rental history.
- Rented or maintenance vehicles cannot be deactivated.
- Maintenance cannot be added to rented or inactive vehicles.
- Completing maintenance can return a vehicle to the available state, subject to the application's status rules.

### Payments

The current implementation records Cash payment entries for payment at vehicle pickup. UPI and Card processor implementations demonstrate interfaces and polymorphism, but the application does not implement actual online payment processing.

The Payments screen displays payment records rather than processing online transactions.

## Troubleshooting

| Problem | Possible solution |
|---|---|
| Connection problem at startup | Check `.env`, database availability, credentials, and network access. |
| `DB_URL`, `DB_USER`, and `DB_PASSWORD` must be set | Ensure the required keys exist and are read from the correct `.env` file. |
| MySQL JDBC driver not found | Confirm the connector JAR exists in `lib/` and use `lib/*` in the classpath. |
| `Unknown database 'vehicle_rental_db'` | Create the database and import the schema. |
| Application does not appear | Run it in a graphical desktop environment; Swing requires a display. |
| `.env` values are not loaded | Run from the project root and check the dotenv configuration. |
| Compilation errors involving external classes | Verify that both dependency JARs are present and the classpath is correct. |
| Database connection fails with SSL errors | Check the provider's SSL requirements and connection URL. |

## Future Improvements

Potential improvements include:

- Migrating password storage to bcrypt or Argon2id.
- Adding stronger database-level protection against concurrent booking conflicts.
- Introducing automated unit and integration tests.
- Improving error logging and database transaction handling.
- Adding actual payment gateway integration.
- Packaging the application for easier installation.
- Adding reports and rental analytics.

## Project Status

The project has been compiled successfully with JDK 21, according to the development verification performed.

Compilation confirms that the source code compiles with the configured dependencies; database connectivity, runtime behavior, and all business rules should be tested separately.

## License

Add a license file if you intend to distribute this project publicly. Until a license is specified, reuse and redistribution are subject to the applicable copyright rules.
