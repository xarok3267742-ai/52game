package ru.cisgame.colorquarter.data

internal object BoardSnapshotCodec {
    fun encodeRows(rows: List<String>): String {
        return rows.joinToString(separator = ROWS_SEPARATOR)
    }

    fun decodeRows(snapshot: String): List<String> {
        return snapshot.split(ROWS_SEPARATOR)
    }

    fun encodeHistory(history: List<String>): String {
        return history.joinToString(separator = HISTORY_SEPARATOR)
    }

    fun decodeHistory(rawHistory: String): List<String> {
        if (rawHistory.isBlank()) return emptyList()
        return rawHistory.split(HISTORY_SEPARATOR)
    }

    private const val ROWS_SEPARATOR = "/"
    private const val HISTORY_SEPARATOR = "|"
}
