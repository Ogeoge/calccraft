# CalcCraft

CalcCraft is an **offline** Android calculator app built with **Kotlin + Jetpack Compose**. It evaluates expressions safely using a **Tokenizer → Shunting-Yard (RPN) → Evaluator** pipeline (no `eval`, no scripting engines) and keeps an **in-memory-only** history that is cleared when the app/process closes.

## Features

- **Two screens**
  - **Calculator**: display + responsive keypad
  - **History**: list of prior calculations, **Clear**, and **Export** via Android Sharesheet (plain text)
- **Supported syntax**
  - Operators: `+`, `-`, `*`, `/`
  - Parentheses: `(` `)`
  - **Unary minus** (e.g., `-3`, `2*-5`)
  - Decimals (e.g., `3.14`)
  - **Percent** postfix (e.g., `50%`)
  - **Exponentiation**: `^` (right-associative)
  - **Sqrt**: `√` and `sqrt(...)`
  - Functions: `sin(...)`, `cos(...)`, `tan(...)`, `log(...)`
- **Robust, non-crashing errors** surfaced to the UI
  - EMPTY_EXPRESSION, INVALID_SYNTAX, UNKNOWN_TOKEN, MISMATCHED_PARENTHESES, DIVIDE_BY_ZERO, DOMAIN_ERROR
- **Hardware keyboard input**
  - Digits/operators append to the expression
  - `Enter` = evaluate (`=`)
  - `Backspace` = delete
  - `Escape` or `C` = clear
  - Parentheses keys append `(` / `)`
- **Quality gates**
  - Unit tests (JUnit) for tokenizer, shunting-yard, evaluator, and core ViewModel logic
  - `ktlint` formatting/linting
  - GitHub Actions CI: lint + unit tests + assemble

## Project constraints (by contract)

- Android-only
- Fully offline: **no network calls at runtime**
- No accounts/auth: reserved endpoints exist only for contract-based tooling and must not be called
- No persistence: history is **in-memory only** (do not add Room/SQLite/DataStore without updating the contract)

## Contract notes (endpoints)

This app has **no backend**. These endpoints are documented only as a lightweight contract artifact:

- `GET /health` (local-only stub spec; if ever implemented, returns `{"status":"ok"}`)
- `POST /auth/login` (reserved / not implemented)
- `POST /auth/logout` (reserved / not implemented)

The Android app must **not** call these endpoints.

## Build & run

### Prerequisites

- Android Studio (latest stable)
- Android SDK (installed via Android Studio)
- JDK 17 (Android Studio bundled JDK is fine)

### Run from Android Studio

1. Open the project in Android Studio
2. Select an emulator/device
3. Run the `app` configuration

### Run from command line

- Debug install (connected device/emulator required):

```bash
./gradlew :app:installDebug
```

- Build APK:

```bash
./gradlew :app:assembleDebug
```

## Tests

Run unit tests:

```bash
./gradlew test
```

(Tests cover tokenizer, shunting-yard conversion, RPN evaluator behavior, and core ViewModel intent handling.)

## Lint / formatting

Run ktlint checks:

```bash
./gradlew ktlintCheck
```

Auto-format (if configured in the project):

```bash
./gradlew ktlintFormat
```

## Usage tips / examples

Try expressions like:

- `1+2*3` → `7`
- `(1+2)*3` → `9`
- `2^3^2` → `512` (right-associative)
- `50%` → `0.5`
- `sqrt(9)` or `√9` → `3`
- `log(10)`
- `sin(0)`

If evaluation fails, the app shows a user-visible error message and keeps running.

## History export format

History is exported as plain text suitable for sharing, containing entries with:

- expression
- result (or error message)

History is in-memory only and is cleared when the app closes.

## Initialize git repo and push to GitHub (repo name: `calccraft`)

From the project root:

```bash
git init
git add .
git commit -m "Initial commit: CalcCraft" 
```

Create an empty GitHub repository named **calccraft** (no README/license from GitHub, since they’re already included), then:

```bash
git branch -M main
git remote add origin https://github.com/<your-username>/calccraft.git
git push -u origin main
```

## License

MIT License — see `LICENSE`.
