package ru.cisgame.colorquarter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelProgressTest {
    @Test
    fun withResultKeepsBestMoveCount() {
        val progress = LevelProgress()
            .withResult(levelId = 1, moves = 8)
            .withResult(levelId = 1, moves = 10)

        assertEquals(8, progress.bestMoves(1))
    }

    @Test
    fun withResultReplacesWorseMoveCount() {
        val progress = LevelProgress()
            .withResult(levelId = 1, moves = 8)
            .withResult(levelId = 1, moves = 6)

        assertEquals(6, progress.bestMoves(1))
    }

    @Test
    fun completedCountTracksRawUniqueCompletedLevelIds() {
        val progress = LevelProgress()
            .withResult(levelId = 1, moves = 6)
            .withResult(levelId = 2, moves = 7)
            .withResult(levelId = 2, moves = 5)

        assertEquals(2, progress.completedCount())
        assertTrue(progress.isCompleted(1))
        assertTrue(progress.isCompleted(2))
    }

    @Test
    fun withResultReturnsSameInstanceWhenResultIsNotBetter() {
        val progress = LevelProgress().withResult(levelId = 1, moves = 6)

        assertSame(progress, progress.withResult(levelId = 1, moves = 8))
    }

    @Test
    fun improvesBestOnlyForPositiveBetterResults() {
        val progress = LevelProgress().withResult(levelId = 1, moves = 6)

        assertTrue(LevelProgress().improvesBest(levelId = 1, moves = 6))
        assertTrue(progress.improvesBest(levelId = 1, moves = 5))
        assertTrue(!progress.improvesBest(levelId = 1, moves = 6))
        assertTrue(!progress.improvesBest(levelId = 1, moves = 7))
        assertTrue(!LevelProgress().improvesBest(levelId = 1, moves = 0))
    }

    @Test
    fun withResultIgnoresNonPositiveResults() {
        val progress = LevelProgress()

        assertSame(progress, progress.withResult(levelId = 1, moves = 0))
        assertSame(progress, progress.withResult(levelId = 1, moves = -1))
    }

    @Test
    fun progressCodecKeepsBestDuplicateResultWhenDecoding() {
        val progress = ProgressCodec.decodeProgress("1:8,1:6,1:10,2:7,2:0,broken,3:9:extra")

        assertEquals(mapOf(1 to 6, 2 to 7), progress.bestMovesByLevel)
    }

    @Test
    fun progressCodecEncodesResultsInStableLevelOrder() {
        val progress = LevelProgress(mapOf(3 to 9, 1 to 6, 2 to 7))

        assertEquals("1:6,2:7,3:9", ProgressCodec.encodeProgress(progress))
    }
}
