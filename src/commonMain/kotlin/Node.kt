sealed interface MfmNode{
    val props: Any?  //TODO あとでかえる
        get() = null
    val children: List<MfmNode>?
}

sealed interface MfmSimpleNode


sealed interface MfmBlock : MfmNode
sealed interface MfmInline : MfmNode
interface MfmUnicodeEmoji : MfmSimpleNode,MfmInline
interface MfmEmojiCode : MfmSimpleNode,MfmInline

interface MfmBold : MfmInline
interface MfmSmall : MfmInline
interface MfmItalic : MfmInline
interface MfmStrike : MfmInline
interface MfmInlineCode : MfmInline
interface MfmMathInline : MfmInline
interface MfmMention : MfmInline
interface MfmHashtag : MfmInline
interface MfmUrl : MfmInline
interface MfmLink : MfmInline
interface MfmFn : MfmInline
interface MfmPlain : MfmInline

interface MfmText : MfmSimpleNode,MfmInline

val TEXT:(String)->MfmText = {
    object : MfmText {
        override val children: List<MfmNode>
            get() = listOf()

        override val props: Any
            get() = it
    }
}

interface MfmQuote : MfmBlock

val QUOTE:(List<MfmNode>)->MfmQuote = {
    object : MfmQuote {
        override val props: Any?
            get() = null
        override val children: List<MfmNode>
            get() = it

    }
}

interface MfmSearch : MfmBlock
interface MfmCodeBlock : MfmBlock
interface MfmMathBlock : MfmBlock
interface MfmCenter : MfmBlock
