sealed interface MfmNode{
    val props: Any? //TODO あとでかえる
    val children: Array<MfmNode>?
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

interface MfmQuote : MfmBlock
interface MfmSearch : MfmBlock
interface MfmCodeBlock : MfmBlock
interface MfmMathBlock : MfmBlock
interface MfmCenter : MfmBlock
