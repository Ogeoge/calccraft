package com.calccraft.domain.model

/**
 * Represents the result of a safe expression evaluation.
 * This is a sealed class that can be either a [Success] or an [Error].
 * This corresponds to the `EvalResult` data model in the contract.
 */
sealed class EvalResult(val type: String) {

    /**
     * Represents a successful evaluation.
     *
     * @property value The raw numeric result of the evaluation.
     * @property formatted The display-ready formatted string of the result (e.g., trimmed trailing zeros).
     */
    data class Success(
        val value: Double,
        val formatted: String
    ) : EvalResult("success")

    /**
     * Represents a failed evaluation.
     *
     * @property errorKind A machine-readable error category (e.g., "DivideByZero").
     * @property message A user-friendly message describing the error.
     */
    data class Error(
        val errorKind: String,
        val message: String
    ) : EvalResult("error")
}
