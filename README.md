EVPrime Backend Automated Tests

Automated tests for the EVPrime backend API, written in Java using JUnit and RestAssured. Covers user authentication and event management.
Test Structure
Tests:

SignUpTests.java – User registration (positive & negative)
LoginTests.java – Login scenarios
GetAllEventsTests.java – Fetch all events
GetSingleEventTests.java – Fetch event by ID
PostEventTests.java – Create new events
UpdateEventTests.java – Update events
DeleteEventTests.java – Delete events

Support classes:
client/ – API requests
data/ – Test data factories
database/ – DB validation
models/ – Request & response objects
objectbuilder/ – Request object builders
util/ – Configs & helpers

How to Run
git clone <repository-url>
cd EVPrimeBackendAutomatedTests
mvn test

Reporting
Console output by default
Optional HTML reports with Surefire or Allure

Notes
Tests are independent and can run in any order
Test data is dynamically generated
Each bug or feature is linked to its Issue or Commit in the repository
