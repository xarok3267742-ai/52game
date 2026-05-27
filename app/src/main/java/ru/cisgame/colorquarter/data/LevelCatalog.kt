package ru.cisgame.colorquarter.data

import ru.cisgame.colorquarter.game.GameEngine
import kotlin.math.min

object LevelCatalog {
    val levels: List<GameLevel> = listOf(
        level(1, "Тихий двор", "Учебный квартал", 5, "ABCDABC", 2),
        level(2, "Летний сквер", "Учебный квартал", 5, "ACBDACB", 2),
        level(3, "Крыши после дождя", "Учебный квартал", 5, "ADBCADE", 2),
        level(4, "Переулок с фонарями", "Центр", 5, "BACDBACD", 2),
        level(5, "Речной поворот", "Центр", 5, "CADBCAD", 2),
        level(6, "Площадь у вокзала", "Центр", 5, "DBCABDCA", 2),
        level(7, "Северный двор", "Центр", 5, "EADBCEAD", 2),
        level(8, "Утренняя линия", "Центр", 5, "BDEACBDE", 2),
        level(9, "Теплый бульвар", "Старый город", 5, "CEBADCEB", 2),
        level(10, "Сиреневый проезд", "Старый город", 5, "DEACBDEA", 2),
        level(11, "Большая клумба", "Старый город", 6, "ABCDEABCD", 3),
        level(12, "Трамвайное кольцо", "Старый город", 6, "ADBECADBE", 3),
        level(13, "Лавки у фонтана", "Парковый район", 6, "BECADBEC", 3),
        level(14, "Солнечный мост", "Парковый район", 6, "CDAEBCDAE", 3),
        level(15, "Двор с муралом", "Парковый район", 6, "EBCDAEBCD", 3),
        level(16, "Линия каштанов", "Парковый район", 6, "DAEBCDAEB", 3),
        level(17, "Вечерний рынок", "Новый берег", 6, "ACEBDACEB", 3),
        level(18, "Окна у набережной", "Новый берег", 6, "BDACEBDAC", 3),
        level(19, "Переход через парк", "Новый берег", 6, "ECADBECAD", 3),
        level(20, "Квартал мастеров", "Новый берег", 6, "DBEACDBEA", 3),
        level(21, "Малый проспект", "Высокий район", 6, "CABDECABDE", 3),
        level(22, "Пятый подъезд", "Высокий район", 6, "EDACBEDAC", 3),
        level(23, "Парк на холме", "Высокий район", 7, "ABCDEABCDE", 4),
        level(24, "Дальняя аллея", "Высокий район", 7, "ADECBADECB", 4),
        level(25, "Ночной двор", "Огни города", 7, "BCEDAECDBA", 4),
        level(26, "Широкая набережная", "Огни города", 7, "CEADBCEADB", 4),
        level(27, "Теплая остановка", "Огни города", 7, "DEBACDEBAC", 4),
        level(28, "Свет в окнах", "Огни города", 7, "EACDBEACDB", 4),
        level(29, "Новый маршрут", "Финальный район", 7, "ADBCEADBCEA", 4),
        level(30, "Площадь пяти цветов", "Финальный район", 7, "BECADBECADB", 4),
        level(31, "Береговая дуга", "Финальный район", 7, "CDAEBCDAEBC", 4),
        level(32, "Чистый проспект", "Финальный район", 7, "DBEACDBEACD", 4),
        level(33, "Сквозной проход", "Мастерский блок", 7, "EBCDAEBCDAE", 4),
        level(34, "Двор без лишнего", "Мастерский блок", 7, "ACEDBACEDBA", 4),
        level(35, "Ровная сетка", "Мастерский блок", 7, "BDCAEBDCAEB", 4),
        level(36, "Весь квартал", "Мастерский блок", 7, "CADEBCADEBC", 4),
    )

    fun byId(id: Int): GameLevel? = levels.firstOrNull { it.id == id }

    fun firstUnfinished(progress: LevelProgress): GameLevel {
        return levels.firstOrNull { !progress.isCompleted(it.id) } ?: levels.last()
    }

    fun completedCount(progress: LevelProgress): Int {
        return levels.count { progress.isCompleted(it.id) }
    }

    fun totalStars(progress: LevelProgress): Int {
        return levels.sumOf { level ->
            progress.bestMoves(level.id)
                ?.takeIf { it > 0 }
                ?.let { moves -> starsFor(level, moves) }
                ?: 0
        }
    }

