# Arqemio

Arqemio is a construction project management platform designed to help companies manage projects, teams, tasks, equipment, expenses, and progress updates in one centralized system.

The application focuses on improving coordination, transparency, and resource management while providing secure access based on company membership and user roles.

## Frontend & Live Application

Arqemio includes a separate frontend application connected to the backend API.

**Frontend Repository:** [View Frontend Repository](FRONTEND_REPO_LINK)  
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
**Database:** PostgreSQL  
**Security:** JWT  
**Storage:** Cloudinary  
**Notifications:** Spring Mail, Server-Sent Events (SSE)  
**Documentation & Testing:** Swagger/OpenAPI, Postman, JUnit  
**Development:** IntelliJ IDEA, Git, GitHub

## Architecture

Arqemio follows a layered architecture:

```text
Controller → Service → Repository → PostgreSQL
```

- **Models** represent the application entities.
- **Repositories** handle database operations using Spring Data JPA.
- **Services** contain business rules and authorization logic.
- **Controllers** expose the REST API.
- **DTOs** control API request and response data.
- **Security** handles JWT authentication and protected endpoints.

## General Approach

I started by designing the ERD, relationships, user roles, and core project workflow before implementing the API.

Arqemio was developed feature by feature using a layered architecture. Company roles are stored through memberships, allowing users to belong to different companies while maintaining company data isolation. Spring Security and JWT protect private endpoints, while additional authorization checks control access to company and project resources.

The project was later extended with equipment reservations, customer project sharing, Cloudinary uploads, email notifications, SSE notifications, audit logs, rate limiting, search, pagination, and Swagger documentation.

## API Documentation

Interactive API documentation is available through Swagger after starting the backend:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to explore and test the available API endpoints. Protected endpoints require JWT authentication.

Use the **Authorize** button with a valid JWT to access protected endpoints.

## ERD

The ERD defines the relationships between users, companies, memberships, projects, tasks, resources, and other application entities.

![Arqemio ERD](https://i.imgur.com/po8TVNX.png)

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

| Request Type | URL | Functionality | Access |
| ------------ | --- | ------------- | ------ |
| POST | `/auth/users/login` | User login | Public |
| POST | `/invitation` | Send company invitation | Admin / Owner |
| GET | `/invitation/validate/{token}` | Validate invitation token | Public |
| POST | `/auth/users/register` | Register using invitation | Public |
| PUT | `/auth/api/profile` | Update current user profile | Private |
| POST | `/auth/api/change-password` | Change password | Private |
| POST | `/auth/users/forgot-password` | Request password reset | Public |
| POST | `/auth/users/reset-password` | Reset password | Public |
| POST | `/company` | Create company | Platform Admin |
| GET | `/company` | Get companies | Private |
| GET | `/company/{companyId}` | Get company details | Private |
| PUT | `/company/{companyId}` | Update company | Admin / Owner |
| DELETE | `/company/{companyId}` | Archive company | Admin / Owner |
| GET | `/company/members` | Get company members | Private |
| PUT | `/company/members/{membershipId}` | Update company member | Owner |
| DELETE | `/company/members/{membershipId}` | Remove company member | Owner |
| POST | `/company/projects` | Create project | Admin / Owner |
| GET | `/company/projects` | Get accessible projects | Private |
| GET | `/company/projects/{projectId}` | Get project details | Private |
| PUT | `/company/projects/{projectId}` | Update project | Admin / Owner |
| DELETE | `/company/projects/{projectId}` | Archive project | Admin / Owner |
| POST | `/company/projects/{projectId}/managers/{membershipId}` | Assign manager to project | Admin / Owner |
| DELETE | `/company/projects/{projectId}/managers/{membershipId}` | Remove manager from project | Admin / Owner |
| POST | `/company/projects/{projectId}/workers/{membershipId}` | Assign worker to project | Admin / Owner |
| DELETE | `/company/projects/{projectId}/workers/{membershipId}` | Remove worker from project | Admin / Owner |
| GET | `/company/projects/search` | Search, filter, paginate and sort projects | Private |
| POST | `/tasks` | Create project task | Admin / Manager |
| GET | `/tasks` | Get project tasks | Private |
| GET | `/tasks/{taskId}` | Get task details | Private |
| PUT | `/tasks/{taskId}` | Update task | Admin / Manager |
| DELETE | `/tasks/{taskId}` | Delete task | Admin / Manager |
| POST | `/tasks/{taskId}/workers/{membershipId}` | Assign worker to task | Admin / Manager |
| DELETE | `/tasks/{taskId}/workers/{membershipId}` | Remove worker from task | Admin / Manager |
| POST | `/equipment` | Create equipment | Admin / Owner |
| GET | `/equipment` | Get equipment | Private |
| PUT | `/equipment/{equipmentId}` | Update equipment | Admin / Owner |
| DELETE | `/equipment/{equipmentId}` | Archive equipment | Admin / Owner |
| POST | `/reservations` | Reserve equipment | Private |
| GET | `/reservations` | Get equipment reservations | Private |
| PUT | `/reservations/{reservationId}` | Update reservation | Private |
| DELETE | `/reservations/{reservationId}` | Cancel reservation | Private |
| POST | `/expenses` | Create project expense | Admin / Owner / Manager |
| GET | `/expenses` | Get project expenses | Private |
| PUT | `/expenses/{expenseId}` | Update expense | Admin / Owner / Manager |
| DELETE | `/expenses/{expenseId}` | Archive expense | Admin / Owner |
| POST | `/project-updates` | Create project progress update | Worker |
| GET | `/project-updates` | Get project updates | Private |
| PUT | `/project-updates/{updateId}` | Update project progress | Private |
| POST | `/project-updates/{updateId}/approve` | Approve project update | Owner / Manager |
| POST | `/project-share/{updateId}` | Generate customer sharing link | Owner / Manager |
| GET | `/project-share/customer/{token}` | View shared project progress | Public |
| POST | `/attachments` | Upload project file/image | Private |
| GET | `/attachments/{attachmentId}` | Get attachment | Private |
| DELETE | `/attachments/{attachmentId}` | Archive attachment | Private |
| GET | `/notifications/subscribe` | Subscribe to real-time SSE notifications | Private |
| GET | `/audit-logs` | View audit logs | Platform Admin |

> This table provides a quick reference to the main Arqemio API endpoints. Full interactive documentation, including request bodies, parameters, response models, and available endpoints, is available through Swagger/OpenAPI.

## Major Challenges

One of the main challenges was designing **multi-company authorization**. Roles are stored in `CompanyMembership` rather than directly on the user, allowing one user to belong to multiple companies while keeping company data separated.

Another challenge was securely sharing project progress with customers without requiring customer accounts. This was handled using unique public tokens that expire after 24 hours.

Equipment reservation conflicts and project-level permissions also required additional business rules to prevent unauthorized access and resource conflicts.

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
- [Baeldung - Spring Boot data.sql and schema.sql](https://www.baeldung.com/spring-boot-data-sql-and-schema-sql)

## Acknowledgements

A special thanks to **Saad Iqbal** for the guidance and support throughout the development of Arqemio.
