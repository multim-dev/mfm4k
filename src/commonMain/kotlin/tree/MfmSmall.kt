package tree

data class MfmSmall(override val children: List<MfmNode> = listOf()) : MfmNode(children)
