# Arnav Sankapal - Repositories application setup and persistence

Roll 23206 | SY10 | E10

Read the included STUDY_GUIDE.pdf, then open the matching code files.

## Copy location

Copy **the contents of project-files/** into the root of your clone of the shared project repository. Merge directories and preserve relative paths. Do not put project-files itself inside the repository. The root eventually contains pom.xml and src/ beside each other.

This archive is one source slice; it does not compile on its own. All six backend archives plus the separate frontend archive reconstruct the complete project. Other members own the dependencies named in the PDF.

## Your exact files

- `src/main/java/com/pict/wallet/repository/UserRepository.java`
- `src/main/java/com/pict/wallet/repository/TransactionRepository.java`
- `src/main/java/com/pict/wallet/DigitalWalletApplication.java`
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/maven-wrapper.properties`
- `.gitignore`
- `.gitattributes`
- `src/main/resources/application.properties`
- `README.md`
- `CONTRIBUTING.md`
- `docs/CLASS_GUIDE.md`

## Initial upload from your account

The coordinator should create main with only an initial README, add all members as collaborators, and avoid uploading the completed app before your module commits. If the repository already contains identical files, these files will not generate a new diff. Use meaningful changes in that case; copying alone cannot create new code contributions.

Replace the two placeholders. Run these commands inside your own clone; use the email linked to your GitHub account, including GitHub's provided noreply address if preferred.

```sh
git clone REPOSITORY_URL pocket-wallet
cd pocket-wallet
git config user.name "Arnav Sankapal"
git config user.email "YOUR_VERIFIED_EMAIL"
git switch -c backend/repositories-sankapal
```

Copy the project-files contents into that clone, then:

```sh
git add -- "src/main/java/com/pict/wallet/repository/UserRepository.java"
git add -- "src/main/java/com/pict/wallet/repository/TransactionRepository.java"
git add -- "src/main/java/com/pict/wallet/DigitalWalletApplication.java"
git add -- "pom.xml"
git add -- "mvnw"
git add -- "mvnw.cmd"
git add -- ".mvn/wrapper/maven-wrapper.properties"
git add -- ".gitignore"
git add -- ".gitattributes"
git add -- "src/main/resources/application.properties"
git add -- "README.md"
git add -- "CONTRIBUTING.md"
git add -- "docs/CLASS_GUIDE.md"
git diff --cached
git commit -m "Add repositories application setup and database configuration"
git push -u origin backend/repositories-sankapal
```

Open a pull request targeting main. The coordinator merges the module commits, preferably using a merge commit to preserve individual history. Do not commit another member's files. You do not need to upload STUDY_GUIDE.pdf or these handoff notes for the app to run.

## Verify after assembly

Install JDK 17 or newer. Once all backend modules and frontend resources are merged:

```sh
chmod +x mvnw
./mvnw verify
./mvnw spring-boot:run
```

On Windows use mvnw.cmd. Missing-class or missing-template errors before assembly is complete are expected. Do not use data/ from another person's machine. A packaged JAR must be stopped before it is rebuilt.

The source in this archive is a prepared project handoff. Review, understand and test your module; the commit proves that your account contributed those repository changes, not that GitHub can verify who originally wrote them.
