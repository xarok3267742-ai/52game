package ru.cisgame.colorquarter.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BoardSnapshotCodecTest {
    @Test
    fun rowSnapshotsRoundTripWithStableSeparator() {
        val rows = listOf("ABCD", "BCDA", "CDAB", "DABC")

        val snapshot = BoardSnapshotCodec.encodeRows(rows)

        assertEquals("ABCD/BCDA/CDAB/DABC", snapshot)
        assertEquals(rows, BoardSnapshotCodec.decodeRows(snapshot))
    }

    @Test
    fun historySnapshotsRoundTripWithStableSeparator() {
        val history = listOf(
            BoardSnapshotCodec.encodeRows(listOf("AB", "BA")),
            BoardSnapshotCodec.encodeRows(listOf("BB", "BA")),
        )

        val rawHistory = BoardSnapshotCodec.encodeHistory(history)

        assertEquals("AB/BA|BB/BA", rawHistory)
        assertEquals(history, BoardSnapshotCodec.decodeHistory(rawHistory))
        assertEquals(emptyList<String>(), BoardSnapshotCodec.decodeHistory(""))
    }
}
