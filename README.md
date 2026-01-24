# Kapital Payment Service

Backend service for handling billing and payments, including **Kapitalbank payment flows** and **Paddle subscriptions/webhooks**.  
Built with **Java 25**, **Spring Boot + Spring Data JPA**, **Liquibase**, and **Gradle**.

---

## Project Structure (high level)

- `src/main/java/com/kaptialbank/payment`
    - `controller/` — REST API endpoints (billing, users, licenses, webhooks, etc.)
    - `service/` — business logic
    - `client/` — integrations (e.g., external payment providers)
    - `dao/entity/` — JPA entities (users, roles, orders, tokens, payments, trials, saved cards)
    - `dao/repo/` — Spring Data repositories
    - `mapper/` — mapping layer
    - `config/`, `util/`, `model/`
- `src/main/resources`
    - `application.yml`, `application-local.yml` — configuration
    - `liquibase/` — database migrations
- `document/` — internal docs (API/provider notes)

---

## Main Capabilities

- User & auth-related persistence (users, roles, permissions, tokens)
- Orders & billing lifecycle
- Kapitalbank payment records & saved cards
- Trials management
- Paddle checkout initiation + webhook handling for payment/subscription events
- License creation/activation logic (based on successful payment events)

---

## API Overview

Controllers present in the project:

- `BillingController`
- `KapitalbankController`
- `PaddleWebhookController`
- `LicenseController`
- `UserController`
- `InternalTrialController`

> Exact request/response schemas depend on implementation in controllers/services.

---

## Kapital Bank Flow 
  1. Müştəri 1 illik lisenziya al-a kliklədikdə front cakend-ə /payment/kapitalbank POST request atıb geri paymentUrl alır:
     {
         "paymentUrl": "<hppUrl>",
         "orderId": "<yourLocalOrderId>"   // optional but very useful
     }
  2. Front müştərini bu url-ə yönləndirir:
          window.location.href = paymentUrl;
  3. Ödənişidən sonra müştəri bu iki səhifədən birinə qaytarılır:
        1) User redirect (browser redirect): user comes back to your UI page (e.g., /payment/success or /payment/fail)

        Server callback (backend-to-backend): Kapitalbank calls your /callback


___

## Paddle Flow 

Typical flow:

1. Frontend calls backend to create a checkout (e.g., billing/checkout).
2. Backend creates a Paddle transaction and returns `checkout.url`.
3. Frontend redirects user to Paddle checkout.
4. Paddle sends webhook events to backend (payment completion, subscription updates).
5. Backend **verifies webhook signature**, ensures **idempotency**, writes DB records, and activates license.

**Important:** Do not treat a “success page” as proof of payment—only verified webhooks.

