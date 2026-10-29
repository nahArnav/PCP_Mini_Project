# Backend module ownership and Git handoff

This is the current six-member backend division. It replaces the earlier split that counted frontend work toward a member's module. All main Java classes and both Java test classes have exactly one owner. HTML, CSS and UI screenshots belong to the separate frontend archive that Arnav Patidar will upload.

| Member | Backend files under src/main/java/com/pict/wallet unless stated otherwise |
|---|---|
| Aadi Gaware, 23201 | model/WalletUser.java, model/WalletTransaction.java, model/TransactionType.java, model/TransactionStatus.java |
| Arnav Sankapal, 23206 | repository/UserRepository.java, repository/TransactionRepository.java, DigitalWalletApplication.java; Maven wrapper, pom.xml, main application.properties and shared setup documents |
| Arnav Chandurwar, 23207 | service/UserService.java, security/WalletUserDetailsService.java, config/SecurityConfig.java, dto/RegisterRequest.java |
| Arnav Patidar, 23208 | service/WalletService.java, exception/WalletException.java, dto/AddMoneyRequest.java, dto/TransferRequest.java |
| Ayaan Shaikh, 23210 | controller/WalletApiController.java, exception/ApiExceptionHandler.java, dto/WalletResponse.java, dto/TransactionResponse.java; docs/API.md and scripts/demo_api.py |
| Charu Singla, 23215 | controller/AuthController.java, controller/WalletController.java; src/test/java/com/pict/wallet/WalletServiceTest.java and WalletHttpTest.java; test application.properties, CI workflow and test results document |

All members are Class SY10, Batch E10. Java MVC controllers are backend code; no HTML or CSS is assigned to Charu. Arnav Patidar's separate frontend upload does not replace his wallet-service backend assignment.

## Initial repository upload

1. Create the shared GitHub repository with an initial README on main and add the six members as collaborators. Do not upload the completed app first if the aim is to let members contribute their initial files.
2. Give each member the matching ZIP in delivery/member-handoff. Each archive contains a personalized study PDF, exact file manifest and project-files directory.
3. Each member clones the repository with their own GitHub account, configures their own verified commit email, creates a branch, and copies the contents of project-files into the clone root. Preserve src/main/java and all package directories.
4. Stage only that member's listed files, inspect git diff --cached, commit and push. Open a pull request into main. The coordinator merges each member's authored commits; a normal merge commit preserves the individual history.
5. Upload the separate frontend package. Only the assembled application is expected to compile and pass its HTTP/template tests. Missing dependencies during the initial partial uploads are expected.
6. After all seven packages are merged, run ./mvnw verify, then ./mvnw spring-boot:run. Use mvnw.cmd on Windows and JDK 17 or newer.

Identical code that already exists in the repository creates no new diff. In that case each member needs an actual meaningful change, rather than copying the same files again. Use the email connected to the member's account; setting a display name alone does not establish GitHub attribution. Keep local data/, target/, generated output/ and delivery/ out of version control.

The PDFs explain the prepared code, its dependencies, concrete demonstrations and likely viva questions. Each member should review and test their code and describe their actual work in the final report. The previous Word report's contribution section must be aligned with this new split before submission; the report is not part of these code archives.
