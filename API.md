# REST API guide

Base URL: `http://localhost:8080`. Amounts are decimal INR values, timestamps are UTC ISO 8601, and JSON responses never include password hashes. Browser pages display timestamps in IST.

The API uses the same session and CSRF protection as the website. It intentionally does not add a second JWT authentication system.

| Method | Path | Result |
|---|---|---|
| GET | `/api/csrf` | 200, token details; also available before login |
| POST | `/api/register` | 201, account details; 400 invalid input; 409 duplicate email |
| POST | `/login` | Form-encoded email/password; 302 to `/wallet` on success or `/login?error` on failure |
| GET | `/api/wallet` | 200, current user's account and balance; 401 without login |
| POST | `/api/wallet/add-money` | 200 `SUCCESS`; 422 `FAILED` when balance limit is exceeded |
| POST | `/api/wallet/transfer` | 200 `SUCCESS`; 422 `FAILED` for business rejection |
| GET | `/api/wallet/transactions` | 200, current user's history, newest first |
| POST | `/logout` | Ends the session; 302 to login |

All POST requests require CSRF. Missing/invalid token gives 403 before business code runs. Invalid JSON, missing fields, nonpositive amounts, amounts over 100000 or more than two decimal places give 400. A database lock conflict gives 409; check history before retrying. API routes require login except registration and CSRF.

## Session setup in Postman

1. Send GET `/api/csrf`. Keep Postman's cookie jar enabled. Copy the `token`; its `headerName` is `X-CSRF-TOKEN`.
2. POST `/api/register` with that header and JSON:

   ```json
   {"name":"Aadi Gaware","email":"aadi@example.com","password":"YOUR_DEMO_PASSWORD"}
   ```

3. POST `/login` with the token header, body type **x-www-form-urlencoded**, and fields `email` and `password`. Disable automatic redirects if you want to inspect the 302 response. Success redirects to `/wallet`; login failure redirects to `/login?error`.
4. GET `/api/csrf` again after login: authentication rotates the session and clears the old CSRF token. Use the new token for later POSTs and keep the same session cookie.
5. Call the authenticated endpoints below. Fetch a fresh token after logout or another login.

## Request examples

POST `/api/wallet/add-money`:

```json
{"amount":1000.00}
```

POST `/api/wallet/transfer`:

```json
{"recipientEmail":"arnav@example.com","amount":250.50}
```

Successful transfer body (ID and timestamp vary):

```json
{
  "id":2,
  "initiatorEmail":"aadi@example.com",
  "recipientEmail":"arnav@example.com",
  "amount":250.50,
  "type":"TRANSFER",
  "status":"SUCCESS",
  "message":"Transfer completed successfully",
  "createdAt":"2026-10-24T17:45:00Z"
}
```

A syntactically valid attempt with insufficient funds returns the same shape with HTTP 422, `status` equal to `FAILED` and `message` equal to `Insufficient balance`. It is stored in the sender's history; balances do not change. Unknown recipients and self-transfers also create failed records. Malformed or invalid requests do not create transaction records. Unexpected database errors roll back the operation and do not promise a failure record.

GET `/api/wallet`:

```json
{"id":1,"name":"Aadi Gaware","email":"aadi@example.com","balance":749.50}
```

History contains the user's top-ups, outgoing attempts and successful incoming transfers. The server does not accept a sender ID, and adding `?userId=...` cannot switch accounts.

## Automated demonstration

Run `python3 scripts/demo_api.py` while the application is running. It creates two uniquely named demo accounts, uses proper CSRF/session handling, verifies a transfer, confirms a failed overspend and checks both balances. It uses simulated funds and leaves those demonstration accounts in your local database.
