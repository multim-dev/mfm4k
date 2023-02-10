import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.parser.parseToEnd
import tree.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MfmGrammarTest{
    @Test
    fun mfmTest() {
        val mfm = """
            @test
            #hathtag
            https://example.com
            
            a[test](https://example.com)
            
            aahello
            
            a.a.a.a.a>aaaa[aa]a
            
            > quote
            :emoji:
            **bold**
            
            <small>small</small>
            
            <small>aaaab</small>
            
            <center>center</center>
            
            <center><small>aaa</small></center>
            <small><center>bbb</center></small>
        """.trimIndent()

        val mfmNode = MfmGrammar.parseToEnd(mfm)
        println(mfmNode)
    }

    @Test
    fun mention() {
        val s = "@test"
        val parseToEnd1 = MfmGrammar.parseToEnd(s)
        assertEquals(MfmRoot(listOf(MfmMention("test"))),parseToEnd1)

    }

    @Test
    fun mentionSimple() {
        val s = "@test"
        val parseToEnd = MfmGrammar.mention.parseToEnd(DefaultTokenizer(listOf(MfmGrammar.atMark,MfmGrammar.systemString)).tokenize(s))
        println(parseToEnd)
    }

    @Test
    fun hashtag() {
        val s = "#aaa"

        val parseToEnd1 = MfmGrammar.hashtag.parseToEnd(
            DefaultTokenizer(
                listOf(
                    MfmGrammar.sharp,
                    MfmGrammar.systemString
                )
            ).tokenize(s)
        )
        println(parseToEnd1)

        val parseToEnd = MfmGrammar.parseToEnd(s)
        println(parseToEnd)
    }

    @Test
    fun link() {
        val s= "[test](https://example.com)"

        val parseToEnd1 = MfmGrammar.link.parseToEnd(MfmGrammar.tokenizer.tokenize(s))
        println(parseToEnd1)

        val parseToEnd = MfmGrammar.parseToEnd(s)
        println(parseToEnd)
    }


    @Test
    fun bold() {
        val bold = "**aaaa**"
        val parseToEnd = MfmGrammar.parseToEnd(bold)
        println(parseToEnd)
    }

    @Test
    fun center() {

        val center = "<center>center</center>"
        val parseToEnd = MfmGrammar.parseToEnd(center)
        assertEquals(MfmRoot(listOf(MfmCenter(listOf(MfmText("center"))))),parseToEnd)
        println(parseToEnd)
        val parseToEnd1 = MfmGrammar.center.parseToEnd(MfmGrammar.tokenizer.tokenize(center))
        println(parseToEnd1)
    }

    @Test
    fun small() {
        val small = "<small>small</small>"
        val parseToEnd = MfmGrammar.parseToEnd(small)
        assertEquals(MfmRoot(listOf(MfmSmall(listOf(MfmText("small"))))),parseToEnd)
    }

    @Test
    fun smallSimple() {
        val small = "<small>small</small>"
        val parseToEnd = MfmGrammar.small.parseToEnd(MfmGrammar.tokenizer.tokenize(small))
        assertEquals(MfmSmall(listOf(MfmText("small"))),parseToEnd)
    }

    @Test
    fun centerSmallNest() {
        val s = "<center><small>aaa</small></center>"
        val parseToEnd = MfmGrammar.parseToEnd(s)
        println(parseToEnd)
    }


    @Test
    fun smallCenterNest() {
        val s = "<small><center>aaa</center></small>"
        val parseToEnd = MfmGrammar.parseToEnd(s)
        println(parseToEnd)

        val parseToEnd1 = MfmGrammar.small.parseToEnd(MfmGrammar.tokenizer.tokenize(s))
        println(parseToEnd1)
    }

    @Test
    fun text() {
        val s = "<center>aaa</center>"
        val parseToEnd = oneOrMore(MfmGrammar.text).parseToEnd(MfmGrammar.tokenizer.tokenize(s))
        println(parseToEnd)
    }
}
