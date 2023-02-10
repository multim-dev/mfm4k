@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.combinators.SkipParser
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
class NegativeLookaheadCombinator(internal val lookaheadParser: Parser<*>) : Parser<Unit> {
    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<Unit> =
        when (lookaheadParser.tryParse(tokens, fromPosition)) {
            is Parsed -> LookaheadFoundInNegativeLookahead
            else -> ParsedValue(Unit, fromPosition)
        }
}

object LookaheadFoundInNegativeLookahead : ErrorResult()

fun not(parser: Parser<*>): SkipParser =
    skip(NegativeLookaheadCombinator(parser))
