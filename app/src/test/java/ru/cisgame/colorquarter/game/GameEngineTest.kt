package ru.cisgame.colorquarter.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.cisgame.colorquarter.data.LevelCatalog
import ru.cisgame.colorquarter.data.LevelProgress
import ru.cisgame.colorquarter.data.QuarterTile

class GameEngineTest {
    @Test
    fun allCatalogLevelsAreSolvableByStoredSolution() {
        LevelCatalog.levels.forEach { level ->
            val solvedRows = GameEngine.applySolution(level)
            assertTrue("Level ${level.id} should be solved", GameEngine.isSolved(solvedRows))
            assertTrue("Level ${level.id} solution should fit limit", level.solution.size <= level.moveLimit)
        }
    }

    @Test
    fun selectingCurrentColorDoesNotConsumeMove() {
        val level = LevelCatalog.levels.first()
        val current = QuarterTile.fromCode(level.rows.first().first())

        val outcome = GameEngine.applyColor(level.rows, current)

        assertFalse(outcome.consumedMove)
        assertEquals(level.rows, outcome.rows)
    }

    @Test
    fun selectingNeighborColorExpandsCapturedArea() {
        val rows = listOf(
            "AB",
            "BB",
        )

        val outcome = GameEngine.applyColor(rows, QuarterTile.Sun)

        assertTrue(outcome.consumedMove)
        assertEquals(listOf("BB", "BB"), outcome.rows)
        assertEquals(4, outcome.capturedCells)
        assertTrue(GameEngine.isSolved(outcome.rows))
    }

    @Test
    fun capturedFractionUsesConnectedOriginOnly() {
        val rows = listOf(
            "AAB",
            "ABB",
            "BAA",
        )

        assertEquals(3f / 9f, GameEngine.capturedFraction(rows), 0.0001f)
        assertEquals(3, GameEngine.capturedCells(rows))
        assertEquals(0, GameEngine.capturedCells(emptyList()))
    }

    @Test
    fun capturedPercentUsesConnectedOriginOnly() {
        val rows = listOf(
            "AAB",
            "ABB",
            "BAA",
        )

        assertEquals(33, GameEngine.capturedPercent(rows))
        assertEquals(100, GameEngine.capturedPercent(listOf("AA", "AA")))
        assertEquals(0, GameEngine.capturedPercent(emptyList()))
    }

    @Test
    fun capturedMaskMarksConnectedOriginOnly() {
        val rows = listOf(
            "AAB",
            "ABB",
            "BAA",
        )

        assertEquals(
            listOf(
                listOf(true, true, false),
                listOf(true, false, false),
                listOf(false, false, false),
            ),
            GameEngine.capturedMask(rows),
        )
        assertEquals(emptyList<List<Boolean>>(), GameEngine.capturedMask(emptyList()))
    }

    @Test
    fun remainingMovesNeverGoesBelowZero() {
        assertEquals(8, GameEngine.remainingMoves(moves = 0, limit = 8))
        assertEquals(1, GameEngine.remainingMoves(moves = 7, limit = 8))
        assertEquals(0, GameEngine.remainingMoves(moves = 8, limit = 8))
        assertEquals(0, GameEngine.remainingMoves(moves = 9, limit = 8))
    }

    @Test
    fun expansionGainCountsNewlyCapturedCells() {
        val rows = listOf(
            "AB",
            "BB",
        )

        assertEquals(3, GameEngine.expansionGain(rows, QuarterTile.Sun))
        assertEquals(0, GameEngine.expansionGain(rows, QuarterTile.Lagoon))
        assertEquals(
            mapOf(
                QuarterTile.Lagoon to 0,
                QuarterTile.Sun to 3,
            ),
            GameEngine.expansionGains(rows, listOf(QuarterTile.Lagoon, QuarterTile.Sun)),
        )
    }

    @Test
    fun suggestColorChoosesLargestImmediateExpansion() {
        val rows = listOf(
            "ABB",
            "ACB",
            "CCB",
        )

        val hint = GameEngine.suggestColor(
            rows = rows,
            palette = listOf(QuarterTile.Lagoon, QuarterTile.Sun, QuarterTile.Berry),
        )
        val suggestion = GameEngine.suggestMove(
            rows = rows,
            palette = listOf(QuarterTile.Lagoon, QuarterTile.Sun, QuarterTile.Berry),
        )

        assertEquals(QuarterTile.Sun, hint)
        assertEquals(QuarterTile.Sun, suggestion?.tile)
        assertEquals(4, suggestion?.gain)
    }

    @Test
    fun suggestColorReturnsNullForSolvedBoard() {
        assertNull(
            GameEngine.suggestColor(
                rows = listOf("AA", "AA"),
                palette = listOf(QuarterTile.Lagoon, QuarterTile.Sun),
            ),
        )
        assertNull(
            GameEngine.suggestMove(
                rows = listOf("AA", "AA"),
                palette = listOf(QuarterTile.Lagoon, QuarterTile.Sun),
            ),
        )
    }

    @Test
    fun suggestColorReturnsNullWhenNoColorExpandsCapturedArea() {
        assertNull(
            GameEngine.suggestColor(
                rows = listOf(
                    "AB",
                    "BA",
                ),
                palette = listOf(QuarterTile.Lagoon, QuarterTile.Berry),
            ),
        )
        assertNull(
            GameEngine.suggestMove(
                rows = listOf(
                    "AB",
                    "BA",
                ),
                palette = listOf(QuarterTile.Lagoon, QuarterTile.Berry),
            ),
        )
    }

    @Test
    fun legalMoveTransitionRequiresConsumedPaletteMove() {
        val level = LevelCatalog.levels.first()
        val movedRows = GameEngine.applyColor(level.rows, level.solution.first()).rows
        val impossibleRows = level.rows.toMutableList().also { rows ->
            val lastRow = rows.last()
            val replacement = level.palette.first { tile -> tile.code != lastRow.last() }.code
            rows[rows.lastIndex] = lastRow.dropLast(1) + replacement
        }

        assertTrue(GameEngine.isLegalMoveTransition(level.rows, movedRows, level.palette))
        assertFalse(GameEngine.isLegalMoveTransition(level.rows, level.rows, level.palette))
        assertFalse(GameEngine.isLegalMoveTransition(level.rows, impossibleRows, level.palette))
    }

    @Test(expected = IllegalStateException::class)
    fun unknownTileCodeFailsFast() {
        GameEngine.parseRows(listOf("AZ"))
    }

    @Test
    fun firstLevelUnlocksAndNextRequiresPreviousCompletion() {
        val emptyProgress = LevelProgress()
        assertTrue(LevelCatalog.isUnlocked(1, emptyProgress))
        assertFalse(LevelCatalog.isUnlocked(2, emptyProgress))

        val progress = emptyProgress.withResult(1, 7)
        assertTrue(LevelCatalog.isUnlocked(2, progress))
    }
}
