package internal

import QUOTE


val space = regexp(Regex("[\u0020\u3000\\t]"))


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
    return@Parser if (!state.linkLabel) success(index, null) else failure()
}

val nestable = Parser { input, index, state ->
    return@Parser if (state.depth < state.nestLimit) success(index, null) else failure()
}


// todo ここおかしいのでなおす stringも返せるようにしないとだめ
fun <T> nest(parser: Parser<T>, fallback: Parser<String>?): Parser<Any> {
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
    return Parser { input, index, state ->
        state.depth++
        val result = inner.handler(input, index, state)
        state.depth--
        result
    }
}

val language = createLanguage(
    mapOf(
        "fullParser" to { it[""]!!.many(0) },
        "simpleParser" to { it[""]!!.many(0) }
    )
)

object language2 {

    val quote = {
        val lines = seq(
            listOf(
                str(">"),
                space.option(),
                seq(listOf(notMatch(newLine), char), 1).many(0).text()
            ), 2
        ).sep(newLine, 1)
        val parser = seq(
            listOf(
                newLine.option(),
                newLine.option(),
                lineBegin,
                lines,
                newLine.option(),
                newLine.option()
            ), 3
        )
        Parser { input, index, state ->
            val result = parser.handler(input, index, state)
            when (result) {
                is Failure -> {
                    result
                }
                is Success ->{
                    val contents = result.value.toString()
                    val quoteIndex = result.index
                    if (contents.length == 1) {
                        return@Parser failure()
                    }
                    val contentParser = nest(fullParser).many(0)
                    val handler = contentParser.handler(contents, 0, state)
                    if (handler is Success){
                        success(quoteIndex,QUOTE(meregeText(handler.value)))
                    }else {
                        handler
                    }

                }
            }
        }
    }

    //FIXME ↑これの型エラー直すの無理だと思う mfm.jsは当たり前のようにAny方とunion型を使っていて、部分的にunion型をKotlinで再現可能とは言え再現するのは現実的ではない
    //      既存のパーサライブラリでMFMを再構築したほうがやっぱり早いと思う。

    val full = alt(
        listOf(
            unicodeEmoji,
            centerTag,
            smallTag,
            plainTag,
            boldTag,
            italicTag,
            strikeTag,
            urlAlt,
            big,
            boldAsta,
            italicAsta,
            boldUnder,
            italicUnder,
            codeBlock,
            inlineCode,
            quote,
            mathBlock,
            mathInline,
            strikeWave,
            fn,
            mention,
            hashtag,
            emojiCode,
            link,
            url,
            search,
            text
        )
    )

    val simple = alt(
        listOf(
            unicodeEmoji,
            emojiCode,
            text
        )
    )

    val inline = alt(
        listOf(
            unicodeEmoji,
            smallTag,
            plainTag,
            boldTag,
            italicTag,
            strikeTag,
            urlAlt,
            big,
            boldAsta,
            italicAsta,
            boldUnder,
            italicUnder,
            inlineCode,
            mathInline,
            strikeWave,
            fn,
            mention,
            hashtag,
            emojiCode,
            link
                    url,
            text
        )
    )

    val fullParser = full.many(0)
    val simpleParser = simple.many(0)
}
