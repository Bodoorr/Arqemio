# Arqemio

Arqemio is a construction project management platform designed to help companies manage projects, teams, tasks, equipment, expenses, and progress updates in one centralized system.

The application focuses on improving coordination, transparency, and resource management while providing secure access based on company membership and user roles.

## Frontend & Live Application

Arqemio includes a separate frontend application connected to the backend API.

**Frontend Repository:** [View Frontend Repository](https://github.com/Bodoorr/Arqemio-frontend)  
**Deployed Application:** [Open Arqemio](DEPLOYED_LINK)

## Main Features

- Multi-company project management
- Platform Admin, Owner, Manager, and Worker roles
- Invitation-based onboarding
- JWT authentication and Spring Security
- Project, task, equipment, reservation, and expense management
- Manager and worker project assignments
- Project updates with Cloudinary image uploads
- 24-hour customer project preview links
- Email and real-time SSE notifications
- Search, filtering, pagination, and sorting
- Audit logging and API rate limiting
- Soft deletion and database seeding
- Swagger/OpenAPI documentation

## Technologies

**Backend:** Java, Spring Boot, Spring Security, Spring Data JPA, Hibernate, Maven  
**Frontend:** React, Vite, JavaScript, Tailwind CSS, Axios, React Router  
**UI Notifications:** Sonner (Toast Notifications)  
**Database:** PostgreSQL  
**Security:** JWT  
**Storage:** Cloudinary  
**Notifications:** Spring Mail, Server-Sent Events (SSE)  
**Documentation & Testing:** Swagger/OpenAPI, Postman, JUnit  
**Development:** IntelliJ IDEA, Git, GitHub

## Architecture

Arqemio follows a layered architecture:

```text
React Frontend (Vite + Tailwind CSS)
              |
              | Axios / HTTP Requests
              v
Spring Boot REST Controllers
              |
              v
           Services
              |
              v
         Repositories
              |
              v
          PostgreSQL
```

- **Models** represent the application entities.
- **Repositories** handle database operations using Spring Data JPA.
- **Services** contain business rules and authorization logic.
- **Controllers** expose the REST API.
- **DTOs** control API request and response data.
- **Security** handles JWT authentication and protected endpoints.
- **Frontend** provides role-based dashboards and pages using React.
- **Tailwind CSS** handles responsive styling and interface design.
- **Axios** connects the frontend to the backend API.
- **React Router** handles frontend navigation.
- **Sonner** displays toast notifications for user actions and errors.

## General Approach

I started by designing the ERD, relationships, user roles, and core project workflow before implementing the API.

Arqemio was developed feature by feature using a layered architecture. Company roles are stored through memberships, allowing users to belong to different companies while maintaining company data isolation. Spring Security and JWT protect private endpoints, while additional authorization checks control access to company and project resources.

The project was later extended with equipment reservations, customer project sharing, Cloudinary uploads, email notifications, SSE notifications, audit logs, rate limiting, search, pagination, and Swagger documentation.

The frontend was developed separately using React and Vite, with Tailwind CSS for styling. Axios handles API communication, React Router manages navigation, and Sonner provides toast notifications. Separate dashboards were created for different user roles to display relevant information and features.

## User Stories

User stories were documented during the planning stage to define the main application requirements and expected user interactions.

[View User Stories on Trello](https://trello.com/b/RXTdTXRk/arqemio)

## API Documentation

Interactive API documentation is available through Swagger after starting the backend:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to explore and test the available API endpoints. Protected endpoints require JWT authentication.

Use the **Authorize** button with a valid JWT to access protected endpoints.

## ERD

The ERD defines the relationships between users, companies, memberships, projects, tasks, resources, and other application entities.

![Arqemio ERD](https://i.imgur.com/vrjyFNz.png)

## Planning

Project development was planned and tracked using Trello.

The planning board includes:

- User stories
- Project scope
- Deliverables
- Timeline
- Development tasks
- Progress tracking

[View Trello Planning Board](https://trello.com/b/RXTdTXRk/arqemio)

## API Endpoint Reference

The following table provides an overview of the Arqemio REST API.

For detailed request bodies, responses, parameters, and testing, see the Swagger/OpenAPI documentation.

The endpoints below are based on the application's controllers. Access permissions are enforced by the backend security and service layers.

### Authentication & User Profiles

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/auth/users/login` | User login |
| POST | `/auth/users/forget-password` | Request password reset |
| POST | `/auth/users/reset-password` | Reset password |
| GET | `/users/profile` | Get current user profile |
| PATCH | `/users/profile` | Update current user profile |
| PATCH | `/users/change-password` | Change password |
| GET | `/users/companies/{companyId}/employees` | Get company employee profiles |
| PATCH | `/users/companies/{companyId}/employees/{employeeId}/profile` | Update employee profile and optional picture |

### Invitations

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/invitation` | Send company invitation |
| GET | `/invitation/validate/{token}` | Validate invitation token |
| POST | `/invitation/register` | Register invited user |
| POST | `/invitation/accept/{token}` | Accept invitation |
| DELETE | `/invitation/cancel/{invitationId}` | Cancel invitation |
| PUT | `/invitation/resend/{invitationId}` | Resend invitation |

### Companies

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company` | Create company |
| GET | `/company` | Get all accessible companies |
| GET | `/company/{companyId}` | Get company details |

### Projects

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/projects` | Create project |
| GET | `/company/projects` | Get accessible projects |
| GET | `/company/projects/search` | Search, filter, paginate, and sort projects |
| GET | `/company/projects/{projectId}` | Get project details |
| PUT | `/company/projects/{projectId}` | Update project |
| DELETE | `/company/projects/{projectId}` | Archive project |
| POST | `/company/projects/{projectId}/managers/{membershipId}` | Assign manager to project |
| DELETE | `/company/projects/{projectId}/managers/{membershipId}` | Remove manager from project |
| POST | `/company/projects/{projectId}/workers/{membershipId}` | Assign worker to project |
| DELETE | `/company/projects/{projectId}/workers/{membershipId}` | Remove worker from project |
| GET | `/company/projects/{projectId}/members` | Get project members |
| GET | `/company/projects/{projectId}/available-members` | Get available project members |

### Tasks & Assignments

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/projects/tasks` | Create task |
| GET | `/company/projects/tasks` | Get tasks |
| GET | `/company/projects/tasks/{taskId}` | Get task details |
| PUT | `/company/projects/tasks/{taskId}` | Update task |
| DELETE | `/company/projects/tasks/{taskId}` | Archive task |
| PATCH | `/company/projects/tasks/{taskId}/status` | Update task status |
| POST | `/company/projects/tasks/{taskId}/assignments` | Assign worker to task |
| DELETE | `/company/projects/tasks/{taskId}/assignments/{workerMembershipId}` | Remove worker assignment |

### Equipment

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/equipments` | Create equipment |
| GET | `/company/equipments` | Get equipment |
| GET | `/company/equipments/{equipmentId}` | Get equipment details |
| PUT | `/company/equipments/{equipmentId}` | Update equipment |
| DELETE | `/company/equipments/{equipmentId}` | Archive equipment |

### Equipment Reservations

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/equipments/reservations` | Create reservation |
| GET | `/company/equipments/reservations` | Get reservations |
| GET | `/company/equipments/reservations/{reservationId}` | Get reservation details |
| PUT | `/company/equipments/reservations/{reservationId}` | Update reservation |
| DELETE | `/company/equipments/reservations/{reservationId}` | Cancel reservation |
| PATCH | `/company/equipments/reservations/{reservationId}/status` | Update reservation status |
| GET | `/company/equipments/reservations/availability/{equipmentId}` | Check available equipment quantity for a date range |

### Expenses

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/projects/expenses` | Create expense |
| GET | `/company/projects/expenses` | Get expenses |
| GET | `/company/projects/expenses/{expenseId}` | Get expense details |
| PUT | `/company/projects/expenses/{expenseId}` | Update expense |
| DELETE | `/company/projects/expenses/{expenseId}` | Archive expense |

### Project Updates

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/projects/updates` | Create project progress update |
| GET | `/company/projects/updates/project/{projectId}` | Get updates for a project |
| GET | `/company/projects/updates/{updateId}` | Get project update details |
| PUT | `/company/projects/updates/{updateId}` | Update project progress |
| DELETE | `/company/projects/updates/{updateId}` | Archive project update |
| PATCH | `/company/projects/updates/{updateId}/review` | Review project update |

### File Attachments

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/company/projects/attachments` | Upload file attachment |
| GET | `/company/projects/attachments/{attachmentId}` | Get attachment details |
| GET | `/company/projects/attachments/project/{projectId}` | Get project attachments |
| GET | `/company/projects/attachments/update/{projectUpdateId}` | Get project update attachments |
| GET | `/company/projects/attachments/expense/{expenseId}` | Get expense attachments |
| DELETE | `/company/projects/attachments/{attachmentId}` | Archive attachment |

### Customer Project Sharing

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/project-share/{updateId}` | Share project update with customer |
| GET | `/project-share/customer/{token}` | View shared project update |
| GET | `/project-share/register/{token}` | Get customer registration email |
| POST | `/project-share/register/{token}` | Register customer |
| GET | `/project-share/my-updates` | Get customer's shared updates |

### Notifications & Audit Logs

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| GET | `/notifications/subscribe` | Subscribe to real-time SSE notifications |
| GET | `/audit-logs/admin` | Get platform audit logs |
| GET | `/audit-logs/company/{companyId}` | Get company audit logs |

### Email Testing

| Request Type | URL | Functionality |
| ------------ | --- | ------------- |
| POST | `/api/test/emails/send` | Send test email |
| POST | `/api/test/emails/sendMailWithAttachment` | Send test email with attachment |

> This table provides a quick reference to the endpoints defined in the supplied controllers. Full interactive documentation, including request bodies, parameters, response models, and available endpoints, is available through Swagger/OpenAPI.

## Installation

### Prerequisites

Install the following tools:

- Java JDK compatible with the project
- Maven
- PostgreSQL
- Node.js and npm
- Git
- IntelliJ IDEA or another IDE

### 1. Clone the Repository

Clone the backend repository:

```bash
git clone https://github.com/Bodoorr/Arqemio.git
cd Arqemio
```

Clone the frontend repository separately:

```bash
git clone https://github.com/Bodoorr/Arqemio-frontend.git
cd Arqemio-frontend
```

### 2. Configure PostgreSQL

Start PostgreSQL and create a database for Arqemio.

Example:

```sql
CREATE DATABASE arqemio;
```

Configure the database connection in the Spring Boot application configuration.

### 3. Configure Environment Variables

Configure the required environment variables according to the backend configuration.

These may include:

- PostgreSQL database URL
- PostgreSQL username and password
- JWT secret
- Email SMTP credentials
- Cloudinary credentials
- Frontend application URL

Do not commit passwords, API keys, or other secrets to GitHub.

### 4. Seed the Database

Start the backend application to execute its configured database initialization process.

Arqemio includes database seeding for the initial platform administrator.

### 5. Start the Backend

From the backend project directory:

```bash
./mvnw spring-boot:run
```

The API is available locally at:

```text
http://localhost:8080
```

### 6. Start the Frontend

From the frontend project directory:

```bash
npm install
npm run dev
```

The frontend is available locally at:

```text
http://localhost:5173
```

Ensure that the frontend API configuration points to the running backend.

### 7. Access the API

Use Postman or Swagger to test the API endpoints.

For protected endpoints, log in and provide a valid JWT token.

### 8. Access Swagger/OpenAPI

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

## Major Challenges

One of the main challenges was designing **multi-company authorization**. Roles are stored in `CompanyMembership` rather than directly on the user, allowing one user to belong to multiple companies while keeping company data separated.

Another challenge was securely sharing project progress with customers without requiring customer accounts. This was handled using unique public tokens that expire after 24 hours.

Equipment reservation conflicts and project-level permissions also required additional business rules to prevent unauthorized access and resource conflicts.

### Handling Concurrent Equipment Reservations

A major technical challenge was preventing multiple requests from reserving the same equipment at the same time.

The reservation implementation uses synchronization with `ReentrantLock`, database-level locking, and transactional operations to help prevent conflicting reservations and incorrect equipment availability.

### Concurrent Task Assignment Notifications

Another challenge was sending notifications when multiple workers are assigned to tasks.

An `ExecutorService` with a fixed thread pool is used to process worker notifications asynchronously, allowing multiple notification tasks to be handled without blocking the main assignment workflow.


## Document a Race Condition

### Scenario: Concurrent Equipment Reservations

A race condition can occur when two users attempt to reserve the same equipment at the same time.

To demonstrate this scenario, I used two manager accounts to reserve the same equipment with the following details:

- **Equipment:** Excavator - Concurrency Test
- **Total Quantity:** 1
- **Project:** Arqemio Concurrency Test
- **Start Date:** 29 October 2026, 08:20 AM
- **End Date:** 30 October 2026, 09:00 AM
- **Requested Quantity:** 1 per manager

Both managers attempted to create reservations for the same equipment and overlapping time period.

### Solution

The reservation service uses:

- **ReentrantLock:** Synchronizes reservation operations within the application.
- **Database Locking:** Protects equipment availability during concurrent database operations.
- **@Transactional:** Maintains database consistency.
- **Availability Validation:** Prevents reservations from exceeding the available equipment quantity.

### Test Evidence

**1. Two Managers Attempting to Reserve the Same Equipment**

Both managers selected the same excavator, project, dates, and quantity.

![Two Reservation Requests](https://i.imgur.com/4HCGFgZ.png)

**2. Successful Reservation**

One manager successfully reserved the excavator.

![Successful Reservation](https://i.imgur.com/jdR24PI.png)

**3. Rejected Reservation**

The other manager received the following error:

> Reservation failed! Not enough Excavator - Concurrency Test available. Only 0 remaining.

![Rejected Reservation](https://i.imgur.com/ryHDl86.png)

### Results

The application successfully prevented the second reservation from exceeding the available equipment quantity.

- One reservation was successful.
- The other reservation was rejected.
- The available quantity was reduced to zero.
- No equipment overbooking was observed.

This demonstrates that the reservation availability checks prevent conflicting bookings in the tested scenario.


### Concurrency: Task Assignment Notifications

Arqemio supports assigning multiple workers to the same task.

The frontend uses `Promise.allSettled()` to submit multiple worker assignment requests concurrently.

The backend uses an `ExecutorService` with a fixed thread pool of 3 threads to process notifications asynchronously.

#### Testing

1. Logged in as two different workers in separate browser sessions.
2. Selected both workers for the same task.
3. Clicked "Assign 2 Workers".
4. Verified that the backend processed notifications using separate threads.
5. Confirmed that both workers received assignment notifications.

#### Screenshots

**1. Worker 1 Dashboard**

![Worker 1 Dashboard](https://i.imgur.com/MObtK39.png)

**2. Worker 2 Dashboard**

![Worker 2 Dashboard](https://i.imgur.com/hIpEkjE.png)

**3. Selecting Two Workers**

![Selecting Two Workers](https://i.imgur.com/chtWnE0.png)

**4. Notifications Received by Both Workers**

![Worker Notifications](https://i.imgur.com/3gBm8cC.png)

#### Results

- Both worker assignments succeeded.
- Notifications were processed by two different threads.
- Both workers received assignment notifications.
- The task appeared in both workers' dashboards.

This demonstrates concurrent request handling and asynchronous notification processing using Java's `ExecutorService`.

## Unsolved Problems

- SSE notifications require further frontend integration and testing.
- Not every backend feature is currently exposed through the frontend.
- Automated test coverage can be expanded.
- The current rate limiter uses application memory and can be improved for production environments.

## Future Improvements

- Advanced project analytics and dashboards
- Gantt charts and project timelines
- Budget forecasting and financial reports
- Improved real-time notifications
- More automated testing
- Mobile application support
- Redis-based rate limiting
- Additional reporting and export features

## Resources & Credits

The following documentation and learning resources were referenced during the development of Arqemio:

#### Spring Boot & API Development

- [MDN Web Docs - HTTP Response Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status)
- [GeeksforGeeks - Exception Handling in Spring Boot](https://www.geeksforgeeks.org/springboot/exception-handling-in-spring-boot/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/reference/)
- [DEV Community - Handling Validation Errors Globally in Spring Boot](https://dev.to/realnamehidden1_61/how-do-you-handle-validation-errors-globally-in-spring-boot-1mg2)

#### Authentication & Security

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JJWT - Java JWT](https://github.com/jwtk/jjwt)
- [Baeldung - OpenAPI JWT Authentication](https://www.baeldung.com/openapi-jwt-authentication)

#### Email & Notifications

- [GeeksforGeeks - Sending Email via SMTP in Spring Boot](https://www.geeksforgeeks.org/springboot/spring-boot-sending-email-via-smtp/)
- [Baeldung - Server-Sent Events in Spring](https://www.baeldung.com/spring-server-sent-events)

#### File & Image Upload

- [Cloudinary - Java Quick Start](https://cloudinary.com/documentation/java_quickstart)
- [Cloudinary - Upload Images Using Java](https://cloudinary.com/blog/questions/how-to-upload-images-using-java)

#### Search, Filtering, Pagination & Sorting

- [GeeksforGeeks - Search and Filtering with React & Spring Boot](https://www.geeksforgeeks.org/advance-java/search-and-filtering-with-react-spring-boot-integration/)
- [GeeksforGeeks - Pagination and Sorting with Spring Data JPA](https://www.geeksforgeeks.org/advance-java/pagination-and-sorting-with-spring-data-jpa/)
- [DEV Community - Pagination and Filtering with Spring Boot](https://dev.to/devcorner/pagination-and-filtering-spring-boot-1c7a)

#### Swagger & API Documentation

- [GeeksforGeeks - Spring Boot REST API Documentation Using Swagger](https://www.geeksforgeeks.org/springboot/spring-boot-rest-api-documentation-using-swagger/)
- [Springdoc OpenAPI](https://springdoc.org/)

#### Rate Limiting

- [GeeksforGeeks - Implementing Rate Limiting in Spring Boot](https://www.geeksforgeeks.org/advance-java/implementing-rate-limiting-in-a-spring-boot-application/)

#### Database & Seeding

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [PostgreSQL - Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
- [Baeldung - Spring Boot data.sql and schema.sql](https://www.baeldung.com/spring-boot-data-sql-and-schema-sql)

## Acknowledgements

A special thanks to **Saad Iqbal** for the guidance and support throughout the development of Arqemio.
