# ⚡ EV Charging Station Slot Booking and Billing System

**SIMATS ENGINEERING**  
**Department of Computer Science and Engineering**  
**CSA0926 – Java Programming | Assignment Solution**

A comprehensive, multithreaded Java Desktop application featuring **Swing GUI**, **Object-Oriented Design (OOP)**, **Collection Framework**, **Concurrency & Thread Synchronization**, and **JDBC MySQL Database persistence**.

---

## 🎨 Design System & Color Palette

The user interface features a modern dark theme inspired by modern developer tooling:

| Role / Element | Color Name | HEX Code | RGB |
|---|---|---|---|
| **Primary Header & Footer** | Deep Slate | #0F172A | 
gb(15, 23, 42) |
| **Text White / Highlights** | Pure White Mist | #F8FAFC | 
gb(248, 250, 252) |
| **Body / Main Background** | Dark Slate Panel | #1E293B | 
gb(30, 41, 59) |
| **Output / Console Box** | Slate Console | #334155 | 
gb(51, 65, 85) |
| **Text & Field Labels** | Soft Slate Lavender | #E2E8F0 | 
gb(226, 232, 240) |
| **Book Slot Action Button** | Cyan / Sky Blue | #0EA5E9 | 
gb(14, 165, 233) |
| **Bill & Navigation Action Buttons** | Royal Indigo | #6366F1 | 
gb(99, 102, 241) |

---

## 🎯 Course Outcome Realization

| CO | Requirement | Implementation Detail |
|---|---|---|
| **CO 1** | OOP – Classes, Encapsulation, Inheritance, Polymorphism | User (abstract base), Customer, Vehicle, ChargingStation, ChargingSlot, Booking, Bill |
| **CO 2** | Collection Framework, Generics, Iterators | ArrayList and HashMap in ChargingManager for high-performance station, slot, and booking lookups |
| **CO 3** | Multithreading, Synchronization, Custom Exceptions | BookingProcessor (Runnable), synchronized ookSlot() to prevent double-booking, SlotUnavailableException, InvalidBookingException |
| **CO 4** | GUI, Events, Layout Managers | Rich Swing GUI (EVChargingGUI) with custom gradients, anti-aliased curved buttons, BorderLayout, GridLayout, and interactive booking & billing |
| **CO 5** | JDBC – INSERT, UPDATE, DELETE, SELECT | DAO layer (DBConnection, StationDAO, BookingDAO, BillDAO) with MySQL connection |

---

## 📁 Project Architecture & Package Structure

`
EV_Charging_Station/
├── src/
│   └── evcharging/
│       ├── model/          # OOP entities: User, Customer, Vehicle, Station, Slot, Booking, Bill
│       ├── exception/      # Custom exceptions: SlotUnavailableException, InvalidBookingException
│       ├── collection/     # In-memory manager: ChargingManager (Map + synchronized methods)
│       ├── concurrent/     # Multithreading: BookingProcessor (Runnable)
│       ├── dao/            # JDBC persistence: DBConnection, StationDAO, BookingDAO, BillDAO
│       ├── gui/            # Modern Swing GUI: EVChargingGUI with custom styled components
│       └── app/            # Application entry point: EVChargingApp
├── ev_charging_db.sql      # Complete MySQL database schema & sample seed data
├── SIMATS ENGINEERING.docx # Original assignment problem statement & design specification
├── run.bat                 # One-click compilation and execution script (Windows)
└── README.md               # Documentation
`

---

## 🔒 Concurrency & Thread-Safety

- Each booking request executes on an independent Thread via BookingProcessor (implements Runnable).
- ChargingManager.bookSlot() is synchronized to enforce atomic slot status transitions.
- Prevents race conditions and double bookings when multiple users simultaneously attempt to reserve the same slot.
- Custom exceptions (InvalidBookingException and SlotUnavailableException) safeguard state validity.

---

## 🗄️ Database Design (MySQL)

Execute ev_charging_db.sql in MySQL to initialize the schema:

- **users**: User identification, email, credentials.
- **ehicle**: Linked to users via foreign key k_vehicle_user.
- **charging_station**: Station information, location, charger type, rate per kWh.
- **charging_slot**: Slots associated with stations, timeslot, and availability status.
- **ookings**: Records customer, vehicle, station, slot, date, and status.
- **ills**: Units consumed, charging cost, booking fee, GST/tax, total payable amount, and payment status.

---

## 🚀 How to Compile & Run

### Prerequisites
- Java Development Kit (JDK 17 or higher recommended)
- MySQL Server (optional for GUI demo; app includes built-in in-memory fallback for instant execution)

### Method 1: Double-Click Run Script (Windows)
Double-click **
un.bat** in the EV_Charging_Station folder to compile and launch.

### Method 2: Command Line
`cmd
# 1. Navigate to the EV_Charging_Station directory
cd EV_Charging_Station

# 2. Compile all source files
javac -d out -sourcepath src src/evcharging/app/EVChargingApp.java

# 3. Run the application
java -cp out evcharging.app.EVChargingApp
`
