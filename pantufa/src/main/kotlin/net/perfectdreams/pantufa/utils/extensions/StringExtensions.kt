package net.perfectdreams.pantufa.utils.extensions

fun String.normalize(): String {
    val replacements = mapOf('ę' to 'e', 'š' to 's')

    return this.map { replacements[it] ?: it }.joinToString("")
}

/**
 * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Double] number
 *
 * This also converts a normal number (non shortened) to a [Double]
 *
 * @param input the shortened number
 * @return      the number as long or null if it is a non valid (example: text) number
 */
fun String.convertShortenedNumberToDouble(): Double? {
    val inputAsLowerCase = this.lowercase()

    return when {
        inputAsLowerCase.endsWith("m") -> inputAsLowerCase.removeSuffix("m").toDoubleOrNull()?.times(1_000_000)
        inputAsLowerCase.endsWith("kk") -> inputAsLowerCase.removeSuffix("kk").toDoubleOrNull()?.times(1_000_000)
        inputAsLowerCase.endsWith("k") -> inputAsLowerCase.removeSuffix("k").toDoubleOrNull()?.times(1_000)
        else -> inputAsLowerCase.toDoubleOrNull()
    }
}

fun String.convertShortenedNumberToLong(): Long? {
    val inputAsLowerCase = this.lowercase()

    return when {
        inputAsLowerCase.endsWith("m") -> inputAsLowerCase.removeSuffix("m").toLongOrNull()?.times(1_000_000)
        inputAsLowerCase.endsWith("kk") -> inputAsLowerCase.removeSuffix("kk").toLongOrNull()?.times(1_000_000)
        inputAsLowerCase.endsWith("k") -> inputAsLowerCase.removeSuffix("k").toLongOrNull()?.times(1_000)
        else -> inputAsLowerCase.toLongOrNull()
    }
}