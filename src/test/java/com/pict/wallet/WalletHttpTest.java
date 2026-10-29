package com.pict.wallet;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;

// Real HTTP requests verify security filters, JSON binding, controllers and templates together.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletHttpTest {
    @LocalServerPort int port;
    HttpClient browser;
    String email;

    @BeforeEach
    void prepare() {
        browser = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        email = "test-" + UUID.randomUUID() + "@example.com";
    }

    private HttpResponse<String> get(String path) throws Exception {
        return browser.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String csrf() throws Exception {
        String body = get("/api/csrf").body();
        var matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        assertThat(matcher.find()).as("CSRF endpoint returns a token").isTrue();
        return matcher.group(1);
    }

    private HttpResponse<String> post(String path, String body, String contentType, boolean withCsrf) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", contentType);
        if (withCsrf) { request.header("X-CSRF-TOKEN", csrf()); }
        return browser.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private void registerAndLogin() throws Exception {
        var registration = post("/api/register", "{\"name\":\"Test User\",\"email\":\"" + email
                + "\",\"password\":\"Classroom123!\"}", "application/json", true);
        assertThat(registration.statusCode()).isEqualTo(201);
        assertThat(registration.body()).doesNotContain("password", "Hash");
        var login = post("/login", "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&password=Classroom123%21", "application/x-www-form-urlencoded", true);
        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(login.headers().firstValue("location").orElse("")).endsWith("/wallet");
    }

    @Test
    void anonymousUsersCannotReadWalletAndPagesRender() throws Exception {
        assertThat(get("/api/wallet").statusCode()).isEqualTo(401);
        assertThat(get("/api/wallet/transactions").statusCode()).isEqualTo(401);
        assertThat(get("/login").body()).contains("Sign in to your wallet", "_csrf");
        assertThat(get("/register").statusCode()).isEqualTo(200);
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        var response = post("/login", "email=unknown%40example.com&password=wrong-password",
                "application/x-www-form-urlencoded", true);
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location").orElse("")).endsWith("/login?error");
        assertThat(get("/api/wallet").statusCode()).isEqualTo(401);
    }

    @Test
    void csrfIsRequiredEvenWithAnAuthenticatedSession() throws Exception {
        registerAndLogin();
        var response = post("/api/wallet/add-money", "{\"amount\":100}", "application/json", false);
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(get("/api/wallet").body()).contains("\"balance\":0");
    }

    @Test
    void validTopUpAndFailedTransferHaveUsefulHttpResults() throws Exception {
        registerAndLogin();
        var added = post("/api/wallet/add-money", "{\"amount\":100.25}", "application/json", true);
        assertThat(added.statusCode()).isEqualTo(200);
        assertThat(added.body()).contains("SUCCESS", "createdAt");
        var transfer = post("/api/wallet/transfer", "{\"recipientEmail\":\"missing@example.com\",\"amount\":10}", "application/json", true);
        assertThat(transfer.statusCode()).isEqualTo(422);
        assertThat(transfer.body()).contains("FAILED", "Recipient account does not exist");
        assertThat(get("/api/wallet").body()).contains("100.25");
        assertThat(get("/api/wallet/transactions").body()).contains("SUCCESS", "FAILED");
        var page = get("/wallet");
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains("100.25", "Transaction history", "FAILED");
    }

    @Test
    void invalidJsonAndAmountsAreRejected() throws Exception {
        registerAndLogin();
        for (String body : new String[]{"{\"amount\":-1}", "{\"amount\":1.001}", "{}", "{bad-json}", "{\"amount\":100001}"}) {
            assertThat(post("/api/wallet/add-money", body, "application/json", true).statusCode()).isEqualTo(400);
        }
        assertThat(get("/api/wallet/transactions").body()).isEqualTo("[]");
    }

    @Test
    void anotherSessionCannotChooseTheSenderOrReadAnotherHistory() throws Exception {
        registerAndLogin();
        post("/api/wallet/add-money", "{\"amount\":50}", "application/json", true);
        browser = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        email = "other-" + UUID.randomUUID() + "@example.com";
        registerAndLogin();
        assertThat(get("/api/wallet/transactions").body()).isEqualTo("[]");
        assertThat(get("/api/wallet?userId=1").body()).contains(email).doesNotContain("\"balance\":50");
    }

    @Test
    void browserFormsTopUpAndLogoutWork() throws Exception {
        registerAndLogin();
        assertThat(post("/wallet/add-money", "amount=123.45", "application/x-www-form-urlencoded", true).statusCode())
                .isEqualTo(302);
        assertThat(get("/wallet").body()).contains("123.45", "Demo money added successfully");
        assertThat(post("/logout", "", "application/x-www-form-urlencoded", true).statusCode()).isEqualTo(302);
        assertThat(get("/api/wallet").statusCode()).isEqualTo(401);
    }
}
