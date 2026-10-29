# Verification results

## Latest assembled-project checks: 30 October 2026

Verified the actual merged checkout, including Ayaan's uploaded module at b5954f8, with Java 17.0.21, Spring Boot 4.1.2 and Maven 3.9.11.

- **Maven clean verify:** 19 tests passed, zero failures, zero errors, zero skipped. `WalletServiceTest` contains 12 tests and `WalletHttpTest` contains 7.
- **Packaging:** the Spring Boot executable JAR was built successfully.
- **Assembly:** all 21 main Java classes and both test classes match the complete prepared project. All seven module branch tips are ancestors of the assembled version.

## Earlier manual checks: 24 October 2026

The following results describe the earlier prepared project; they were not repeated as part of the 30 October merge verification:

- **API demonstration:** two accounts registered and signed in; INR 1,000 top-up and INR 250.50 transfer succeeded; a subsequent INR 2,000 attempt failed. Balances stayed at INR 749.50 and INR 250.50.
- **Browser check:** registration, login, top-up, successful transfer, insufficient-balance feedback, history and page reload passed. The page had no horizontal overflow at a 390 px mobile viewport.
- **Restart check:** a separate demo account retained its INR 12.34 balance and one history record after a clean application restart using file-backed H2.
- **Report check:** four pages rendered and inspected; three actual application screenshots included. Original template styles, logo and page geometry were preserved.

Run `./mvnw clean verify` to repeat the automated tests. Run the application and `python3 scripts/demo_api.py` to repeat the API check. Tests use in-memory H2 and do not change the file-backed demo database. GitHub Actions runs Maven verification for repository pushes and pull requests; inspect the Actions tab for the status of a particular revision.
