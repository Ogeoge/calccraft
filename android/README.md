# CalcCraft (Android Module)

This directory contains the Android project for CalcCraft, a calculator app built with Kotlin and Jetpack Compose.

See the [root README.md](../README.md) for a complete project overview.

## Architecture

The app follows a modern Android architecture with a unidirectional data flow pattern inspired by MVI/Redux:

-   **UI Layer (`ui/`)**: Built entirely with Jetpack Compose and Material 3. It consists of screens (`CalculatorScreen`, `HistoryScreen`), reusable components (`CalcButton`), and hardware keyboard event mapping.
-   **State Management (`state/`)**: A central `CalculatorViewModel` holds the UI state in a `StateFlow`. UI events and keyboard inputs are translated into `CalculatorIntent` objects. A pure `CalculatorReducer` function takes the current `CalculatorState` and an `Intent` to produce a new state. This keeps the core logic decoupled from Android framework and easily testable.
-   **Domain Layer (`domain/`)**: Contains the core business logic, completely independent of the UI and Android platform.
    -   **Models**: `EvalResult`, `HistoryEntry`, etc.
    -   **Expression Engine**: A safe, deterministic pipeline for evaluating mathematical expressions:
        1.  `Tokenizer`: Converts the input string into a stream of tokens.
        2.  `ShuntingYard`: Converts the token stream into Reverse Polish Notation (RPN).
        3.  `RpnEvaluator`: Evaluates the RPN stack to produce a result.

This separation ensures that the complex expression parsing logic is robust and can be unit-tested thoroughly.

## Run

1.  Open this `android/` directory in Android Studio.
2.  Wait for the Gradle sync to complete.
3.  Select the `app` run configuration.
4.  Run on an emulator or a physical device.

## Test & Lint

All commands should be run from this `android/` directory.

### Unit Tests

Run unit tests for the expression engine and the reducer:

```bash
./gradlew test
```

### Linting

Check code style with ktlint (configured via the `ktlint-gradle` plugin):

```bash
./gradlew ktlintCheck
```

To auto-format code:

```bash
./gradlew ktlintFormat
```

### Build

Assemble a debug build:

```bash
./gradlew assembleDebug
```

## Hardware Keyboard Support

Hardware keyboard input is a core feature. The mapping from `KeyEvent` to `CalculatorIntent` is handled in `ui/keyboard/HardwareKeyMapper.kt`.

-   **Digits** (`0`-`9`) → `Append`
-   **Operators** (`+`, `-`, `*`, `/`) → `Append`
-   **Parentheses** (`(`, `)`) → `Append`
-   **Decimal** (`.`) → `Append`
-   **Enter** key → `Evaluate`
-   **Backspace** key → `Delete`
-   **Escape** key → `Clear` (optional, may be implemented)