    fun maxStars(): Int {
        return levels.size * STARS_PER_LEVEL
    }

    fun isComplete(progress: LevelProgress): Boolean {
        return levels.isNotEmpty() && levels.all { progress.isCompleted(it.id) }
    }

    fun sanitizedProgress(progress: LevelProgress): LevelProgress {
        return LevelProgress(
            progress.bestMovesByLevel
                .filter { (levelId, moves) ->
                    val level = byId(levelId)
                    level != null && moves in 1..level.moveLimit
                },
        )
    }

    fun sanitizedActiveAttempt(attempt: ActiveAttempt): ActiveAttempt? {
        val level = byId(attempt.levelId) ?: return null
        if (attempt.moves <= 0 || attempt.moves >= level.moveLimit) return null
        if (!isValidRowsForLevel(attempt.rows, level)) return null
        if (attempt.rows == level.rows) return null
        if (isSolvedRows(attempt.rows)) return null
        if (attempt.history.size != attempt.moves) return null
        if (attempt.history.firstOrNull() != BoardSnapshotCodec.encodeRows(level.rows)) return null
        val historyRows = attempt.history.map(BoardSnapshotCodec::decodeRows)
        if (historyRows.any { rows -> !isValidRowsForLevel(rows, level) }) return null
        if (historyRows.any(::isSolvedRows)) return null
        if (!hasOnlyLegalMoveTransitions(level, historyRows + listOf(attempt.rows))) return null
        return attempt
    }

    fun sanitizedActiveAttempt(attempt: ActiveAttempt, progress: LevelProgress): ActiveAttempt? {
        return sanitizedActiveAttempt(attempt)
            ?.takeIf { sanitizedAttempt -> isUnlocked(sanitizedAttempt.levelId, progress) }
    }

    fun isUnlocked(levelId: Int, progress: LevelProgress): Boolean {
        val level = byId(levelId) ?: return false
        return level.id == 1 || progress.isCompleted(level.id - 1)
    }

    fun starsFor(level: GameLevel, moves: Int): Int {
        val ideal = threeStarMoveLimit(level)
        return when {
            moves <= ideal -> STARS_PER_LEVEL
            moves <= ideal + 2 -> 2
            else -> 1
        }
    }

    fun starsStillAvailable(level: GameLevel, moves: Int): Int {
        val usedMoves = moves.coerceAtLeast(0)
        val ideal = threeStarMoveLimit(level)
        return when {
            usedMoves <= ideal -> STARS_PER_LEVEL
            usedMoves <= ideal + 2 -> 2
            usedMoves < level.moveLimit -> 1
            else -> 0
        }
    }

    fun threeStarMoveLimit(level: GameLevel): Int = level.solution.size

    private fun isValidRowsForLevel(rows: List<String>, level: GameLevel): Boolean {
        if (rows.size != level.size) return false
        val paletteCodes = level.palette.map { it.code }.toSet()
        return rows.all { row ->
            row.length == level.size && row.all { code -> code in paletteCodes }
        }
    }

    private fun isSolvedRows(rows: List<String>): Boolean {
        val first = rows.firstOrNull()?.firstOrNull() ?: return false
        return rows.all { row -> row.all { code -> code == first } }
    }

    private fun hasOnlyLegalMoveTransitions(level: GameLevel, states: List<List<String>>): Boolean {
        return states.zipWithNext().all { (previous, next) ->
            GameEngine.isLegalMoveTransition(previous, next, level.palette)
        }
    }

    private fun level(
        id: Int,
        title: String,
        district: String,
        size: Int,
        sequence: String,
        extraMoves: Int,
    ): GameLevel {
        require(sequence.length >= 2) { "Level $id needs at least two colors" }
        require(sequence.zipWithNext().none { it.first == it.second }) {
            "Level $id has repeated adjacent colors"
        }
        val rows = List(size) { row ->
            CharArray(size) { column ->
                sequence[min(row + column, sequence.lastIndex)]
            }.concatToString()
        }
        val palette = sequence.map { QuarterTile.fromCode(it) }.distinct()
        val solution = sequence.drop(1).map { QuarterTile.fromCode(it) }
        return GameLevel(
            id = id,
            title = title,
            district = district,
            size = size,
            rows = rows,
            palette = palette,
            solution = solution,
            moveLimit = solution.size + extraMoves,
        )
    }

    private const val STARS_PER_LEVEL = 3
}
