# Library Management System
## 1. System Definition

The Library Management System is a RESTful web application developed using Java and Spring Boot that provides digital management of a library’s resources.
The system enables librarians to manage books, authors, and categories, while allowing library members to borrow, return, and reserve books.
All data is persisted in a relational database, and the application exposes REST endpoints that can be consumed using tools such as Postman or a graphical user interface.

## 2. Business Requirements

#### The system should allow librarians to add, retrieve, and delete books from the library catalog.
#### The system should store and manage information about book authors.
#### The system should allow books to be categorized by a specific category.
#### The system should allow users to be registered with different roles (librarian or member).
#### The system should allow users to view all books available in the library.
#### The system should allow users to borrow books if copies are available.
#### The system should restrict users from borrowing more books than their defined borrowing limit.
#### The system should track book loans including borrowing date, due date, and return date.
#### The system should allow users to return borrowed books and update availability.
#### The system should allow users to reserve books that are currently unavailable.

## 3. MVP (Minimum Viable Product) Features

Based on the business requirements, the MVP version of the Library Management System includes the following five core features:

## Feature 1: Book Catalog Management

The system allows librarians to manage the library’s book catalog. Books can be added, retrieved, and deleted through REST endpoints.
Each book contains information such as title, ISBN, total copies, available copies, category, and associated authors.

## Feature 2: User Management

The system supports managing library users. Each user has a name, email address, role (librarian or member), and a maximum borrowing limit.
Users can be created and retrieved through REST endpoints.

## Feature 3: Borrowing and Returning Books

Library members can borrow books if copies are available and their borrowing limit has not been exceeded.
The system records the borrowing date and due date when a book is borrowed and updates the return date when the book is returned.
Book availability is updated automatically.

## Feature 4: Book Reservation

The system allows users to reserve books that are currently unavailable.
Users can view their reservations and cancel them if necessary.
Each reservation has a status that reflects its current state (active, cancelled, or completed).

## Feature 5: Loan Tracking

The system tracks all active book loans.
It provides functionality to retrieve a list of books that are currently borrowed and not yet returned.
This feature supports monitoring library usage and serves as a foundation for future enhancements such as overdue detection.
