# Verification results

Verified locally on 24 October 2026 with Java 17.0.21, Spring Boot 4.1.2 and Maven 3.9.11.

- **Maven verify:** 19 tests passed, zero failures, zero errors, zero skipped. `WalletServiceTest` contains 12 tests and `WalletHttpTest` contains 7.
- **API demonstration:** two accounts registered and signed in; INR 1,000 top-up and INR 250.50 transfer succeeded; a subsequent INR 2,000 attempt failed. Balances stayed at INR 749.50 and INR 250.50.
- **Browser check:** registration, login, top-up, successful transfer, insufficient-balance feedback, history and page reload passed. The page had no horizontal overflow at a 390 px mobile viewport.
- **Restart check:** a separate demo account retained its INR 12.34 balance and one history record after a clean application restart using file-backed H2.
- **Report check:** four pages rendered and inspected; three actual application screenshots included. Original template styles, logo and page geometry were preserved.

Run `./mvnw verify` to repeat the automated tests. Run the application and `python3 scripts/demo_api.py` to repeat the API check. Tests use in-memory H2 and do not change the file-backed demo database. GitHub Actions is configured, but no remote CI run or repository push has been performed.
