package ru.cisgame.colorquarter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.cisgame.colorquarter.game.GameEngine

class LevelCatalogTest {
    @Test
    fun catalogHasStableReleaseShape() {
        assertEquals(36, LevelCatalog.levels.size)
        assertEquals((1..36).toList(), LevelCatalog.levels.map { it.id })
        assertEquals(LevelCatalog.levels.size, LevelCatalog.levels.map { it.title }.toSet().size)
    }

    @Test
    fun colorMarkersAreStableAndUniqueForHighContrastMode() {
        assertEquals(listOf("1", "2", "3", "4", "5"), QuarterTile.entries.map { it.marker })
        assertEquals(QuarterTile.entries.size, QuarterTile.entries.map { it.marker }.toSet().size)
    }

    @Test
    fun everyLevelIsSquareAndUsesOnlyPaletteColors() {
        LevelCatalog.levels.forEach { level ->
            assertEquals("Level ${level.id} should have ${level.size} rows", level.size, level.rows.size)
            assertTrue("Level ${level.id} needs a non-empty district", level.district.isNotBlank())
            assertTrue("Level ${level.id} needs a useful move limit", level.moveLimit >= level.solution.size)

            val paletteCodes = level.palette.map { it.code }.toSet()
            level.rows.forEach { row ->
                assertEquals("Level ${level.id} row width", level.size, row.length)
                row.forEach { code ->
                    assertTrue("Level ${level.id} contains code $code outside palette", code in paletteCodes)
                }
            }
        }
    }

    @Test
    fun firstUnfinishedFallsBackToLastLevelWhenGameIsComplete() {
        val completeProgress = LevelCatalog.levels.fold(LevelProgress()) { progress, level ->
            progress.withResult(level.id, level.solution.size)
        }

        assertEquals(LevelCatalog.levels.last(), LevelCatalog.firstUnfinished(completeProgress))
    }

    @Test
    fun isCompleteRequiresEveryCatalogLevel() {
        val emptyProgress = LevelProgress()
        val almostCompleteProgress = LevelCatalog.levels.dropLast(1).fold(LevelProgress()) { progress, level ->
            progress.withResult(level.id, level.solution.size)
        }
        val completeProgress = LevelCatalog.levels.fold(LevelProgress()) { progress, level ->
            progress.withResult(level.id, level.solution.size)
        }
        val unrelatedProgress = LevelProgress(mapOf(999 to 1))

        assertTrue(!LevelCatalog.isComplete(emptyProgress))
        assertTrue(!LevelCatalog.isComplete(almostCompleteProgress))
        assertTrue(!LevelCatalog.isComplete(unrelatedProgress))
        assertTrue(LevelCatalog.isComplete(completeProgress))
    }

    @Test
    fun completedCountIgnoresUnknownLevelIds() {
        val progress = LevelProgress(
            mapOf(
                1 to 2,
                2 to 3,
                999 to 1,
            ),
        )

        assertEquals(2, LevelCatalog.completedCount(progress))
    }

    @Test
    fun sanitizedProgressDropsUnknownAndInvalidResults() {
        val progress = LevelProgress(
            mapOf(
                1 to 2,
                2 to 0,
                3 to -4,
                4 to (LevelCatalog.byId(4)!!.moveLimit + 1),
                999 to 1,
            ),
        )

        assertEquals(mapOf(1 to 2), LevelCatalog.sanitizedProgress(progress).bestMovesByLevel)
    }

    @Test
    fun sanitizedActiveAttemptKeepsValidInProgressBoard() {
        val level = LevelCatalog.levels.first()
        val movedRows = GameEngine.applyColor(level.rows, level.solution.first()).rows
        val attempt = ActiveAttempt(
            levelId = level.id,
            rows = movedRows,
            moves = 1,
            history = listOf(rowsSnapshot(level.rows)),
        )

        assertEquals(attempt, LevelCatalog.sanitizedActiveAttempt(attempt))
    }

    @Test
    fun sanitizedActiveAttemptWithProgressRejectsLockedLevel() {
        val level = LevelCatalog.levels[1]
        val movedRows = GameEngine.applyColor(level.rows, level.solution.first()).rows
        val attempt = ActiveAttempt(
            levelId = level.id,
            rows = movedRows,
            moves = 1,
            history = listOf(rowsSnapshot(level.rows)),
        )

        assertEquals(null, LevelCatalog.sanitizedActiveAttempt(attempt, LevelProgress()))
        assertEquals(
            attempt,
            LevelCatalog.sanitizedActiveAttempt(
                attempt,
                LevelProgress().withResult(1, LevelCatalog.levels.first().solution.size),
            ),
        )
    }

