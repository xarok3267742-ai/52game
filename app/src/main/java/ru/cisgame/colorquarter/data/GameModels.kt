package ru.cisgame.colorquarter.data

enum class QuarterTile(val code: Char, val title: String, val marker: String) {
    Lagoon('A', "Лагуна", "1"),
    Sun('B', "Солнце", "2"),
    Berry('C', "Ягода", "3"),
    Mint('D', "Мята", "4"),
    Violet('E', "Сирень", "5");

    companion object {
        fun fromCode(code: Char): QuarterTile {
            return entries.firstOrNull { it.code == code }
                ?: error("Unknown tile code: $code")
        }
    }
}

data class GameLevel(
    val id: Int,
    val title: String,
    val district: String,
    val size: Int,
    val rows: List<String>,
    val palette: List<QuarterTile>,
    val solution: List<QuarterTile>,
    val moveLimit: Int,
)

data class GameSettings(
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
)

data class ActiveAttempt(
    val levelId: Int,
    val rows: List<String>,
    val moves: Int,
    val history: List<String>,
)

data class LevelProgress(
    val bestMovesByLevel: Map<Int, Int> = emptyMap(),
) {
    fun isCompleted(levelId: Int): Boolean = bestMovesByLevel.containsKey(levelId)

    fun bestMoves(levelId: Int): Int? = bestMovesByLevel[levelId]

    fun completedCount(): Int = bestMovesByLevel.size

    fun improvesBest(levelId: Int, moves: Int): Boolean {
        val currentBest = bestMovesByLevel[levelId]
        return moves > 0 && (currentBest == null || moves < currentBest)
    }

    fun withResult(levelId: Int, moves: Int): LevelProgress {
        if (!improvesBest(levelId, moves)) return this
        return copy(bestMovesByLevel = bestMovesByLevel + (levelId to moves))
    }
}
