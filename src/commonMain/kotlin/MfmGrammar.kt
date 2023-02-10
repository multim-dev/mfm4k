import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.*
import tree.*

object MfmGrammar : Grammar<MfmNode>() {


    //TODO 命名が終わってるのでなんとかする

    val atMark by literalToken("@")
    val sharp by literalToken("#")
    val colon by literalToken(":")
    val asterisk by literalToken("*")
    val squareBracketStart by literalToken("[")
    val squareBracketEnd by literalToken("]")
    val leftParentheses by literalToken("(")
    val rightParentheses by literalToken(")")
    val space by literalToken(" ")

    val xmlSmallEnd by literalToken("</small>")
    val xmlSmallStart by literalToken("<small>")
    val xmlCenterEnd by literalToken("</center>")
    val xmlCenterStart by literalToken("<center>")


    val xmlTagEnd by literalToken(">")
    val tripleBackquote by literalToken("```")
    val backquote by literalToken("`")
    val mfmTagStart by literalToken("$[")

    val mfmTagEnd by literalToken("]")

    val lineBegin by regexToken(Regex("^"))

    val urlRegex by regexToken(Regex("https?://[\\w\\-./?,#:\\u3000-\\u30FE\\u4E00-\\u9FA0\\uFF01-\\uFFE3]+"))

    val newLine by regexToken(Regex("[\r\n]+"), ignore = true)

    //usernameとかemojiとか
    val systemString by regexToken(Regex("\\w+"))

    val noSpaceTextRegex by regexToken(Regex("[^ ]+?"))

    val systemMatch: Parser<String> = (atMark or sharp or colon or asterisk or squareBracketStart or squareBracketEnd or leftParentheses or rightParentheses or xmlTagEnd or xmlCenterStart or xmlSmallStart).map { it.text }

    val noSpaceTextWithoutSystemMatch: Parser<String> = oneOrMore(noSpaceTextRegex or systemString or space).map {it.joinToString(""){ tokenMatch -> tokenMatch.text}   }


    val noSpaceText: Parser<String> = (noSpaceTextWithoutSystemMatch or systemMatch).map { it }

    val textRegex by regexToken(Regex(".+?")).map { it.text }
    val text: Parser<MfmText> = (noSpaceText or textRegex).map { MfmText(it) }

    val textWithoutSystemMatch = (noSpaceTextWithoutSystemMatch or textRegex).map { MfmText(it) }

    val mention: Parser<MfmMention> =
        (-atMark * systemString * optional(-atMark * systemString)).map {
            MfmMention(
                it.t1.text,
                it.t2?.text
            )
        }


    //インライン
    val hashtag: Parser<MfmHashtag> = (-sharp * noSpaceText).map { MfmHashtag(it) }


    //インライン
    val url: Parser<MfmUrl> = urlRegex.map { MfmUrl(it.text) }


    //インライン
    val link: Parser<MfmLink> =
        (-squareBracketStart * textWithoutSystemMatch * -squareBracketEnd * -leftParentheses * urlRegex * -rightParentheses).map {
            MfmLink(
                it.t1.text,
                it.t2.text
            )
        }

    val customEmoji: Parser<MfmEmoji> =
        (-colon * systemString * -colon).map { MfmEmoji(it.text) } //インライン

    val bold: Parser<MfmBold> =
        (-asterisk * -asterisk * (parser(this::inline)) * -asterisk * -asterisk).map { MfmBold(
            listOf(it)
        ) } //入れ子 インライン

    val small: Parser<MfmSmall> =
        (-xmlSmallStart * zeroOrMore(parser(this::inline) )and not(xmlSmallEnd) * -xmlSmallEnd).map {
            MfmSmall(it)
        } //入れ子 インライン

    val center: Parser<MfmCenter> =
        (-xmlCenterStart * zeroOrMore(parser(this::inline)) * -xmlCenterEnd).map {
            MfmCenter(it)
        } //入れ子

    val quote = (-newLine * -xmlTagEnd * -oneOrMore(space) * text).map { MfmQuote(it.text) }

    val codeBlock = (-tripleBackquote * text * -tripleBackquote)

    val inlineCodeBlock =
        (-backquote * text * -backquote).map { MfmInlineCodeBlock(listOf(MfmText(it.text))) } //インライン

    val inline: Parser<MfmNode> =
        (hashtag or url or link or customEmoji or bold  or quote or inlineCodeBlock or mention or small or text)

    val mfm: Parser<MfmNode> = (codeBlock or center or inline)

    override val rootParser: Parser<MfmNode> = zeroOrMore(mfm).map { MfmRoot(it) }


}
