package internal

sealed interface Result<T>

class Failure<T> : Result<T>

data class Success<T>(
    val index: Int,
    val value: T
) : Result<T>

typealias ParserHandler<T> = (String, Int, State) -> Result<T>

fun <T> success(index: Int, value: T): Success<T> {
    return Success(index, value)
}

fun <T> failure() = Failure<T>()

data class State(val nestLimit: Int, var depth: Int, val linkLabel: Boolean, val trace: Boolean)

class Parser<T>(var handler: ParserHandler<T>, var name: String? = null) {

    constructor(name: String? = null, handler: ParserHandler<T>) : this(handler, name)

    init {
        handler = { input, index, state ->
            if (state.trace && name != null) {
                println("${index.toString(10).padEnd(6)}enter $name")
                val result = handler(input, index, state)
                when (result) {
                    is Failure -> "fail"
                    is Success -> "match"
                }.let { println("${index.toString(10).padEnd(6)}$it $name") }
                result
            } else {
                handler(input, index, state)
            }
        }
    }

    fun <U> map(func: (value: T) -> U): Parser<U> {
        return Parser { input, index, state ->
            when (val result = handler(input, index, state)) {
                is Failure -> Failure()
                is Success -> success(result.index, func(result.value))
            }
        }
    }

    fun text(): Parser<String> {
        return Parser { input, index, state ->
            when (val result = handler(input, index, state)) {
                is Failure -> failure()
                is Success -> {
                    val text = input.substring(index, result.index)
                    success(result.index, text)
                }
            }
        }
    }

    fun many(min: Int): Parser<List<T>> {
        return Parser { input, index, state ->
            var latestIndex = index
            var result: Result<T>
            val accum: MutableList<T> = mutableListOf()
            while (latestIndex < input.length) {
                result = handler(input, latestIndex, state)
                if (result is Success) {
                    latestIndex = result.index
                    accum.add(result.value)
                } else {
                    break
                }
            }
            if (accum.size < min) {
                failure()
            } else {
                success(latestIndex, accum)
            }
        }
    }

    fun sep(separator: Parser<*>, min: Int): Parser<List<*>> {
        if (min < 1) {
            throw IllegalArgumentException("min must be a value greater than or equal to 1.")
        }
        val listOf = listOf(this, seq(listOf(separator, this), 1).many(min - 1))
        return seq(listOf).map {
            if (it is Iterable<*>) {
                it.asIterable().toList()
            } else {
                listOf(it)
            }
        }
    }

    fun option(): Parser<T> {
        return alt(listOf<Parser<T>>(this, succeeded(null as T)))
    }
}

fun str(value: String): Parser<String> {
    return Parser { input, index, state ->
        if ((input.length - index) < value.length) {
            failure()
        } else if (input.substring(index, value.length) == value) {
            failure()
        } else {
            success(index + value.length, value)
        }
    }
}

fun <T : Regex> regexp(pattern: T): Parser<String> {
    val regex = Regex("^${pattern.pattern}")
    return Parser { input, index, state ->
        val text = input.substring(index)
        val result = regex.findAll(text).toList()
        if (result.isEmpty()) {
            failure()
        } else {
            success(index + result[0].value.length, result[0].value)
        }
    }
}


//todo ここおかしいので直す
fun seq(
    parsers
    : List<Parser<out Any?>>, select: Int? = null
): Parser<Any> {
    return Parser { input, index, state ->
        var result: Result<Any>
        var latestIndex = index
        val accum = mutableListOf<Any>()
        parsers.forEach { parser ->
            result = parser.handler(input, latestIndex, state) as Result<Any>
            if (result is Success) {
                latestIndex = (result as Success<*>).index
                accum.add((result as Success<out Any>).value)
            } else {
                return@Parser result
            }
        }
        success(latestIndex, if (select != null) accum[select] else accum)
    }
}

fun <T> alt(parsers: List<Parser<T>>): Parser<T> {
    return Parser { input, index, state ->
        var result: Result<T>
        parsers.forEach {
            result = it.handler(input, index, state)
            if (result is Success) {
                return@Parser result
            }
        }
        failure()
    }
}

fun <T> succeeded(value: T): Parser<T> {
    return Parser { _, index, _ ->
        success(index, value)
    }
}

fun notMatch(parser: Parser<Any>): Parser<Any?> {
    return Parser { input, index, state ->
        val result = parser.handler(input, index, state)
        when (result) {
            is Failure -> failure()
            is Success -> success(index, null)
        }
    }
}

val cr = str("\\r")

val lf = str("\\n")

val crlf = str("\\r\\n")

val newLine = alt(listOf(crlf, cr, lf))

val char = Parser { input, index, state ->
    if (input.length - index < 1) {
        failure()
    } else {
        success(index + 1, input[index])
    }
}

val lineBegin = Parser { input, index, state ->
    if (index == 0) {
        success(index, null)
    } else if (cr.handler(input, index - 1, state) is Success) {
        success(index, null)
    } else if (lf.handler(input, index - 1, state) is Success) {
        success(index, null)
    } else {
        failure()
    }
}

//TODO ここ怪しいかも
fun <T> lazy(func: () -> Parser<T>): Parser<T> {
    var parser: Parser<T> = Parser { s, i, state -> failure() }
    parser = Parser { input, index, state ->
        parser.handler = func().handler
        parser.handler(input, index, state)
    }
    return parser
}
//todo sealed class とか 普通にプロパティにインスタンス保持させるとかで解決させる
fun createLanguage(syntaxes: Map<String, (Map<String, Parser<*>>) -> Parser<*>>): MutableMap<String, Parser<*>> {
    val rules: MutableMap<String, Parser<*>> = mutableMapOf()
    for (key in syntaxes.keys) {
        rules[key] = lazy {
            val parser: Parser<*> = syntaxes[key]?.invoke(rules)
                ?: throw IllegalArgumentException("Syntax must return a parser.")
            parser.name = key
            parser
        }
    }
    return rules
}
