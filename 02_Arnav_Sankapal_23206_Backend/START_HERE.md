# Pocket Wallet member handoff

BHAU, send ZIPs 01 to 06 to the named members. Keep ZIP 07 for your separate frontend upload. Each ZIP contains a four-page personalized STUDY_GUIDE.pdf, exact upload instructions, FILES_TO_PUSH.txt and a project-files folder with only that owner's files.

## Backend division

| ZIP | Member | Java module |
|---|---|---|
| 01_Aadi_Gaware_23201_Backend.zip | Aadi Gaware - 23201 | WalletUser, WalletTransaction, TransactionType, TransactionStatus |
| 02_Arnav_Sankapal_23206_Backend.zip | Arnav Sankapal - 23206 | UserRepository, TransactionRepository, DigitalWalletApplication; shared Maven and database setup |
| 03_Arnav_Chandurwar_23207_Backend.zip | Arnav Chandurwar - 23207 | UserService, WalletUserDetailsService, SecurityConfig, RegisterRequest |
| 04_Arnav_Patidar_23208_Backend.zip | Arnav Patidar - 23208 | WalletService, WalletException, AddMoneyRequest, TransferRequest |
| 05_Ayaan_Shaikh_23210_Backend.zip | Ayaan Shaikh - 23210 | WalletApiController, ApiExceptionHandler, WalletResponse, TransactionResponse |
| 06_Charu_Singla_23215_Backend.zip | Charu Singla - 23215 | AuthController, WalletController, WalletServiceTest, WalletHttpTest |
| 07_Frontend_For_Arnav_Patidar.zip | Arnav Patidar - frontend upload | Three Thymeleaf HTML templates, CSS and existing UI screenshots; zero Java files |

All members: SY10, E10. There are 21 main Java files and 2 Java test files. Every one belongs to exactly one member. The ownership manifest lists all 45 project files and SHA-256 hashes. No code was changed to manufacture extra classes or commits.

## How to make the initial commits from all six accounts

1. Create a shared GitHub repository with an initial README on main. Add each teammate as a collaborator and have them accept the invitation. Do not upload the completed application first if the goal is to let each person add their module.
2. Let Sankapal add the shared Maven/application setup first. Each teammate then clones the latest main, creates the branch in their guide, and copies only their project-files contents into the clone root. Merge src folders and keep original package paths.
3. Each teammate configures their own Git user.name and verified user.email, stages only the paths in FILES_TO_PUSH.txt, checks git diff --cached, commits and pushes from their account. Exact commands are inside each archive.
4. Each teammate opens a pull request into main. Merge the authored commits, preferably with Create a merge commit so the individual history stays easy to inspect. Do not push all six modules yourself if the members are meant to contribute them from their own accounts.
5. Push ZIP 07's project-files contents in a separate frontend branch under your own account. This is in addition to your wallet-service module in ZIP 04.
6. Once all seven slices are merged, run ./mvnw verify. All modules depend on other slices, and the HTTP tests also require the frontend templates; a partial slice is not expected to compile or run alone.

If identical files have already been uploaded, copying them will not create a new Git diff. Use a new empty repository for the initial split, or make real module improvements in the existing repository. Do not delete an existing repository or rewrite its history just to apply these instructions.

## Final assembly check

Install JDK 17 or newer. In the assembled repository root:

```sh
chmod +x mvnw
./mvnw verify
./mvnw spring-boot:run
```

Windows uses mvnw.cmd. Open http://localhost:8080. Register two demo accounts, add INR 1000 to one, transfer INR 250.50 to the other, then attempt an unaffordable transfer. The expected balances remain INR 749.50 and INR 250.50 after the rejected attempt. Funds are simulated.

Keep data/, target/, output/ and delivery/ out of Git. Do not share local account databases. Stop a running packaged JAR before rebuilding it; development is simpler with spring-boot:run.

The existing Word report and earlier whole-project archive remain separate. The report's previous contribution table used the older split; copy the current table from REPORT_CONTRIBUTIONS.md before submitting it. The member code ZIPs deliberately omit the report, generated build output and local database.

## GitHub attribution references

A member's commit email must be associated with their own GitHub account, and commits should be merged into the default branch. GitHub's contributor view may not update immediately. See the official instructions:

- [Set a commit email](https://docs.github.com/en/account-and-profile/how-tos/email-preferences/setting-your-commit-email-address)
- [Troubleshoot missing contributions](https://docs.github.com/en/account-and-profile/how-tos/contribution-settings/troubleshooting-missing-contributions)

These are prepared code packages. Members should understand and test their files; Git can track the account that contributed the changes, not establish original authorship of prepared code.
