# Unit Testing Strategy for CAMRS

This document outlines the unit testing strategy for the Clinic Appointment and Medical Records System (CAMRS) to ensure codebase reliability and correctness.

## 1. Backend Testing (Spring Boot)

### Frameworks Used
- **JUnit 5**: The primary foundation for unit and integration testing.
- **Mockito**: For mocking dependencies in service layer tests.
- **Spring Boot Test**: For context loading and `@WebMvcTest` controller testing.

### Testing Layers

#### 1. Repository Layer
- **Goal**: Ensure custom queries and database interactions work as expected.
- **Approach**: Use `@DataJpaTest` to load only JPA slice. Test `StaffRepository`, `LabResultRepository`, etc. Provide an embedded H2 database or use `@AutoConfigureTestDatabase`.

#### 2. Service Layer (Core Business Logic)
- **Goal**: Validate complex logic like appointment availability checking, lab result flagging, and billing calculations.
- **Approach**: Write standard JUnit 5 tests utilizing Mockito framework (`@ExtendWith(MockitoExtension.class)`). Mock repositories and external services strictly to test isolation.
- **Key Areas to Test**:
  - `LabService.computeResultFlag()` validates High/Low/Normal flags properly.
  - `ConsultationService.checkAllergies()` checks for matching allergy strings.
  - `AppointmentService.getAvailableSlots()` filters out past time slots appropriately.

#### 3. Controller Layer
- **Goal**: Verify API routing, payload mapping, and HTTP response codes.
- **Approach**: Use `@WebMvcTest(YourController.class)` combined with `@MockBean` for the underlying services. Perform requests utilizing `MockMvc`.
- **Key Areas to Test**: 
  - Validate field constraints mapping (e.g. invalid email returns `400 Bad Request`).
  - Auth/JWT permissions to verify roles (e.g. patient accessing admin endpoints returns `403 Forbidden`).

### How to Run Backend Tests
Run the following from the `camrs-backend` directory:
```powershell
mvn test
```

---

## 2. Frontend Testing (React)

### Frameworks Used
- **Vitest**: Fast, Vite-compatible unit testing framework.
- **React Testing Library (RTL)**: To render components and simulate user events.

### Testing Layers

#### 1. Utility Functions
- **Goal**: Test standalone business logic scripts inside the frontend.
- **Approach**: Basic Vitest test cases `*.test.js`.
- **Examples**: `statusVariant` logic or JWT decoding.

#### 2. UI Components
- **Goal**: Validate that specific elements render correctly when passed certain props.
- **Approach**: RTL `render()` and `screen.getBy...` functions. Verify badges (e.g., Lab flag rendering as "HIGH ↑" in red).

### How to Run Frontend Tests
Currently, End-to-End browser tests (using Selenium) are prioritized in the `selenium-tests` folder. If pure UI unit tests are introduced:
```powershell
npm run test
```

## Continuous Integration (CI)
Unit tests should be triggered on all Pull Requests on both generic main branch commits and feature branch PRs. Merging is restricted if test coverage falls below the acceptable threshold (75%).
