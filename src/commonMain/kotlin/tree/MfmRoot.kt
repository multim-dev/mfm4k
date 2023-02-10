package tree

data class MfmRoot(override val children: List<MfmNode> = listOf()): MfmNode(children)
