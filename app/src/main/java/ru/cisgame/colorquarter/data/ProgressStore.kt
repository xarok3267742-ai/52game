package ru.cisgame.colorquarter.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class ProgressStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadProgress(): LevelProgress {
        val raw = preferences.getString(KEY_BEST_MOVES, "").orEmpty()
        return LevelCatalog.sanitizedProgress(ProgressCodec.decodeProgress(raw))
    }

    fun saveProgress(progress: LevelProgress) {
        val raw = ProgressCodec.encodeProgress(LevelCatalog.sanitizedProgress(progress))
        preferences.commitCritical {
            putString(KEY_BEST_MOVES, raw)
        }
    }

    fun loadSettings(): GameSettings {
        return GameSettings(
            hapticsEnabled = preferences.getBoolean(KEY_HAPTICS, true),
            reducedMotion = preferences.getBoolean(KEY_REDUCED_MOTION, false),
            highContrast = preferences.getBoolean(KEY_HIGH_CONTRAST, false),
        )
    }

    fun saveSettings(settings: GameSettings) {
        preferences.edit()
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putBoolean(KEY_REDUCED_MOTION, settings.reducedMotion)
            .putBoolean(KEY_HIGH_CONTRAST, settings.highContrast)
            .apply()
    }

    fun loadActiveAttempt(progress: LevelProgress): ActiveAttempt? {
        val levelId = preferences.getInt(KEY_ACTIVE_LEVEL_ID, -1)
        val rows = BoardSnapshotCodec.decodeRows(preferences.getString(KEY_ACTIVE_ROWS, "").orEmpty())
        val moves = preferences.getInt(KEY_ACTIVE_MOVES, 0)
        val history = BoardSnapshotCodec.decodeHistory(preferences.getString(KEY_ACTIVE_HISTORY, "").orEmpty())

        return LevelCatalog.sanitizedActiveAttempt(
            ActiveAttempt(
                levelId = levelId,
                rows = rows,
                moves = moves,
                history = history,
            ),
            progress,
        )
    }

    fun hasActiveAttemptPayload(): Boolean {
        return preferences.contains(KEY_ACTIVE_LEVEL_ID) ||
            preferences.contains(KEY_ACTIVE_ROWS) ||
            preferences.contains(KEY_ACTIVE_MOVES) ||
            preferences.contains(KEY_ACTIVE_HISTORY)
    }

    fun saveActiveAttempt(attempt: ActiveAttempt, progress: LevelProgress) {
        val sanitizedAttempt = LevelCatalog.sanitizedActiveAttempt(attempt, progress)
        if (sanitizedAttempt == null) {
            clearActiveAttempt()
            return
        }

        preferences.commitCritical {
            putInt(KEY_ACTIVE_LEVEL_ID, sanitizedAttempt.levelId)
            putString(KEY_ACTIVE_ROWS, BoardSnapshotCodec.encodeRows(sanitizedAttempt.rows))
            putInt(KEY_ACTIVE_MOVES, sanitizedAttempt.moves)
            putString(KEY_ACTIVE_HISTORY, BoardSnapshotCodec.encodeHistory(sanitizedAttempt.history))
        }
    }

    fun clearActiveAttempt() {
        preferences.commitCritical {
            remove(KEY_ACTIVE_LEVEL_ID)
            remove(KEY_ACTIVE_ROWS)
            remove(KEY_ACTIVE_MOVES)
            remove(KEY_ACTIVE_HISTORY)
        }
    }

    fun isOnboardingSeen(): Boolean = preferences.getBoolean(KEY_ONBOARDING_SEEN, false)

    fun markOnboardingSeen() {
        preferences.commitCritical {
            putBoolean(KEY_ONBOARDING_SEEN, true)
        }
    }

    fun resetProgress() {
        preferences.commitCritical {
            remove(KEY_BEST_MOVES)
            remove(KEY_ACTIVE_LEVEL_ID)
            remove(KEY_ACTIVE_ROWS)
            remove(KEY_ACTIVE_MOVES)
            remove(KEY_ACTIVE_HISTORY)
        }
    }

    @SuppressLint("ApplySharedPref")
    private inline fun SharedPreferences.commitCritical(
        block: SharedPreferences.Editor.() -> Unit,
    ) {
        val editor = edit()
        editor.block()
        if (!editor.commit()) {
            edit().apply {
                block()
                apply()
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "color_quarter_progress"
        const val KEY_BEST_MOVES = "best_moves"
        const val KEY_ACTIVE_LEVEL_ID = "active_level_id"
        const val KEY_ACTIVE_ROWS = "active_rows"
        const val KEY_ACTIVE_MOVES = "active_moves"
        const val KEY_ACTIVE_HISTORY = "active_history"
        const val KEY_HAPTICS = "haptics"
        const val KEY_REDUCED_MOTION = "reduced_motion"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    }
}
