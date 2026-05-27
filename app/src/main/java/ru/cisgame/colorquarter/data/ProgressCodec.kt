package ru.cisgame.colorquarter.data

internal object ProgressCodec {
    fun decodeProgress(raw: String): LevelProgress {
        if (raw.isBlank()) return LevelProgress()
        return raw.split(ENTRY_SEPARATOR)
            .mapNotNull(::decodeEntry)
            .fold(LevelProgress()) { progress, (levelId, moves) ->
                progress.withResult(levelId, moves)
            }
    }

    fun encodeProgress(progress: LevelProgress): String {
        return progress.bestMovesByLevel.entries
            .sortedBy { it.key }
            .joinToString(separator = ENTRY_SEPARATOR.toString()) { "${it.key}:${it.value}" }
    }

    private fun decodeEntry(item: String): Pair<Int, Int>? {
        val parts = item.split(KEY_VALUE_SEPARATOR)
        if (parts.size != 2) return null
        val levelId = parts[0].toIntOrNull()
        val moves = parts[1].toIntOrNull()
        return if (levelId != null && moves != null) levelId to moves else null
    }

    private const val ENTRY_SEPARATOR = ','
    private const val KEY_VALUE_SEPARATOR = ':'
}
