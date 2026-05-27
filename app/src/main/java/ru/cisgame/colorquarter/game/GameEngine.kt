package ru.cisgame.colorquarter.game

import ru.cisgame.colorquarter.data.GameLevel
import ru.cisgame.colorquarter.data.QuarterTile
import java.util.ArrayDeque

typealias Board = List<List<QuarterTile>>

data class MoveOutcome(
    val rows: List<String>,
    val consumedMove: Boolean,
    val capturedCells: Int,
)

data class ColorSuggestion(
    val tile: QuarterTile,
    val gain: Int,
)

object GameEngine {
    fun initialRows(level: GameLevel): List<String> = level.rows

    fun parseRows(rows: List<String>): Board {
        require(rows.isNotEmpty()) { "Board cannot be empty" }
        val size = rows.first().length
        require(size > 0) { "Board rows cannot be empty" }
        require(rows.all { it.length == size }) { "Board rows must have equal length" }
        return rows.map { row -> row.map { QuarterTile.fromCode(it) } }
    }

    fun serialize(board: Board): List<String> {
        return board.map { row -> row.joinToString(separator = "") { it.code.toString() } }
    }

    fun isSolved(rows: List<String>): Boolean {
        val first = rows.firstOrNull()?.firstOrNull() ?: return false
        return rows.all { row -> row.all { it == first } }
    }

    fun capturedFraction(rows: List<String>): Float {
        if (rows.isEmpty()) return 0f
        val board = parseRows(rows)
        val captured = connectedFromOrigin(board).size
        return captured.toFloat() / (rows.size * rows.first().length).toFloat()
    }

    fun capturedCells(rows: List<String>): Int {
        if (rows.isEmpty()) return 0
        return connectedFromOrigin(parseRows(rows)).size
    }

    fun capturedPercent(rows: List<String>): Int {
        return (capturedFraction(rows) * 100).toInt().coerceIn(0, 100)
    }

    fun capturedMask(rows: List<String>): List<List<Boolean>> {
        if (rows.isEmpty()) return emptyList()
        val board = parseRows(rows)
        val captured = connectedFromOrigin(board)
        return board.mapIndexed { rowIndex, row ->
            row.mapIndexed { columnIndex, _ -> Point(rowIndex, columnIndex) in captured }
        }
    }

    fun remainingMoves(moves: Int, limit: Int): Int {
        return (limit - moves).coerceAtLeast(0)
    }

    fun expansionGain(rows: List<String>, selected: QuarterTile): Int {
        if (rows.isEmpty()) return 0
        val currentCapturedCells = connectedFromOrigin(parseRows(rows)).size
        val outcome = applyColor(rows, selected)
        return (outcome.capturedCells - currentCapturedCells).coerceAtLeast(0)
    }

    fun expansionGains(rows: List<String>, palette: List<QuarterTile>): Map<QuarterTile, Int> {
        return palette.associateWith { tile -> expansionGain(rows, tile) }
    }

    fun suggestColor(rows: List<String>, palette: List<QuarterTile>): QuarterTile? {
        return suggestMove(rows, palette)?.tile
    }

    fun suggestMove(rows: List<String>, palette: List<QuarterTile>): ColorSuggestion? {
        if (isSolved(rows)) return null
        val board = parseRows(rows)
        val current = board[0][0]
        val currentCapturedCells = connectedFromOrigin(board).size
        return palette
            .filterNot { it == current }
            .mapNotNull { tile ->
                val gain = applyColor(rows, tile).capturedCells - currentCapturedCells
                if (gain > 0) ColorSuggestion(tile, gain) else null
            }
            .maxByOrNull { suggestion -> suggestion.gain }
    }

    fun isLegalMoveTransition(
        rows: List<String>,
        nextRows: List<String>,
        palette: List<QuarterTile>,
    ): Boolean {
        if (rows == nextRows) return false
        return palette.any { selected ->
            val outcome = applyColor(rows, selected)
            outcome.consumedMove && outcome.rows == nextRows
        }
    }

    fun applyColor(rows: List<String>, selected: QuarterTile): MoveOutcome {
        val board = parseRows(rows)
        val originColor = board[0][0]
        if (originColor == selected) {
            return MoveOutcome(rows = rows, consumedMove = false, capturedCells = connectedFromOrigin(board).size)
        }

        val captured = connectedFromOrigin(board)
        val mutable = board.map { it.toMutableList() }.toMutableList()
        captured.forEach { point -> mutable[point.row][point.column] = selected }

        val expandedRows = serialize(mutable)
        val expandedBoard = parseRows(expandedRows)
        return MoveOutcome(
            rows = expandedRows,
            consumedMove = true,
            capturedCells = connectedFromOrigin(expandedBoard).size,
        )
    }

    fun applySolution(level: GameLevel): List<String> {
        return level.solution.fold(level.rows) { rows, tile -> applyColor(rows, tile).rows }
    }

    private fun connectedFromOrigin(board: Board): Set<Point> {
        val target = board[0][0]
        val visited = mutableSetOf<Point>()
        val queue = ArrayDeque<Point>()
        queue.add(Point(0, 0))

        while (!queue.isEmpty()) {
            val current = queue.removeFirst()
            if (!visited.add(current)) continue
            neighbors(current, board.size, board.first().size)
                .filter { it !in visited && board[it.row][it.column] == target }
                .forEach(queue::add)
        }
        return visited
    }

    private fun neighbors(point: Point, rows: Int, columns: Int): List<Point> {
        return listOf(
            Point(point.row - 1, point.column),
            Point(point.row + 1, point.column),
            Point(point.row, point.column - 1),
            Point(point.row, point.column + 1),
        ).filter { it.row in 0 until rows && it.column in 0 until columns }
    }
}

private data class Point(val row: Int, val column: Int)
