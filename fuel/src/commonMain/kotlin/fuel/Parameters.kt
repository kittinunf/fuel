package fuel

public fun String.fillURLWithParameters(parameters: List<Pair<String, String>>): String {
    val joiner = if (this.contains("?")) {
        if (parameters.isNotEmpty()) "&"
        else ""
        // There is already a trailing ?
    } else "?"
    return this + joiner + parameters.formUrlEncode()
}

private fun List<Pair<String, String>>.formUrlEncode(): String = buildString { formUrlEncodeTo(this) }

private fun List<Pair<String, String>>.formUrlEncodeTo(out: Appendable) {
    joinTo(out, "&") {
        val key = UriCodec.encode(it.first)
        val value = UriCodec.encode(it.second)
        "$key=$value"
    }
}