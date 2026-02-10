# CalcCraft

Offline Android calculator built with **Kotlin + Jetpack Compose**. It evaluates expressions safely using a **Tokenizer → Shunting-yard → RPN evaluator** pipeline (no `eval`, no JS engine), supports **hardware keyboard input**, and maintains an **in-memory history** (cleared on app/process death).

> Note on “/health”: The global contract includes a `GET /health` shape for compliance, but this project is **Android-only** and **MUST NOT** run an HTTP server or make network calls. The equivalent is an in-app `HealthCheck` function returning a `HealthStatus` model.

## Features

- **Safe expression engine**
  - Operators: `+ - * /`
  - Parentheses: `(` `)`
  - Decimals: `1.23`
  - Unary minus: `-3`, `-(2+1)`
  - Percent: `%` (see `CONTRACT.md` for exact behavior)
  - Functions: `sin`, `cos`, `tan`, `log`, `sqrt`, `pow(a,b)`
  - Constants: `pi`, `e`
- **Robust error handling** (non-crashing)
  - Divide-by-zero, invalid syntax, unknown tokens, mismatched parentheses, domain errors, overflow/stack errors
  - Errors are shown in the UI and also stored in history
- **Two destinations**
  - Calculator
  - History (in-memory only; can be cleared)
- **Hardware keyboard support**
  - Digits/operators/parentheses/decimal
  - `Enter` evaluates
  - `Backspace` deletes
  - `Escape` may clear (optional)
- **Polished Compose UI**
  - Material 3
  - Custom button component
  - Animated result/error transitions

## Project structure

- `android/` — Android Gradle project
  - `app/src/main/java/com/calccraft/domain/engine/` — Tokenizer, shunting-yard, RPN evaluator
  - `app/src/main/java/com/calccraft/state/` — intents/state/reducer/ViewModel
  - `app/src/main/java/com/calccraft/ui/` — Compose UI components and screens

## Contract (local-only)

This project follows the repository contract described in `CONTRACT.md`.

Key points:
- No network calls.
- No HTTP server.
- “Endpoint” `GET /health` is implemented as an **in-app function** returning a `HealthStatus` model:
  - `status`: must be `"ok"` when functional
  - `app`: must be `"CalcCraft"`
  - `version`: app version name

The evaluation pipeline returns an `EvalResult` data model:
- `type`: `"success"` or `"error"`
- On success: `value` (Double), `formatted` (String)
- On error: `error_kind` and `message`

History is in-memory `HistoryEntry` items and is cleared when the app process is killed.

## Requirements

- Android Studio (recent stable)
- JDK 17

## Run (Android)

1. Open `android/` in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration.
4. Run on an emulator/device.

## Test & lint

From the repository root:

```bash
cd android
./gradlew test
```

Run ktlint (configured via Gradle plugin/tasks):

```bash
cd android
./gradlew ktlintCheck
```

Assemble debug:

```bash
cd android
./gradlew assembleDebug
```

## CI

GitHub Actions is expected to run on push/PR:
- ktlint
- unit tests
- assembleDebug

See `.github/workflows/` in the repository.

## Notes on keyboard input

Hardware keyboard events are mapped to reducer intents. At minimum:
- Digits `0-9` → append
- `+ - * /` → append
- `(` `)` `.` → append
- `Enter` → evaluate
- `Backspace` → delete
- `Escape` → clear (optional)

Reducer/mapper behavior is covered by unit tests.

## License

MIT. See `LICENSE`.

## Suggested clean commit sequence

A small, readable history helps reviewers and CI debugging:

1. `chore: scaffold android project + ci + ktlint`
2. `feat: expression engine (tokenizer + shunting-yard + rpn evaluator)`
3. `feat: calculator reducer + viewmodel + in-memory history`
4. `feat: compose ui (calculator + history) + hardware keyboard mapping`
5. `test: engine and reducer unit tests`
6. `docs: README + CONTRACT`

## Create repo + push to GitHub (repo name: `calccraft`)

From the **repository root** (same level as `README.md`):

1. Initialize git and make the first commit:

```bash
git init
git add .
git commit -m "chore: initial import"
```

2. Create the GitHub repository named **`calccraft`** (via GitHub UI).

3. Add remote and push:

```bash
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/calccraft.git
git push -u origin main
```

4. Open GitHub Actions tab and confirm CI is green.

## Non-goals / guardrails

- No persistence (no Room/SQLite/DataStore). History must remain in-memory only.
- No auth.
- No backend services.
- No reflection/JS evaluation; expressions must be parsed and evaluated deterministically.