3) Üçüncü addım: Checkout yaratmaq (Sənin backend-in Paddle API-ni çağırır)
   3.1 Müştəri sənin saytında “Subscribe” klik edir

        Frontend sənin backend-ə çağırır:
        Sənin API:
        POST /api/billing/checkout
        Body: email priceId instanceId (sənin məhsulun üçün lazımdır)

   3.2 Backend Paddle API-ni çağırır

        Backend burada Paddle-a Transaction yaradır:
        Paddle API:
        POST /transactions

        Body-nin məğzi:
        items: [{ price_id, quantity }]
        custom_data: { email, instanceId, checkoutRef }

   Nəticə: Paddle cavabında sənə checkout URL qaytarır (checkout.url)

    3.3 Frontend redirect edir

        Frontend checkoutUrl-a redirect edir.
        Müştəri Paddle checkout-da kartla ödəyir.

  4) Dördüncü addım: Ödəniş tamamlanır (Paddle tərəfində)
        Müştəri ödənişi bitirəndə:
        Paddle transaction-u “paid/completed” edir
        Müştəriyə receipt göstərir
        Səni success page-ə yönləndirə bilər (opsional)

        Amma burada qızıl qayda:
        Success page göründü deyə “payment oldu” demək deyil. Backend yalnız webhook-a inanır.

     5) Beşinci addım: Webhook gəlir (ƏN VACİB mərhələ)
        Paddle sənin backend-ə webhook göndərir:
        Sənin endpoint:
        POST /api/paddle/webhook

            Header:
            Paddle-Signature: ts=...;h1=...

            Body:
            event_type (məs: transaction.completed)
        data (transaction məlumatları)
        içində sənin göndərdiyin custom_data da olur (email, instanceId, checkoutRef)

            5.1 Backend webhook-u imza ilə yoxlayır

                Sən:
                raw body götürürsən
                Paddle-Signature-u HMAC ilə verify edirsən
                Verify olmazsa → 401 qaytarırsan

            5.2 İdempotency (təkrar webhook)
                Paddle eyni webhook-u təkrar ata bilər.
                Ona görə sən:
                event_id və ya transaction_id-ni DB-də unique saxlayırsan
                Əgər artıq işlənibsə → “ok” deyib çıxırsan

        6) Altıncı addım: DB-yə yazmaq və Licence aktivləşdirmək 
        Webhook verified + event transaction.completed (və ya payment success) olduqda:
        DB-də “payment” record yaradırsan:
            email
            transaction_id
            price_id / plan
            status = PAID
            paidAt

            Licence yaradırsan:
            licenseKey
            instanceId
            validFrom, validUntil
            status = ACTIVE

            Email göndərirsən:
            “Your license key is …”
            (və ya UI-də göstərirsən)
        Nəticə:
        Müştəri artıq məhsulu açıb aktivləşdirə bilir.

    Addımlar
        1. Paddle Dashboard-a daxil ol
            👉   https://vendors.paddle.com
                (yeni UI-də bəzən https://billing.paddle.com)

        2. Sol menyudan gir:
                 Developer tools → Authentication / API keys
                 (UI adları bir az dəyişə bilər, amma mütləq “API key” yazır)


        1. Paddle Dashboard
        👉 https://vendors.paddle.com
           2. Sol menyu:
           Developer tools → Webhooks / Notifications
           3. Webhook yarat (və ya mövcuduna bax)
           URL:
           https://api.senin-domain.com/api/paddle/webhook
        
        Events:
        transaction.completed
        subscription.created
        subscription.updated
        subscription.cancelled
        
        4. Secret key
        
        Webhook yaradanda Paddle sənə Secret göstərir:
        whsec_xxxxxxxxxxxxx


        👉 BAX BU PADDLE_WEBHOOK_SECRET-dir
---

## Configuration

Configuration is stored in:

- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`

Common categories you’ll typically configure:

- Server port / base URL
- Database connection (JDBC URL, username, password)
- Liquibase settings
- Provider keys/secrets (e.g., Paddle API key, webhook secret)
- Environment/profile selection (`local`, `dev`, `prod`)

### Suggested Environment Variables
(Names may differ in your project—align with `application*.yml`.)

- `SPRING_PROFILES_ACTIVE` (e.g., `local`)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `PADDLE_API_KEY`
- `PADDLE_WEBHOOK_SECRET`

---

## Database & Migrations (Liquibase)

Liquibase changelogs are under:

- `src/main/resources/liquibase/`

On application start (depending on config), Liquibase applies migrations automatically.

---

## Build & Run

### Prerequisites
- Java **25**
- Gradle Wrapper included (`./gradlew`)

### Build


### Run (local profile example)


Or run the main class:
- `com.kapitalbank.payment.PaymentApplication`

---

## Testing

If tests exist under `src/test`:



Or run the main class:
- `com.kapitalbank.payment.PaymentApplication`

---

## Testing

If tests exist under `src/test`:



---

## Notes / Operational Concerns

- **Webhook security:** Always verify Paddle webhook signatures using the raw request body.
- **Idempotency:** Store unique event/transaction identifiers to prevent double-processing.
- **Secrets management:** Do not commit API keys/webhook secrets; use environment variables or a secret manager.

---

## Contributing

1. Create a feature branch
2. Implement changes with tests (where applicable)
3. Ensure migrations are added for schema changes (Liquibase)
4. Open a PR with a clear description

---

## License

Internal / TBD