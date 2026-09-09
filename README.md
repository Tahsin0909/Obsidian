# Obsidian — Modern Library Management System

[![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-5382a1?style=for-the-badge)](https://en.wikipedia.org/wiki/Swing_(Java))
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=for-the-badge)](#)

**Obsidian** is a clean, modern, and modular desktop Library Management System built in pure Java and Java Swing. It provides a comprehensive administration portal for librarians as well as a self-service dashboard for library members and students, all without requiring external database installations or third-party runtime dependencies.

---

## Table of Contents
- [Highlights & Architecture](#highlights--architecture)
- [System Features](#system-features)
  - [Administrator Dashboard](#administrator-dashboard)
  - [Member / Student Dashboard](#member--student-dashboard)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites](#prerequisites)
- [Build and Run](#build-and-run)
  - [Windows](#windows)
  - [macOS / Linux](#macos--linux)
- [Demo Credentials](#demo-credentials)
- [Core OOP Concepts Demonstrated](#core-oop-concepts-demonstrated)
- [License](#license)

---

## Highlights & Architecture

- **Clean Modular UI Architecture**: The user interface decomposes complex workflows into single-responsibility panels (`com.library.ui.admin` and `com.library.ui.member`) coordinated via the Mediator design pattern.
- **Polymorphic Role-Based Access Control (RBAC)**: A single authentication gateway dispatches dynamically to distinct role-based experiences for `Admin` and `Member` accounts.
- **Modern Look & Feel**: Customized `UITheme` with smooth typography, harmonious color palettes, rounded borders, card-based KPI summaries, and responsive table sorters.
- **Zero Heavy Setup**: Operates with an in-memory, thread-safe `DataStore` pre-seeded with books, members, loans, and fines for immediate out-of-the-box evaluation.
- **Security & Integrity**: Password hashing and verification via `PasswordUtil`, plus safety validation preventing deletion of books currently on loan.

---

## System Features

### Administrator Dashboard

The Admin Dashboard provides total oversight over library resources and circulation workflows:

- **Executive KPI Strip**:
  - Live counts for Total Books, Available Copies, Active Borrowings, Overdue Loans, and Total Fines Assessed.
- **Book Inventory Management (`AdminBooksPanel`)**:
  - Add, update, and search books across ISBN, Title, Author, Category, Publisher, and Year.
  - Active loan safety check preventing accidental deletion of borrowed books.
- **Member Directory (`AdminMembersPanel`)**:
  - Register new members and modify existing contact profiles.
  - Quick-view shortcut to inspect fine history and balances for any selected member.
- **Circulation Management (`AdminBorrowingsPanel`)**:
  - Issue books with automatic quantity deduction and return loans with stock restoration.
  - Filter by All, Active, and Overdue borrowings.
  - Automatic overdue day calculation and direct transition to fine assessment upon return.
- **Fine & Fee Management (`AdminFinesPanel`)**:
  - Track Outstanding Balance vs. Total Settled revenue.
  - Auto-Assess Overdue fines across all active loans in a single click.
  - Calculate custom fines ($1.00/day default rate).
  - Settle payments, waive fees with audit notes, and inspect detailed digital receipts.

### Member / Student Dashboard

The Member Dashboard provides a personalized self-service experience:

- **Personal Metrics Strip**:
  - Real-time tracker for My Borrowed Books, Available in Library, Outstanding Fines, and Membership Status.
- **My Borrowed Books (`MemberBorrowingsPanel`)**:
  - View current and historical loans, borrow dates, due dates, countdown of remaining days, and overdue indicators.
  - Colored fine badges (Paid in green, Unpaid in red).
- **Browse Catalog (`MemberCatalogPanel`)**:
  - Real-time regex search across the entire library catalog with live stock status indicators.
- **My Fines & Fees (`MemberFinesPanel`)**:
  - Detailed summary of assessed vs. unpaid fines.
  - Self-service payment processing with payment methods (Card, Online Banking, Mobile Wallet / bKash, Cash).
  - Batch "Pay All Unpaid Fines" feature with instant receipt generation.
- **My Profile & Security (`MemberProfilePanel`)**:
  - Update personal information (Full Name, Username, Email, Phone, Address).
  - Duplicate check validation for username and email.
  - Secure password changes requiring current password verification and length validation.

---

## Project Directory Structure

```text
Obsidian/
├── src/main/
│   ├── java/com/library/
│   │   ├── Main.java                        # Application entry point
│   │   ├── model/                           # Domain entities
│   │   │   ├── Admin.java
│   │   │   ├── Book.java
│   │   │   ├── Borrowing.java
│   │   │   ├── Category.java
│   │   │   ├── Fine.java
│   │   │   ├── Member.java
│   │   │   ├── Reservation.java
│   │   │   └── User.java
│   │   ├── services/                        # Business logic & authentication
│   │   │   └── AuthService.java
│   │   ├── store/                           # Thread-safe in-memory data store
│   │   │   └── DataStore.java
│   │   ├── ui/                              # User interface & dashboards
│   │   │   ├── AdminDashboard.java          # Admin coordinator frame
│   │   │   ├── MemberDashboard.java         # Member coordinator frame
│   │   │   ├── LoginFrame.java              # Authentication window
│   │   │   ├── UIHelper.java                # Shared component & form utilities
│   │   │   ├── UITheme.java                 # Fonts, palettes, and button styles
│   │   │   ├── admin/                       # Modular Admin Panels
│   │   │   │   ├── AdminBooksPanel.java
│   │   │   │   ├── AdminBorrowingsPanel.java
│   │   │   │   ├── AdminFinesPanel.java
│   │   │   │   └── AdminMembersPanel.java
│   │   │   └── member/                      # Modular Member Panels
│   │   │       ├── MemberBorrowingsPanel.java
│   │   │       ├── MemberCatalogPanel.java
│   │   │       ├── MemberFinesPanel.java
│   │   │       └── MemberProfilePanel.java
│   │   └── util/                            # Security & cryptographic helpers
│   │       └── PasswordUtil.java
│   └── resources/
│       └── sideImage.jpg                    # Login branding illustration
├── bin/                                     # Compiled bytecode (.class)
├── compile.bat                              # Windows build script
├── run.bat                                  # Windows execution script
├── compile.sh                               # Unix build script
├── run.sh                                   # Unix execution script
└── README.md
```

---

## Prerequisites

- **Java Development Kit (JDK) 11** or newer (OpenJDK, Temurin, or Oracle JDK).
- Verify installation:
  ```bash
  javac -version
  java -version
  ```

---

## Build and Run

### Windows

1. **Compile**:
   ```cmd
   .\compile.bat
   ```
2. **Run**:
   ```cmd
   .\run.bat
   ```

### macOS / Linux

1. **Grant execution permissions**:
   ```bash
   chmod +x compile.sh run.sh
   ```
2. **Compile**:
   ```bash
   ./compile.sh
   ```
3. **Run**:
   ```bash
   ./run.sh
   ```

---

## Demo Credentials

The application initializes demo accounts in `DataStore` on startup:

| Role | Username / Email | Password | Description |
|---|---|---|---|
| **Administrator** | `admin` / `admin@mail.com` | `123456` | Full system administrative privileges |
| **Member** | `member` / `member@mail.com` | `123456` | Alice Rahman (Active normal loan) |
| **Member** | `member2` / `member2@mail.com` | `123456` | Bob Karim (Has overdue loan & unpaid fine) |
| **Member** | `member3` / `member3@mail.com` | `123456` | Chloe Islam (Has returned loan & paid fine) |

---

## Core OOP Concepts Demonstrated

- **Encapsulation**: Strict private fields, validated accessors/mutators, and encapsulated UI event handlers.
- **Inheritance & Polymorphism**: `Admin` and `Member` extend the abstract `User` class. `AuthService.login()` returns a polymorphic `User` reference that resolves dynamically at runtime.
- **Mediator Pattern**: `AdminDashboard` and `MemberDashboard` act as coordinators, orchestrating events across modular sub-panels (`Books`, `Members`, `Borrowings`, `Fines`, `Profile`) without tight coupling.
- **Singleton Pattern**: `DataStore.getInstance()` ensures a single, globally synchronized source of truth during runtime.

---

## License

This project is licensed under the [MIT License](LICENSE).