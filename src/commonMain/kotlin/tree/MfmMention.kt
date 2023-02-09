package tree

data class MfmMention(
    val username: String,
    val hostname: String? = null,
    override val children: List<MfmNode> = listOf()
) : MfmNode(children)
