# CalcCraft Contract

This document is a lightweight contract/spec used for consistency and tooling. **CalcCraft is an Android-only, fully offline app** and must not make network calls at runtime.

## 1) API endpoints (documented only; not used by the app)

The project includes these reserved paths for contract-based tooling. Because the app is offline, **the Android app must never call them**.

- `GET /health`
  - Auth: not required
  - Notes: Local-only contract stub. If a local dev stub is ever introduced, it must return:
    - `{"status":"ok"}`

- `POST /auth/login`
  - Auth: not required
  - Notes: Not implemented (offline, no accounts). Reserved path; must not be used by the app.

- `POST /auth/logout`
  - Auth: not required
  - Notes: Not implemented (offline, no accounts). Reserved path; must not be used by the app.

## 2) Data models

These shapes define the consistent error/result/state contract used internally by the engine and UI.

### 2.1 CalcError

Consistent error shape for the engine and any future API errors. In-app errors are user-visible and non-crashing.

- `code` (string, required)
  - One of:
    - `EMPTY_EXPRESSION`
    - `INVALID_SYNTAX`
    - `UNKNOWN_TOKEN`
    - `MISMATCHED_PARENTHESES`
    - `DIVIDE_BY_ZERO`
    - `DOMAIN_ERROR`
- `message` (string, required)
  - Human-readable message safe to show in UI.
- `position` (int|null, optional)
  - Optional character index in the original expression where the error was detected.

### 2.2 EvaluationResult

Returned by `CalculatorEngine.evaluate(expression: String)`. Exactly **one** of `value` or `error` must be present.

- `value` (double|null)
- `formatted` (string|null)
  - Display-ready string representation of `value` (e.g., trimmed trailing zeros).
- `error` (CalcError|null)

### 2.3 HistoryEntry

In-memory-only history row. Cleared on app close/process death.

- `id` (long, required)
  - Monotonic sequence id assigned in-memory by `CalcViewModel`.
- `expression` (string, required)
- `resultText` (string, required)
  - Either formatted numeric result or a user-visible error message.
- `isError` (boolean, required)
- `timestampMs` (long, required)

### 2.4 CalcState

Single source of truth for Compose UI state in `CalcViewModel`.

- `currentExpression` (string, required)
- `displayValue` (string, required)
- `lastError` (CalcError|null, optional)
- `history` (List<HistoryEntry>, required)

## 3) Expression engine contract

### 3.1 Safety / implementation requirements

- Evaluation must be implemented via:
  - **Tokenizer** → **Shunting-yard (to RPN)** (or AST) → **Evaluator**
- Forbidden:
  - `eval`, JavaScript engines, reflection-based execution, or executing arbitrary code.
- All failures must return a `CalcError` (non-crashing) and be shown to the user.

### 3.2 Supported syntax

The calculator must support:

- Binary operators:
  - Addition: `+`
  - Subtraction: `-`
  - Multiplication: `*`
  - Division: `/`
  - Exponentiation: `^` (right-associative)
- Parentheses: `(`, `)`
- Unary minus:
  - Examples: `-3`, `2*-5`, `(-2)^2`
- Decimals:
  - Examples: `3.14`, `0.5`, `.5` (allowed if tokenizer supports; if not, require leading `0`)
- Percent postfix operator:
  - Example: `50%` → `0.5`
  - Percent applies to the immediately preceding value/expression.
- Square root:
  - Symbol form: `√9`
  - Function form: `sqrt(9)`
- Functions (prefix, parentheses recommended):
  - `sin(...)`, `cos(...)`, `tan(...)`, `log(...)`

Notes:
- Trigonometric functions are expected to use **radians**.
- `log(x)` is natural logarithm unless the implementation explicitly defines base-10; if defined as base-10, keep consistent in UI/help text.

### 3.3 Precedence and associativity

Expected operator precedence (highest to lowest):

1. Functions: `sin`, `cos`, `tan`, `log`, `sqrt` and symbol `√` (prefix)
2. Exponentiation: `^` (right-associative)
3. Unary minus (prefix)
4. Multiplication/division: `*`, `/` (left-associative)
5. Addition/subtraction: `+`, `-` (left-associative)
6. Percent `%` (postfix) binds to the immediately preceding value; it should be applied before lower-precedence binary ops.

(Exact internal handling can vary, but behavior should match common calculator expectations and unit tests.)

### 3.4 Formatting rules

- `EvaluationResult.formatted` should be display-ready:
  - Trim trailing zeros (e.g., `2.5000` → `2.5`)
  - Avoid scientific notation for common ranges if feasible (not strictly required).

### 3.5 Error handling contract

All errors must be non-crashing and mapped to `CalcError`.

- `EMPTY_EXPRESSION`
  - When evaluation is requested with an empty/blank expression.
- `UNKNOWN_TOKEN`
  - Unrecognized character or identifier.
- `INVALID_SYNTAX`
  - General parse/evaluation errors: missing operands, malformed number, operator misuse, stack underflow/overflow.
- `MISMATCHED_PARENTHESES`
  - Unbalanced parentheses.
- `DIVIDE_BY_ZERO`
  - Division by zero.
- `DOMAIN_ERROR`
  - Invalid domains, e.g.:
    - `sqrt(x)` with `x < 0`
    - `log(x)` with `x <= 0`

If available, `position` should point to the character index where the error was detected.

## 4) UI behavior contract

### 4.1 Screens

- **Calculator screen**
  - Shows display + keypad.
  - Provides actions to:
    - open History
    - clear (`C`)
    - backspace/delete
    - evaluate (`=`)

- **History screen**
  - Shows a list of `HistoryEntry` items: expression + resultText.
  - Clear history action (in-memory only).
  - Export history via Android Sharesheet as plain text.

### 4.2 History rules

- History is **in-memory only**.
- Must be cleared when the app/process is closed.
- Each evaluation attempt (success or error) may append a history entry, with:
  - `expression`: the expression evaluated
  - `resultText`: formatted numeric result OR user-visible error message
  - `isError`: true when `resultText` is an error
  - `timestampMs`: creation time

### 4.3 Export format

Export is plain text (MIME `text/plain`). Recommended format:

- One entry per line or a simple block per entry.
- Must include both expression and result.

Example:

```
1+2*3 = 7
sqrt(9) = 3
1/0 = Divide by zero
```

(Exact wording of error messages may vary; it must be user-safe and consistent with `CalcError.message`.)

## 5) Hardware keyboard mapping

Hardware keyboard input must be supported:

- Digits `0-9`: append digit
- Decimal point `.`: append decimal
- Operators `+ - * / ^`: append operator
- Parentheses `(` and `)`: append parentheses
- Enter / Numpad Enter: evaluate (`=`)
- Backspace: delete last character
- Escape or `C` / `c`: clear

If the platform reports both key codes and text characters, use whichever is most reliable for the given event.

## 6) Non-negotiable constraints

- Android-only, offline-only: **no network calls at runtime**.
- No authentication/accounts: `/auth/*` endpoints are reserved and unused.
- No persistence: do not add Room/SQLite/DataStore without updating the project contract.
- Safe evaluation only (tokenizer + shunting-yard/AST + evaluator).
