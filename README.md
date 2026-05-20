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

Open [http://localhost:8080](http://localhost:8080) and register a new account.

### Registration approval

New accounts are **disabled until you approve them**:

1. Someone registers on the site.
2. You receive an email at **musamakhan201@gmail.com** with an **Approve** link.
3. Open that link to enable the account; the user can then sign in.

Configure SMTP in `application-local.yml` (see `.env.example`). Without mail settings, approval links are logged to the console instead.

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: musamakhan201@gmail.com
    password: your_gmail_app_password

ibfarms:
  admin-email: musamakhan201@gmail.com
  base-url: http://localhost:8080   # use your public URL in production
```

For Gmail, create an [App Password](https://myaccount.google.com/apppasswords) (2-Step Verification required).

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
