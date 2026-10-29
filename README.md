# Pocket Wallet

A beginner-friendly Java mini project for PCP CIE, PICT. One Spring Boot application handles account registration, login, simulated top-ups, transfers, and transaction history. The browser interface uses plain HTML, CSS and Thymeleaf. There is no JavaScript framework.

**Build status:** All six backend modules and the frontend are assembled. A clean Maven build passes all 19 tests and packages the runnable application. See [the integration checklist](docs/INTEGRATION_STATUS.md) for the merged module revisions and [verification results](docs/TEST_RESULTS.md) for test details.

## Run the project

Requires a **JDK 17 or newer** and Git. A JRE alone cannot compile the project. You do not need to install Maven or MySQL. Internet is needed on the first run so the Maven wrapper can download Maven 3.9.11 and the project dependencies.

### 1. Get the code

For a first-time download, run:

```sh
git clone https://github.com/nahArnav/PCP_Mini_Project.git
cd PCP_Mini_Project
```

If you already have the repository, open a terminal inside that existing project folder instead. Use `git pull --ff-only` on a clean `main` checkout to get the latest merged version.

### 2. Start on macOS or Linux

Run these commands from the project root, where `pom.xml` is located:

```sh
java -version
chmod +x mvnw
./mvnw spring-boot:run
```

If macOS says it cannot locate Java, but you already installed `openjdk@17` using Homebrew, select that JDK for the current terminal and start again:

```sh
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
./mvnw spring-boot:run
```

### 2. Start on Windows PowerShell

With JDK 17 or newer available in your terminal, run from the project root:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

### 3. Open the wallet and stop the server

Open **[http://localhost:8080](http://localhost:8080)** after the terminal reports that `DigitalWalletApplication` has started. Create two accounts with different email addresses. Sign in to one, add demo funds, then transfer to the other account's email. Use a separate browser profile for the second account if you want both open at once.

Keep the server terminal open. Press **Ctrl+C** in it to stop the application. H2 stores data in `data/wallet.mv.db`, created automatically, so accounts and history survive restarts. Always start from the same project root to use the same database. To reset your own demo, stop the app and delete the `data` folder; that permanently deletes its local accounts and history.

If port 8080 is busy, start on port 8081 instead:

```sh
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Then open **http://localhost:8081**. The app binds to loopback for a local demonstration.

## What works

- Registration with unique, normalized email, zero starting balance and BCrypt password hashing.
- Session login/logout and CSRF protection on all POST requests.
- Simulated add money, transfer by email and private transaction history.
- `SUCCESS` and `FAILED` transaction results with timestamps. The UI displays IST; the API returns UTC timestamps.
- Rejection of insufficient funds, self-transfers, unknown recipients and wallet limit violations. These business failures appear in the initiating user's history.
- Rejection of invalid amounts, malformed input and duplicate accounts. Invalid requests do not create wallet transactions.
- Exact money arithmetic with `BigDecimal`; no `double` or `float` for balances.
- One database transaction saves the debit, credit and transfer record together. Ordered database row locks protect simultaneous operations.
- File-backed H2 data survives restarts. Tests use a separate in-memory database.

Amounts are INR 0.01–1,00,000 per operation, with at most two decimal places. A wallet can hold up to INR 10,00,000. Incoming transfer failures are visible only to the sender; recipients see successful incoming transfers.

## Keep the scope clear

Top-ups create **simulated money**. This project has no bank, card, UPI or payment gateway integration. A repeated valid POST is a new operation; idempotency keys are a future improvement. There is no OTP, password reset, login throttling, production HTTPS setup or database encryption. Use disposable demo credentials. This is a college demonstration, not a service for real funds.

## Understand the code

```text
Browser pages / REST requests
             |
     Spring Security (login and CSRF)
             |
        Controllers         accept input and choose a response
             |
          Services          business rules and transactions
             |
       Repositories         database access through Spring Data JPA
             |
        H2 database         users and wallet transactions
```

`WalletUser` holds one account and its balance. `WalletTransaction` holds an operation and references its initiator and optional recipient. A separate wallet table is unnecessary because each user has exactly one wallet. DTO classes carry form/JSON input and safe responses; entities are never returned directly by the API.

Read [the class and viva guide](docs/CLASS_GUIDE.md), [the API guide](docs/API.md), and [the six-member contribution guide](CONTRIBUTING.md).

## Test and package

Stop any running copy of this project first. From the project root, run all 19 tests and build the executable JAR:

```sh
./mvnw clean verify
java -jar target/digital-wallet-1.0.0.jar
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
java -jar target/digital-wallet-1.0.0.jar
```

The second command starts the packaged application at **http://localhost:8080**. Press **Ctrl+C** to stop it.

The tests cover registration, password hashing, transfers, failed attempts, money precision, limits, rollback, concurrent operations, authorization, CSRF, JSON validation and browser form rendering. `WalletServiceTest` tests business rules; `WalletHttpTest` sends real HTTP requests through the running application. To demonstrate API calls and automatically check a full two-account flow while the app is running, use `python3 scripts/demo_api.py` (Python 3 standard library only).

If you run the packaged JAR, stop it with Ctrl+C **before** running Maven `package` or `verify` again, then restart it after the build finishes. Rebuilding replaces that JAR; a process still using the old file can return HTTP 500 with class-loading errors. During development, prefer `./mvnw spring-boot:run`.

## Team

All six members: Class **SY10**, Batch **E10**.

| Roll no. | Name | Assigned module |
|---|---|---|
| 23201 | Aadi Gaware | Entities and enums |
| 23206 | Arnav Sankapal | Repositories, application entry point and project setup |
| 23207 | Arnav Chandurwar | Registration and authentication |
| 23208 | Arnav Patidar | Wallet business logic and money request DTOs |
| 23210 | Ayaan Shaikh | REST API, response DTOs and validation responses |
| 23215 | Charu Singla | Java MVC controllers and both Java integration test classes |

Frontend HTML, CSS and UI screenshots are a separate coordinator package for Arnav Patidar. No frontend resources count toward the six backend assignments. The current split and upload sequence are documented in CONTRIBUTING.md, and the assembly status is in docs/INTEGRATION_STATUS.md. These are ownership assignments; update the report to match each member's actual work and Git commits before submitting.

## Report and submission

The filled Word report is distributed separately as `report/PCP_CIE_Digital_Wallet_Report.docx`; the individual code archives omit it. Its earlier contribution table should be updated to the current backend division before submission. It follows the supplied PICT template and includes the team, database design, API summary, verified test outcomes and actual application screenshots. Add your final GitHub URL and project number, and confirm the contribution descriptions before submission. Repository URL: [nahArnav/PCP_Mini_Project](https://github.com/nahArnav/PCP_Mini_Project).

## References

- [Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Security password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Spring Data JPA locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)

Spring Boot version: 4.1.2; Java target: 17. Maven manages dependency versions through the Spring Boot parent.
