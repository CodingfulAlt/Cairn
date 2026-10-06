package io.github.codingfulalt.cairn.core.model

enum class HabitColor(
    val key: String,
) {
    Moss("moss"),
    Mint("mint"),
    Sky("sky"),
    Lavender("lavender"),
    Rose("rose"),
    Peach("peach"),
    Sand("sand"),
    Coral("coral"),
    ;

    companion object {
        fun fromKey(key: String?): HabitColor = entries.firstOrNull { it.key == key } ?: Moss
    }
}
