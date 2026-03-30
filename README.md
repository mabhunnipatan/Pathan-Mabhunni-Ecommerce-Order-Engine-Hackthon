# Distributed E-Commerce Order Engine

> Hackathon Technical Assessment — Backend CLI Simulation

---

## Project Overview

A robust, menu-driven CLI application that simulates a real-world distributed e-commerce backend engine.
Inspired by large-scale platforms like Amazon, Flipkart, and Meesho, this system handles inventory conflicts,
concurrent users, payment failures, transaction rollbacks, and event-driven order processing — all in pure Java
with zero external dependencies.

---

## Features Implemented

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Product Management** | Add products, prevent duplicate IDs, update stock, view catalog |
| 2 | **Multi-User Cart System** | Each user has a separate cart; add, remove, update quantity |
| 3 | **Real-Time Stock Reservation** | Stock is reserved on add-to-cart, released on remove |
| 4 | **Concurrency Simulation** | Multiple threads compete for the same stock; logical locking ensures only valid reservations succeed |
| 5 | **Order Placement Engine** | Atomic 5-step process: validate → calculate → lock stock → create order → clear cart |
| 6 | **Payment Simulation** | 70% success rate by default; configurable failure mode |
| 7 | **Transaction Rollback** | If any step fails, all previous steps are undone (stock restored, order marked FAILED) |
| 8 | **Order State Machine** | Valid transitions: CREATED → PENDING_PAYMENT → PAID → SHIPPED → DELIVERED; blocks invalid transitions |
| 9 | **Discount & Coupon Engine** | Auto-discount on total > ₹1000 (10%) and qty > 3 (extra 5%); coupons: SAVE10, FLAT200 |
| 10 | **Inventory Alert System** | Shows low-stock and out-of-stock products; prevents purchase if stock = 0 |
| 11 | **Order Management** | View all/my orders, search by ID, filter by status |
| 12 | **Order Cancellation Engine** | Cancel order + restore stock; edge case: cannot cancel an already cancelled order |
| 13 | **Return & Refund System** | Partial return supported; updates stock and order total; triggers refund |
| 14 | **Event-Driven System** | Simulated event queue: ORDER_CREATED → PAYMENT_SUCCESS → INVENTORY_UPDATED; failure stops chain |
| 15 | **Inventory Reservation Expiry** | Reserved stock expires after 5 minutes and is automatically released |
| 16 | **Audit Logging System** | Immutable, timestamped logs for every action (add, order, payment, return, etc.) |
| 17 | **Fraud Detection System** | Flags users placing 3+ orders/minute or placing high-value orders (> ₹5000) |
| 18 | **Failure Injection System** | Toggle random failures for payment, order creation, and inventory update |
| 19 | **Idempotency Handling** | Prevents duplicate orders if "Place Order" is triggered multiple times in same second |
| 20 | **Microservice Simulation** | Divided into ProductService, CartService, OrderService, PaymentService, etc. |

---

## Design Approach

### Architecture
```
Main.java                     ← Bootstrap & DI wiring
├── cli/MenuHandler.java      ← CLI menu loop & user input
├── service/                  ← Business logic (one service per domain)
│   ├── ProductService
│   ├── CartService
│   ├── InventoryService      ← Stock reservation & locking
│   ├── OrderService          ← Core order lifecycle
│   ├── PaymentService        ← Payment simulation
│   ├── CouponService         ← Discount & coupon logic
│   ├── FraudService          ← Fraud detection rules
│   ├── EventService          ← Event queue processing
│   ├── LoggingService        ← Immutable audit log
│   └── FailureService        ← Chaos/failure injection
├── repository/               ← In-memory data stores (ConcurrentHashMap)
├── model/                    ← Domain entities (Product, Cart, Order, …)
├── event/                    ← Event, EventType, EventQueue
└── util/                     ← IdGenerator, LockManager, TimeUtil, Constants
```

### Key Design Decisions

- **Loose Coupling**: Services depend on repositories and other services via constructor injection (manual DI).
- **Concurrency Safety**: `ConcurrentHashMap` for all repositories; `ReentrantLock` (via `LockManager`) per product ID for stock operations.
- **Atomic Order Placement**: Order placement follows a strict 5-step sequence. Any failure triggers full rollback.
- **Idempotency**: Orders are tagged with a key (userId + epoch-second); duplicate keys are rejected.
- **Immutable Audit Logs**: Logs are append-only (`ArrayList`) and exposed only as `Collections.unmodifiableList`.

---

## Assumptions

1. Persistence is in-memory (no database). All data resets on JVM restart.
2. Payment simulation has a 30% random failure rate unless failure mode is toggled.
3. "Concurrent users" simulation uses Java threads to demonstrate locking, not true distributed concurrency.
4. Reservation expiry (5 min) is checked lazily; a background timer was omitted to keep the CLI single-threaded.
5. The fraud high-value threshold is ₹5000 (configurable in `Constants.java`).
6. Users `USER_1`, `USER_2`, `USER_3` and 5 sample products are seeded on startup.

---

## How to Run

### Prerequisites
- Java 17+ (uses switch expressions and records)

### Compile
```bash
cd Ecommerce_Order_Engine/src
javac -d ../out $(find . -name "*.java")
```

### Run
```bash
cd ../out
java Main
```

### Quick Test Flow
```
1. View Products          → see seeded catalog
3. Add to Cart            → add PROD_1 qty=2
5. View Cart              → confirm items
6. Apply Coupon           → try SAVE10 or FLAT200
7. Place Order            → confirm with 'yes'
9. View Orders            → see your order history
8. Cancel Order           → cancel by order ID
11. Return Product        → partial/full return
12. Concurrent Users      → test stock locking
14. Trigger Failure Mode  → inject failures & retry
13. View Logs             → see full audit trail
```

### Switch Users
At any prompt type `u USER_2` to switch the active user (e.g., to test multi-user cart isolation).

---

## Project Structure

```
Ecommerce_Order_Engine/
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── Cart.java
│   │   ├── CartItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── User.java
│   │   ├── Payment.java
│   │   └── enums/
│   │       ├── OrderStatus.java
│   │       └── PaymentStatus.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── CartService.java
│   │   ├── InventoryService.java
│   │   ├── OrderService.java
│   │   ├── PaymentService.java
│   │   ├── CouponService.java
│   │   ├── FraudService.java
│   │   ├── LoggingService.java
│   │   ├── EventService.java
│   │   └── FailureService.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── CartRepository.java
│   │   ├── OrderRepository.java
│   │   └── UserRepository.java
│   ├── util/
│   │   ├── IdGenerator.java
│   │   ├── LockManager.java
│   │   ├── TimeUtil.java
│   │   └── Constants.java
│   ├── cli/
│   │   └── MenuHandler.java
│   └── event/
│       ├── Event.java
│       ├── EventType.java
│       └── EventQueue.java
└── README.md
```
