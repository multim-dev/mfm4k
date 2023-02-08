package internal

fun seqOrText(parsers: List<Parser<Any>>): Parser<Any> {
    return Parser { input, index, state ->
        val accum: MutableList<Any> = mutableListOf()
        var latestIndex = index
        parsers.forEachIndexed { index, parser ->
            val result = parser.handler(input, index, state)
            if (result is Success) {
                accum.add(result.value)
                latestIndex = result.index
            } else {
                if (latestIndex == index) {
                    return@Parser failure()
                } else {
                    return@Parser success(latestIndex, input.substring(index, latestIndex))
                }
            }
        }

        success(latestIndex, accum)
    }
}

val notLinkLabel = Parser { input, index, state ->
    return@Parser if(!state.linkLabel) success(index,null) else failure()
}

val nestable = Parser{
    input, index, state ->
    return@Parser if (state.depth < state.nestLimit) success(index, null) else failure()
}


// todo ここおかしいのでなおす stringも返せるようにしないとだめ
fun <T> nest(parser: Parser<T>,fallback:Parser<String>?):Parser<Any>{
    val inner = alt(
        listOf(
            seq(
                listOf(
                    nestable, parser
                ),
                1
            )
        )
    )
    return Parser{
        input, index, state ->
        state.depth++
        val result = inner.handler(input, index, state)
        state.depth--
        result
    }
}

val language = createLanguage(
    mapOf(
        "fullParser" to { it[""]!!.many(0) },
        "simpleParser" to {it[""]!!.many(0)}
    )
)
