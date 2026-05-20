# IB Farms

Livestock farm management web application built with **Spring Boot 3**, **Java 17**, **Thymeleaf**, **Spring Security**, **JPA**, and **PostgreSQL**.

## Features

- User registration and login
- Dashboard with Chart.js analytics
- Animal CRUD with multipart image upload
- Pregnancy tracking (expected delivery = pregnancy date + **283 days**)
- Growth records (height, length, weight by date)
- Daily animal expenses
- Sale records with profit calculation: `salePrice - purchasePrice - totalExpenses`
- Reports: expenses, sales, profit/loss, pregnancy, growth
- Search and filters with pagination
- Mobile-responsive Bootstrap 5 UI

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## Database setup (PostgreSQL)

### macOS (Homebrew)

```bash
brew install postgresql@16
brew services start postgresql@16
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"

# Create role and database (matches application.yml)
psql postgres -c "CREATE ROLE postgres WITH LOGIN PASSWORD 'postgres' SUPERUSER CREATEDB;"
psql postgres -c "CREATE DATABASE ibfarms OWNER postgres;"
```

Default connection in `application.yml`:

| Setting  | Value |
|----------|-------|
| URL      | `jdbc:postgresql://localhost:5432/ibfarms` |
| Username | `postgres` |
| Password | `postgres` |

Data persists in PostgreSQL (no in-memory H2). Tables are created automatically on first run (`ddl-auto: update`).

## Run the application

Use **Java 17 or 21** (Maven on Java 24 may fail with Lombok until upgraded):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS example
cd ib-farms
mvn spring-boot:run
```

Open [http://localhost:10000](http://localhost:10000) and register a new account.

### User registration

New accounts are created **disabled** until an administrator enables them in the database.

1. User registers and sees: *Registration successful. Ask your IT administrator to enable your account.*
2. Enable the user in Neon/PostgreSQL:

```sql
UPDATE users SET enabled = true WHERE username = 'their_username';
```

3. The user can sign in.

## Project structure

```
src/main/java/com/ibfarms/
├── config/          # Security, Web MVC, Cloudinary
├── controller/      # MVC controllers
├── dto/             # Form and view DTOs
├── entity/          # JPA entities
├── exception/       # Error handling
├── repository/      # Spring Data JPA
├── service/         # Business logic
└── util/            # Pregnancy, profit, file storage
```

## Branding

The IB Farms logo lives at `src/main/resources/static/images/ib-farms-logo.png` and is used in the navbar, login/register screens, page headers, footer, and browser tab favicon. Replace that file with your own asset to update branding everywhere.

## Cloudinary (animal images)

Animal photos are uploaded to [Cloudinary](https://cloudinary.com/).

**Local setup (recommended):** copy `.env.example` values into `application-local.yml` at the project root (this file is gitignored and loaded automatically):

```yaml
cloudinary:
  cloud-name: your_cloud_name
  api-key: your_api_key
  api-secret: your_api_secret
  folder: ib-farms/animals
```

**Or** set environment variables:

```bash
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
```

Optional: change the folder prefix with `cloudinary.folder` (default `ib-farms/animals`).

The database stores each image’s **public ID**; URLs are generated when pages render (with automatic format/quality optimization and thumbnails on the animal list).

## Deploy on Render

This repo includes a [Render Blueprint](https://render.com/docs/blueprint-spec) (`render.yaml`) and a production `Dockerfile`.

### Option A — Blueprint (recommended)

1. Push this repo to GitHub.
2. In [Render Dashboard](https://dashboard.render.com/) → **New** → **Blueprint**.
3. Connect the `ib-farms` repository and apply `render.yaml`.
4. After the first deploy, open the **ib-farms** web service → **Environment** and set:

| Variable | Example |
|----------|---------|
| `CLOUDINARY_CLOUD_NAME` | from Cloudinary dashboard |
| `CLOUDINARY_API_KEY` | from Cloudinary dashboard |
| `CLOUDINARY_API_SECRET` | from Cloudinary dashboard |

PostgreSQL credentials are wired automatically via `DATABASE_URL`. The app converts Render’s `postgres://` URL to JDBC and uses SSL for `*.render.com` hosts.

Health check: `/actuator/health`

### Option B — Manual web service

1. **New** → **Web Service** → connect GitHub repo.
2. **Runtime:** Docker.
3. Create a **PostgreSQL** database and link it to the service.
4. Set the environment variables from the table above.
5. Render sets `PORT` automatically; the app defaults to `10000` if unset.

### Notes

- Free tier web services spin down after inactivity (cold start ~30s).
- Free PostgreSQL expires after 90 days on Render; upgrade or export data before then.
- Never commit secrets; use Render environment variables only.

## Build JAR

```bash
mvn clean package -DskipTests
java -jar target/ib-farms-1.0.0.jar
```

## Technology stack

- Spring Boot 3.2 / Spring MVC
- Spring Data JPA
- Spring Security (form login)
- Thymeleaf + Bootstrap 5 + Chart.js
- PostgreSQL
- Lombok

## License

MIT
