package tree

data class MfmInlineCodeBlock(override val children: List<MfmNode> = listOf()) : MfmNode(children)
