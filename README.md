E-Commerce Order Engine (Java CLI)

This project is a simple command-line based e-commerce backend simulation built using Java.

It shows how real applications like Amazon or Flipkart work internally, including cart, orders, payments, and inventory handling.

-> What this project does
Users can view products
Add products to cart
Place orders
Make payments (success/failure simulation)
Cancel or return orders

Everything runs in the console (CLI) without using any external libraries.

-> Main Features
Product Management
Add and view products, update stock
Cart System
Each user has their own cart
Stock Handling
Stock is reserved when added to cart

->Order Processing
Steps:

Validate → Lock Stock → Create Order → Payment → Clear Cart
Payment Simulation
Payment may fail randomly (to test real scenarios)
Rollback System
If anything fails → everything is undone

->Order Status Flow

CREATED → PAID → SHIPPED → DELIVERED
Discounts & Coupons
Auto discount + coupon codes like SAVE10
Concurrency
Multiple users can try to buy same product (handled safely)
Cancel & Return
Orders can be cancelled or returned, stock updates back
Logging
All actions are recorded
->Design
Uses service-based structure
Data stored in memory (no database)
Uses:
ConcurrentHashMap
ReentrantLock
->How to Run
Compile
javac -d ../out $(find . -name "*.java")
Run
java Main
