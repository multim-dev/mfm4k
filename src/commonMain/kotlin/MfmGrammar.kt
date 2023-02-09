import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import tree.*

object MfmGrammar : Grammar<MfmNode>() {

    private val atMark by literalToken("@")
    private val sharp by literalToken("#")
    private val colon by literalToken(":")
    private val asterisk by literalToken("*")
    private val squareBracketStart by literalToken("[")
    private val squareBracketEnd by literalToken("]")
    private val leftParentheses by literalToken("(")
    private val rightParentheses by literalToken(")")


    private val urlRegex by regexToken(Regex("https?://[\\w\\-./?,#:\\u3000-\\u30FE\\u4E00-\\u9FA0\\uFF01-\\uFFE3]+"))

    //usernameとかemojiとか
    private val systemString by regexToken(Regex("\\w+"))

    private val text by regexToken(Regex("[^ ]+"))

    val mention: Parser<MfmMention> =
        (-atMark * systemString * optional(-atMark * systemString)).map {
            MfmMention(
                it.t1.text,
                it.t2?.text
            )
        }

    val hashtag: Parser<MfmHashtag> = (-sharp * text).map { MfmHashtag(it.text) }

    val url: Parser<MfmUrl> = urlRegex.map { MfmUrl(it.text) }

    val link: Parser<MfmLink> =
        (-squareBracketStart * text * -squareBracketEnd * -leftParentheses * urlRegex * -rightParentheses).map {
            MfmLink(
                it.t1.text,
                it.t2.text
            )
        }

    val customEmoji: Parser<MfmEmoji> = (-colon * systemString * -colon).map { MfmEmoji(it.text) }

    val bold = (-asterisk * -asterisk * text * -asterisk * -asterisk).map { MfmBold(it.text) }


    override val rootParser: Parser<MfmNode>
        get() = TODO("Not yet implemented")

}