    @Test
    fun sanitizedActiveAttemptRejectsImpossibleMoveTransitions() {
        val level = LevelCatalog.levels.first()
        val impossibleRows = level.rows.toMutableList().also { rows ->
            val lastRow = rows.last()
            val replacement = level.palette.first { tile -> tile.code != lastRow.last() }.code
            rows[rows.lastIndex] = lastRow.dropLast(1) + replacement
        }

        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = impossibleRows,
                    moves = 1,
                    history = listOf(rowsSnapshot(level.rows)),
                ),
            ),
        )
    }

    @Test
    fun sanitizedActiveAttemptRejectsCorruptedOrFinishedAttempts() {
        val level = LevelCatalog.levels.first()

        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(levelId = 999, rows = level.rows, moves = 1, history = emptyList()),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(levelId = level.id, rows = level.rows, moves = level.moveLimit, history = emptyList()),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(levelId = level.id, rows = listOf("AZ"), moves = 1, history = emptyList()),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(levelId = level.id, rows = level.rows, moves = 1, history = emptyList()),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = level.rows,
                    moves = 1,
                    history = listOf(rowsSnapshot(level.rows)),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = GameEngine.applyColor(level.rows, level.solution.first()).rows,
                    moves = 2,
                    history = listOf(
                        rowsSnapshot(level.rows),
                        rowsSnapshot(level.rows),
                    ),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = GameEngine.applyColor(level.rows, level.solution.first()).rows,
                    moves = 1,
                    history = listOf(rowsSnapshot(List(level.size) { "A".repeat(level.size) })),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = List(level.size) { "A".repeat(level.size) },
                    moves = 1,
                    history = listOf(rowsSnapshot(level.rows)),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = level.rows,
                    moves = 1,
                    history = listOf(rowsSnapshot(List(level.size) { "B".repeat(level.size) })),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = level.rows,
                    moves = 2,
                    history = listOf(
                        rowsSnapshot(level.rows),
                        rowsSnapshot(level.rows).replaceFirst("/", "//"),
                    ),
                ),
            ),
        )
        assertEquals(
            null,
            LevelCatalog.sanitizedActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = level.rows,
                    moves = 1,
                    history = listOf(rowsSnapshot(level.rows), rowsSnapshot(level.rows)),
                ),
            ),
        )
    }

    @Test
    fun isUnlockedRejectsUnknownLevelIds() {
        val completeProgress = LevelCatalog.levels.fold(LevelProgress()) { progress, level ->
            progress.withResult(level.id, level.solution.size)
        }

        assertTrue(!LevelCatalog.isUnlocked(0, completeProgress))
        assertTrue(!LevelCatalog.isUnlocked(37, completeProgress))
    }

    @Test
    fun starsRespectIdealAndNearIdealMoves() {
        val level = LevelCatalog.levels.first()

        assertEquals(3, LevelCatalog.starsFor(level, level.solution.size))
        assertEquals(2, LevelCatalog.starsFor(level, level.solution.size + 2))
        assertEquals(1, LevelCatalog.starsFor(level, level.solution.size + 3))
    }

    @Test
    fun threeStarMoveLimitMatchesStoredSolutionLength() {
        LevelCatalog.levels.forEach { level ->
            assertEquals(level.solution.size, LevelCatalog.threeStarMoveLimit(level))
            assertTrue(LevelCatalog.threeStarMoveLimit(level) < level.moveLimit)
        }
    }

    @Test
    fun starsStillAvailableTracksCurrentMovePace() {
        val level = LevelCatalog.levels.first { candidate ->
            candidate.moveLimit - candidate.solution.size >= 4
        }
        val ideal = level.solution.size

        assertEquals(3, LevelCatalog.starsStillAvailable(level, moves = -1))
        assertEquals(3, LevelCatalog.starsStillAvailable(level, moves = ideal))
        assertEquals(2, LevelCatalog.starsStillAvailable(level, moves = ideal + 1))
        assertEquals(2, LevelCatalog.starsStillAvailable(level, moves = ideal + 2))
        assertEquals(1, LevelCatalog.starsStillAvailable(level, moves = ideal + 3))
        assertEquals(0, LevelCatalog.starsStillAvailable(level, moves = level.moveLimit))
    }

    @Test
    fun maxStarsMatchesCatalogCapacity() {
        assertEquals(LevelCatalog.levels.size * 3, LevelCatalog.maxStars())
    }

    @Test
    fun totalStarsCountsOnlyKnownPositiveResults() {
        val first = LevelCatalog.levels[0]
        val second = LevelCatalog.levels[1]
        val third = LevelCatalog.levels[2]
        val progress = LevelProgress(
            mapOf(
                first.id to first.solution.size,
                second.id to second.solution.size + 2,
                third.id to third.solution.size + 3,
                4 to 0,
                999 to 1,
            ),
        )

        assertEquals(6, LevelCatalog.totalStars(progress))
    }

    @Test
    fun catalogLookupReturnsExpectedLevels() {
        assertEquals("Тихий двор", LevelCatalog.byId(1)?.title)
        assertEquals("Весь квартал", LevelCatalog.byId(36)?.title)
        assertNotNull(LevelCatalog.byId(LevelCatalog.firstUnfinished(LevelProgress()).id))
    }

    @Test
    fun storedSolutionsFinishEveryLevelExactlySolved() {
        LevelCatalog.levels.forEach { level ->
            assertTrue("Level ${level.id} stored solution should solve board", GameEngine.isSolved(GameEngine.applySolution(level)))
        }
    }

    private fun rowsSnapshot(rows: List<String>): String {
        return BoardSnapshotCodec.encodeRows(rows)
    }
}
