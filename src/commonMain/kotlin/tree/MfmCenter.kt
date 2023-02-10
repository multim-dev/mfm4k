package tree

data class MfmCenter(override val children: List<MfmNode> = listOf()): MfmNode(children)
