package com.calccraft.domain

import kotlin.math.PI
import kotlin.math.E

/**
 * Token model used by the Tokenizer and ShuntingYard pipeline.
 *
 * Notes:
 * - Percent is modeled as a postfix operator.
 * - Unary minus is modeled as a distinct operator to simplify shunting-yard.
 * - Functions are single-argument in this first version (sin/cos/tan/log/sqrt).
 */
sealed interface Token {
  data class Number(val value: Double, val raw: String? = null) : Token

  data object LeftParen : Token
  data object RightParen : Token

  data class Operator(val kind: Kind) : Token {
    enum class Assoc { LEFT, RIGHT }

    enum class Kind(
      val symbol: String,
      val precedence: Int,
      val associativity: Assoc,
      val arity: Int,
      val isPostfix: Boolean = false,
    ) {
      PLUS(symbol = "+", precedence = 1, associativity = Assoc.LEFT, arity = 2),
      MINUS(symbol = "-", precedence = 1, associativity = Assoc.LEFT, arity = 2),
      TIMES(symbol = "*", precedence = 2, associativity = Assoc.LEFT, arity = 2),
      DIV(symbol = "/", precedence = 2, associativity = Assoc.LEFT, arity = 2),
      POW(symbol = "^", precedence = 4, associativity = Assoc.RIGHT, arity = 2),

      // Unary prefix minus (e.g., -3, 2*-5)
      UNARY_MINUS(symbol = "u-", precedence = 3, associativity = Assoc.RIGHT, arity = 1),

      // Postfix percent (e.g., 50%)
      PERCENT(symbol = "%", precedence = 5, associativity = Assoc.LEFT, arity = 1, isPostfix = true),
    }
  }

  data class Function(val kind: Kind) : Token {
    enum class Kind(val canonicalName: String, val aliases: Set<String>) {
      SIN("sin", setOf("sin")),
      COS("cos", setOf("cos")),
      TAN("tan", setOf("tan")),
      LOG("log", setOf("log")),
      SQRT("sqrt", setOf("sqrt", "√")),
      ;

      companion object {
        fun fromIdentifier(identifier: String): Kind? {
          val id = identifier.lowercase()
          return entries.firstOrNull { id == it.canonicalName || id in it.aliases }
        }
      }
    }
  }

  data class Constant(val kind: Kind) : Token {
    enum class Kind(val canonicalName: String, val value: Double, val aliases: Set<String>) {
      PI("pi", PI, setOf("pi", "π")),
      E("e", E, setOf("e")),
      ;

      companion object {
        fun fromIdentifier(identifier: String): Kind? {
          val id = identifier.lowercase()
          return entries.firstOrNull { id == it.canonicalName || id in it.aliases }
        }
      }
    }
  }

  /**
   * Function argument separator (comma). Not used by the first version evaluator
   * (only single-argument functions), but included to keep the model extensible.
   */
  data object Comma : Token
}
