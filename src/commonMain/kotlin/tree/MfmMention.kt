package tree

data class MfmMention(
    val username: String,
    val hostname: String? = null
) : MfmNode()
