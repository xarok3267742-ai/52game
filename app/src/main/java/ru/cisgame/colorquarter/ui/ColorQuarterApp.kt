package ru.cisgame.colorquarter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.cisgame.colorquarter.BuildConfig
import ru.cisgame.colorquarter.R
import ru.cisgame.colorquarter.data.ActiveAttempt
import ru.cisgame.colorquarter.data.BoardSnapshotCodec
import ru.cisgame.colorquarter.data.GameLevel
import ru.cisgame.colorquarter.data.GameSettings
import ru.cisgame.colorquarter.data.LevelCatalog
import ru.cisgame.colorquarter.data.LevelProgress
import ru.cisgame.colorquarter.data.ProgressStore
import ru.cisgame.colorquarter.data.QuarterTile
import ru.cisgame.colorquarter.game.GameEngine
import ru.cisgame.colorquarter.ui.theme.ColorQuarterTheme
import ru.cisgame.colorquarter.ui.theme.Ink
import ru.cisgame.colorquarter.ui.theme.Line
import ru.cisgame.colorquarter.ui.theme.MutedInk
import ru.cisgame.colorquarter.ui.theme.Paper
import ru.cisgame.colorquarter.ui.theme.Success
import ru.cisgame.colorquarter.ui.theme.Warning
import ru.cisgame.colorquarter.ui.theme.uiColor
import ru.cisgame.colorquarter.ui.theme.uiMarkerColor

private enum class AppScreen {
    Onboarding,
    Home,
    Game,
    Settings,
    AboutPrivacy,
}

private enum class LevelRunResult {
    Win,
    Loss,
}

@Composable
fun ColorQuarterApp() {
    val context = LocalContext.current.applicationContext
    val store = remember { ProgressStore(context) }
    val initialProgress = remember { store.loadProgress() }
    val storedActiveAttempt = remember {
        val activeAttempt = store.loadActiveAttempt(initialProgress)
        if (activeAttempt == null && store.hasActiveAttemptPayload()) {
            store.clearActiveAttempt()
        }
        activeAttempt
    }
    var restoredAttempt by remember { mutableStateOf(storedActiveAttempt) }
    var progress by remember { mutableStateOf(initialProgress) }
    var settings by remember { mutableStateOf(store.loadSettings()) }
    var currentScreen by rememberSaveable {
        mutableStateOf(
            when {
                !store.isOnboardingSeen() -> AppScreen.Onboarding
                storedActiveAttempt != null -> AppScreen.Game
                else -> AppScreen.Home
            },
        )
    }
    var activeLevelId by rememberSaveable {
        mutableIntStateOf(storedActiveAttempt?.levelId ?: LevelCatalog.firstUnfinished(progress).id)
    }

    fun saveSettings(next: GameSettings) {
        settings = next
        store.saveSettings(next)
    }

    fun saveProgress(next: LevelProgress) {
        progress = next
        store.saveProgress(next)
    }

    ColorQuarterTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (currentScreen) {
                AppScreen.Onboarding -> OnboardingScreen(
                    onDone = {
                        store.markOnboardingSeen()
                        currentScreen = AppScreen.Home
                    },
                )

                AppScreen.Home -> HomeScreen(
                    progress = progress,
                    onOpenSettings = { currentScreen = AppScreen.Settings },
                    onStartLevel = { level ->
                        restoredAttempt = null
                        store.clearActiveAttempt()
                        activeLevelId = level.id
                        currentScreen = AppScreen.Game
                    },
                )

                AppScreen.Game -> {
                    val level = LevelCatalog.byId(activeLevelId)
                    if (level == null) {
                        ErrorScreen(onHome = { currentScreen = AppScreen.Home })
                    } else {
                        GameScreen(
                            level = level,
                            progress = progress,
                            settings = settings,
                            initialAttempt = restoredAttempt?.takeIf { it.levelId == level.id },
                            onBack = { currentScreen = AppScreen.Home },
                            onComplete = { levelId, moves ->
                                saveProgress(progress.withResult(levelId, moves))
                            },
                            onNext = { nextId ->
                                activeLevelId = nextId
                            },
                            onSaveActiveAttempt = { attempt ->
                                restoredAttempt = null
                                store.saveActiveAttempt(attempt, progress)
                            },
                            onClearActiveAttempt = {
                                restoredAttempt = null
                                store.clearActiveAttempt()
                            },
                        )
                    }
                }

                AppScreen.Settings -> SettingsScreen(
                    settings = settings,
                    onSettingsChange = ::saveSettings,
                    onBack = { currentScreen = AppScreen.Home },
                    onOpenAboutPrivacy = { currentScreen = AppScreen.AboutPrivacy },
                    onResetProgress = {
                        store.resetProgress()
                        progress = LevelProgress()
                    },
                )

                AppScreen.AboutPrivacy -> AboutPrivacyScreen(
                    onBack = { currentScreen = AppScreen.Settings },
                )
            }
        }
    }
}

@Composable
private fun ScreenFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Image(
            painter = painterResource(R.drawable.app_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.62f),
        )
        content()
    }
}

@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedInk,
                )
                Spacer(Modifier.height(26.dp))
                OnboardingMosaic()
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OnboardingPoint(
                    number = "1",
                    title = stringResource(R.string.onboarding_title_1),
                    body = stringResource(R.string.onboarding_body_1),
                )
                OnboardingPoint(
                    number = "2",
                    title = stringResource(R.string.onboarding_title_2),
                    body = stringResource(R.string.onboarding_body_2),
                )
                OnboardingPoint(
                    number = "3",
                    title = stringResource(R.string.onboarding_title_3),
                    body = stringResource(R.string.onboarding_body_3),
                )
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 54.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.play))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OnboardingMosaic() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(R.drawable.onboarding_illustration),
            contentDescription = stringResource(R.string.onboarding_visual_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.68f)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
private fun OnboardingPoint(number: String, title: String, body: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Ink),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MutedInk)
        }
    }
}

@Composable
private fun HomeScreen(
    progress: LevelProgress,
    onOpenSettings: () -> Unit,
    onStartLevel: (GameLevel) -> Unit,
) {
    val levels = LevelCatalog.levels
    val nextLevel = LevelCatalog.firstUnfinished(progress)
    val isComplete = LevelCatalog.isComplete(progress)
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = Ink,
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedInk,
                    )
                }
                IconTextButton(
                    iconRes = R.drawable.ic_nav_settings,
                    description = stringResource(R.string.settings),
                    onClick = onOpenSettings,
                )
            }

            Spacer(Modifier.height(16.dp))
            HomeIllustration()
            Spacer(Modifier.height(14.dp))
            ProgressPanel(progress = progress, total = levels.size)
            if (isComplete) {
                Spacer(Modifier.height(12.dp))
                CompletionPanel(total = levels.size)
            }
            Spacer(Modifier.height(16.dp))
            NextGoalPanel(
                nextLevel = nextLevel,
                progress = progress,
                total = levels.size,
                isComplete = isComplete,
                onStartLevel = { onStartLevel(nextLevel) },
            )

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.levels), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))

            if (levels.isEmpty()) {
                EmptyState()
            } else {
                HomeLevelGrid(
                    levels = levels,
                    progress = progress,
                    onStartLevel = onStartLevel,
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HomeIllustration() {
    Image(
        painter = painterResource(R.drawable.home_illustration),
        contentDescription = stringResource(R.string.home_visual_description),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun CompletionPanel(total: Int) {
    Surface(
        color = Color(0xFFE9F6EF),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Success.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.completion_title),
                style = MaterialTheme.typography.titleLarge,
                color = Success,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.completion_body, total),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
        }
    }
}

@Composable
private fun ProgressPanel(progress: LevelProgress, total: Int) {
    val completed = LevelCatalog.completedCount(progress)
    val totalStars = LevelCatalog.totalStars(progress)
    val maxStars = LevelCatalog.maxStars()
    val fraction = if (total == 0) 0f else completed / total.toFloat()
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.progress), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.progress_count_format, completed, total),
                    style = MaterialTheme.typography.titleMedium,
                    color = Success,
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ProgressMetric(
                    label = stringResource(R.string.completed),
                    value = stringResource(R.string.progress_count_format, completed, total),
                    valueColor = Success,
                    modifier = Modifier.weight(1f),
                )
                ProgressMetric(
                    label = stringResource(R.string.stars),
                    value = stringResource(R.string.stars_count_format, totalStars, maxStars),
                    valueColor = Color(0xFFC98200),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NextGoalPanel(
    nextLevel: GameLevel,
    progress: LevelProgress,
    total: Int,
    isComplete: Boolean,
    onStartLevel: () -> Unit,
) {
    val completed = LevelCatalog.completedCount(progress)
    val remaining = (total - completed).coerceAtLeast(0)
    val best = progress.bestMoves(nextLevel.id)
    Surface(
        color = Color(0xFFEAF7F3),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Success.copy(alpha = 0.38f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.next_goal_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Success,
                )
                Text(
                    text = stringResource(R.string.next_goal_remaining_format, remaining),
                    style = MaterialTheme.typography.labelLarge,
                    color = MutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.next_goal_level_format, nextLevel.id, nextLevel.title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.next_goal_meta_format,
                    nextLevel.district,
                    nextLevel.moveLimit,
                    LevelCatalog.threeStarMoveLimit(nextLevel),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
            )
            if (best != null || isComplete) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = best?.let { stringResource(R.string.next_goal_best_format, it) }
                        ?: stringResource(R.string.next_goal_complete_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (best != null) Success else MutedInk,
                )
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onStartLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    stringResource(
                        if (isComplete) R.string.replay_final_level_format else R.string.continue_level_format,
                        nextLevel.title,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeLevelGrid(
    levels: List<GameLevel>,
    progress: LevelProgress,
    onStartLevel: (GameLevel) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 520.dp) 4 else 3
        Column {
            levels.chunked(columns).forEach { rowLevels ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                ) {
                    rowLevels.forEach { level ->
                        LevelTile(
                            level = level,
                            progress = progress,
                            unlocked = LevelCatalog.isUnlocked(level.id, progress),
                            onClick = { onStartLevel(level) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - rowLevels.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.defaultMinSize(minHeight = 42.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LevelTile(
    level: GameLevel,
    progress: LevelProgress,
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completed = progress.isCompleted(level.id)
    val best = progress.bestMoves(level.id)
    val bestStars = best?.let { LevelCatalog.starsFor(level, it) }
    val lockedText = stringResource(R.string.locked_level)
    val lockedLevelDescription = stringResource(
        R.string.accessibility_locked_level,
        level.id,
        level.title,
        lockedText,
    )
    val levelDescription = if (best != null && bestStars != null) {
        stringResource(
            R.string.accessibility_completed_level,
            level.id,
            level.title,
            pluralStringResource(
                R.plurals.stars_count_accessibility,
                bestStars,
                bestStars,
            ),
            best,
        )
    } else {
        stringResource(R.string.accessibility_available_level, level.id, level.title, level.district)
    }
    Surface(
        color = when {
            !unlocked -> MaterialTheme.colorScheme.surfaceVariant
            completed -> Color(0xFFE9F6EF)
            else -> Color.White
        },
        contentColor = if (unlocked) Ink else MutedInk,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (completed) Success.copy(alpha = 0.36f) else Line),
        modifier = modifier
            .defaultMinSize(minHeight = 106.dp)
            .alpha(if (unlocked) 1f else 0.62f)
            .clickable(enabled = unlocked, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = if (unlocked) {
                    levelDescription
                } else {
                    lockedLevelDescription
                }
            },
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = level.id.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = if (completed) Success else Ink,
            )
            Column {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = best?.let {
                        stringResource(
                            R.string.level_result_with_stars_format,
                            "★".repeat(LevelCatalog.starsFor(level, it)),
                            it,
                        )
                    }
                        ?: if (unlocked) level.district else stringResource(R.string.locked_level),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GameScreen(
    level: GameLevel,
    progress: LevelProgress,
    settings: GameSettings,
    initialAttempt: ActiveAttempt?,
    onBack: () -> Unit,
    onComplete: (Int, Int) -> Unit,
    onNext: (Int) -> Unit,
    onSaveActiveAttempt: (ActiveAttempt) -> Unit,
    onClearActiveAttempt: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var rows by rememberSaveable(level.id) { mutableStateOf(initialAttempt?.rows ?: GameEngine.initialRows(level)) }
    var moves by rememberSaveable(level.id) { mutableIntStateOf(initialAttempt?.moves ?: 0) }
    var history by rememberSaveable(level.id) { mutableStateOf(initialAttempt?.history ?: emptyList()) }
    var result by rememberSaveable(level.id) { mutableStateOf<LevelRunResult?>(null) }
    var resultPreviousBest by rememberSaveable(level.id) { mutableStateOf<Int?>(null) }
    var resultImprovesBest by rememberSaveable(level.id) { mutableStateOf(false) }
    var showLeaveConfirm by rememberSaveable(level.id) { mutableStateOf(false) }
    var showRestartConfirm by rememberSaveable(level.id) { mutableStateOf(false) }
    var hintCode by rememberSaveable(level.id) { mutableStateOf<String?>(null) }
    var hintRequested by rememberSaveable(level.id) { mutableStateOf(false) }
    var hintGain by rememberSaveable(level.id) { mutableIntStateOf(0) }
    var lastMoveGain by rememberSaveable(level.id) { mutableIntStateOf(0) }
    val currentTile = QuarterTile.fromCode(rows.first().first())
    val hintTile = hintCode?.firstOrNull()?.let { QuarterTile.fromCode(it) }
    val captured = GameEngine.capturedFraction(rows)
    val capturedPercent = GameEngine.capturedPercent(rows)
    val remainingMoves = GameEngine.remainingMoves(moves, level.moveLimit)
    val paletteGains = remember(rows, level.palette) { GameEngine.expansionGains(rows, level.palette) }
    val starPace = LevelCatalog.starsStillAvailable(level, moves)
    val nextLevel = LevelCatalog.levels.firstOrNull { it.id == level.id + 1 }
    val hasActiveRun = moves > 0 && result == null

    fun resetLevel() {
        rows = GameEngine.initialRows(level)
        moves = 0
        history = emptyList()
        result = null
        resultPreviousBest = null
        resultImprovesBest = false
        hintCode = null
        hintRequested = false
        hintGain = 0
        lastMoveGain = 0
        onClearActiveAttempt()
    }

    fun saveAttempt(nextRows: List<String>, nextMoves: Int, nextHistory: List<String>) {
        if (nextMoves <= 0) {
            onClearActiveAttempt()
        } else {
            onSaveActiveAttempt(
                ActiveAttempt(
                    levelId = level.id,
                    rows = nextRows,
                    moves = nextMoves,
                    history = nextHistory,
                ),
            )
        }
    }

    fun requestBack() {
        if (hasActiveRun) {
            showLeaveConfirm = true
        } else {
            onClearActiveAttempt()
            onBack()
        }
    }

    fun requestRestart() {
        if (hasActiveRun) {
            showRestartConfirm = true
        } else {
            resetLevel()
        }
    }

    fun undoMove() {
        val last = history.lastOrNull() ?: return
        val nextRows = BoardSnapshotCodec.decodeRows(last)
        val nextHistory = history.dropLast(1)
        val nextMoves = (moves - 1).coerceAtLeast(0)
        rows = nextRows
        history = nextHistory
        moves = nextMoves
        hintCode = null
        hintRequested = false
        hintGain = 0
        lastMoveGain = 0
        saveAttempt(nextRows, nextMoves, nextHistory)
    }

    fun requestHint() {
        val suggestion = GameEngine.suggestMove(rows, level.palette)
        hintCode = suggestion?.tile?.code?.toString()
        hintGain = suggestion?.gain ?: 0
        hintRequested = true
    }

    fun choose(tile: QuarterTile) {
        if (result != null) return
        val previousCapturedCells = GameEngine.capturedCells(rows)
        val outcome = GameEngine.applyColor(rows, tile)
        if (!outcome.consumedMove) return
        hintCode = null
        hintRequested = false
        hintGain = 0
        lastMoveGain = (outcome.capturedCells - previousCapturedCells).coerceAtLeast(0)
        if (settings.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        val nextHistory = history + BoardSnapshotCodec.encodeRows(rows)
        val nextMoves = moves + 1
        history = nextHistory
        rows = outcome.rows
        moves = nextMoves
        when {
            GameEngine.isSolved(rows) -> {
                resultPreviousBest = progress.bestMoves(level.id)
                resultImprovesBest = progress.improvesBest(level.id, nextMoves)
                result = LevelRunResult.Win
                onClearActiveAttempt()
                onComplete(level.id, nextMoves)
            }

            nextMoves >= level.moveLimit -> {
                resultPreviousBest = null
                resultImprovesBest = false
                result = LevelRunResult.Loss
                onClearActiveAttempt()
            }

            else -> {
                saveAttempt(outcome.rows, nextMoves, nextHistory)
            }
        }
    }

    BackHandler(onBack = ::requestBack)

    if (showLeaveConfirm) {
        ConfirmAttemptDialog(
            title = stringResource(R.string.leave_level_confirm_title),
            body = stringResource(R.string.leave_level_confirm_body),
            confirmText = stringResource(R.string.leave_level_confirm_action),
            onDismiss = { showLeaveConfirm = false },
            onConfirm = {
                showLeaveConfirm = false
                onClearActiveAttempt()
                onBack()
            },
        )
    }

    if (showRestartConfirm) {
        ConfirmAttemptDialog(
            title = stringResource(R.string.restart_level_confirm_title),
            body = stringResource(R.string.restart_level_confirm_body),
            confirmText = stringResource(R.string.restart_level_confirm_action),
            onDismiss = { showRestartConfirm = false },
            onConfirm = {
                showRestartConfirm = false
                resetLevel()
            },
        )
    }

    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTextButton(
                    iconRes = R.drawable.ic_nav_back,
                    description = stringResource(R.string.back),
                    onClick = ::requestBack,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(level.title, style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(level.district, style = MaterialTheme.typography.bodyMedium, color = MutedInk)
                }
                Spacer(Modifier.size(44.dp))
            }

            Spacer(Modifier.height(12.dp))
            LevelIllustration(settings = settings)
            Spacer(Modifier.height(12.dp))
            LevelActionBar(
                undoEnabled = history.isNotEmpty() && result == null,
                hintEnabled = result == null,
                restartEnabled = true,
                settings = settings,
                onUndo = ::undoMove,
                onHint = ::requestHint,
                onRestart = ::requestRestart,
            )
            Spacer(Modifier.height(14.dp))
            StatRow(
                moves = moves,
                limit = level.moveLimit,
                remaining = remainingMoves,
                threeStarTarget = LevelCatalog.threeStarMoveLimit(level),
                captured = captured,
                capturedPercent = capturedPercent,
                lastMoveGain = lastMoveGain,
                starPace = starPace,
                showStarPace = result == null,
                best = progress.bestMoves(level.id),
            )
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(
                visible = result == null && hintRequested,
                enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { -it / 3 },
                exit = fadeOut(tween(120)) + slideOutVertically(tween(120)) { -it / 3 },
            ) {
                HintPanel(tile = hintTile, gain = hintGain)
            }
            if (result == null && hintRequested) {
                Spacer(Modifier.height(12.dp))
            }
            BoardView(
                rows = rows,
                settings = settings,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(18.dp))
            AnimatedVisibility(
                visible = result == null,
                enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 4 },
                exit = fadeOut(tween(120)) + slideOutVertically(tween(120)) { it / 4 },
            ) {
                Palette(
                    palette = level.palette,
                    selected = currentTile,
                    expansionGains = paletteGains,
                    enabled = true,
                    settings = settings,
                    onSelect = ::choose,
                )
            }
            AnimatedVisibility(
                visible = result != null,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f),
                exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.98f),
            ) {
                ResultPanel(
                    won = result == LevelRunResult.Win,
                    level = level,
                    moves = moves,
                    previousBest = resultPreviousBest,
                    improvesBest = resultImprovesBest,
                    capturedPercent = capturedPercent,
                    nextLevel = nextLevel,
                    onRetry = ::resetLevel,
                    onHome = onBack,
                    onNext = {
                        if (nextLevel != null) {
                            resetLevel()
                            onNext(nextLevel.id)
                        }
                    },
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HintPanel(tile: QuarterTile?, gain: Int) {
    Surface(
        color = Color(0xFFFFF8E8),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFC98200).copy(alpha = 0.36f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (tile != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tile.uiColor()),
                )
            }
            Text(
                text = if (tile != null && gain > 0) {
                    pluralStringResource(R.plurals.hint_gain_format, gain, tile.title, gain)
                } else {
                    stringResource(R.string.hint_unavailable)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
        }
    }
}

@Composable
private fun LevelIllustration(settings: GameSettings) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        shadowElevation = if (settings.reducedMotion) 1.dp else 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(R.drawable.level_illustration),
            contentDescription = stringResource(R.string.level_visual_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
private fun LevelActionBar(
    undoEnabled: Boolean,
    hintEnabled: Boolean,
    restartEnabled: Boolean,
    settings: GameSettings,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        shadowElevation = if (settings.reducedMotion) 1.dp else 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.action_panel_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(if (settings.reducedMotion) 0.66f else 0.82f),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LevelActionButton(
                    iconRes = R.drawable.ic_action_undo,
                    label = stringResource(R.string.undo_move_short),
                    description = stringResource(R.string.undo_move),
                    enabled = undoEnabled,
                    accent = Ink,
                    settings = settings,
                    onClick = onUndo,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                LevelActionButton(
                    iconRes = R.drawable.ic_action_hint,
                    label = stringResource(R.string.hint_short),
                    description = stringResource(R.string.hint),
                    enabled = hintEnabled,
                    accent = Color(0xFFC98200),
                    settings = settings,
                    onClick = onHint,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                LevelActionButton(
                    iconRes = R.drawable.ic_action_restart,
                    label = stringResource(R.string.restart_level_short),
                    description = stringResource(R.string.restart_level),
                    enabled = restartEnabled,
                    accent = Warning,
                    settings = settings,
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun LevelActionButton(
    iconRes: Int,
    label: String,
    description: String,
    enabled: Boolean,
    accent: Color,
    settings: GameSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && pressed && !settings.reducedMotion) 0.97f else 1f,
        animationSpec = if (settings.reducedMotion) snap() else tween(120),
        label = "level-action-press-scale",
    )
    val contentColor = if (enabled) accent else MutedInk
    Surface(
        color = if (enabled) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.34f) else Line),
        shadowElevation = if (enabled && !pressed) 1.dp else 0.dp,
        modifier = modifier
            .scale(buttonScale)
            .defaultMinSize(minHeight = 52.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                contentDescription = description
                if (!enabled) {
                    disabled()
                }
            },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (enabled) accent.copy(alpha = 0.12f) else Line.copy(alpha = 0.34f)),
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Ink else MutedInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ConfirmAttemptDialog(
    title: String,
    body: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = Warning)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun StatRow(
    moves: Int,
    limit: Int,
    remaining: Int,
    threeStarTarget: Int,
    captured: Float,
    capturedPercent: Int,
    lastMoveGain: Int,
    starPace: Int,
    showStarPace: Boolean,
    best: Int?,
) {
    val remainingColor = if (remaining <= 2) Warning else Ink
    val starPaceColor = if (starPace <= 1) Warning else Color(0xFFC98200)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatPill(stringResource(R.string.moves_count_compact_format, moves), Modifier.weight(1f))
        StatPill(
            text = stringResource(R.string.remaining_moves_compact_format, remaining),
            modifier = Modifier.weight(1f),
            contentColor = remainingColor,
            borderColor = if (remaining <= 2) Warning.copy(alpha = 0.42f) else Line,
        )
        StatPill(stringResource(R.string.move_limit_compact_format, limit), Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.level_goal_format, limit),
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.captured), style = MaterialTheme.typography.bodyMedium, color = MutedInk)
                Text(
                    stringResource(R.string.captured_percent_format, capturedPercent),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { captured },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.three_star_target_format, threeStarTarget),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC98200),
            )
            AnimatedVisibility(visible = showStarPace && starPace > 0) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.star_pace_format, "★".repeat(starPace)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = starPaceColor,
                    )
                }
            }
            AnimatedVisibility(visible = lastMoveGain > 0) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pluralStringResource(
                            R.plurals.last_move_gain_format,
                            lastMoveGain,
                            lastMoveGain,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Success,
                    )
                }
            }
            if (best != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.best_result_format, best),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color = Ink,
    borderColor: Color = Line,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BoardView(rows: List<String>, settings: GameSettings, modifier: Modifier = Modifier) {
    val boardDescription = stringResource(R.string.board_size_description, rows.size, rows.first().length)
    val capturedMask = remember(rows) { GameEngine.capturedMask(rows) }
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        shadowElevation = 2.dp,
        modifier = modifier.semantics { contentDescription = boardDescription },
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .aspectRatio(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    row.forEachIndexed { columnIndex, code ->
                        val tile = QuarterTile.fromCode(code)
                        val isCaptured = capturedMask.getOrNull(rowIndex)?.getOrNull(columnIndex) == true
                        val color by animateColorAsState(
                            targetValue = tile.uiColor(settings.highContrast),
                            animationSpec = if (settings.reducedMotion) snap() else tween(180),
                            label = "tile-color",
                        )
                        val cellScale by animateFloatAsState(
                            targetValue = if (!settings.reducedMotion && isCaptured) 1.015f else 1f,
                            animationSpec = if (settings.reducedMotion) snap() else tween(180),
                            label = "tile-captured-scale",
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .scale(cellScale)
                                .clip(RoundedCornerShape(7.dp))
                                .background(color)
                                .then(
                                    if (isCaptured) {
                                        Modifier.border(
                                            width = if (settings.highContrast) 2.dp else 1.5.dp,
                                            color = if (settings.highContrast) Ink else Color.White.copy(alpha = 0.78f),
                                            shape = RoundedCornerShape(7.dp),
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .then(
                                    if (settings.highContrast) {
                                        Modifier.border(1.dp, Ink.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            if (settings.highContrast) {
                                Text(
                                    text = tile.marker,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tile.uiMarkerColor(),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Palette(
    palette: List<QuarterTile>,
    selected: QuarterTile,
    expansionGains: Map<QuarterTile, Int>,
    enabled: Boolean,
    settings: GameSettings,
    onSelect: (QuarterTile) -> Unit,
) {
    Column {
        Text(stringResource(R.string.palette_description), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            palette.forEach { tile ->
                val isSelected = tile == selected
                val gain = expansionGains[tile] ?: 0
                val canSelect = enabled && !isSelected && gain > 0
                val tileScale by animateFloatAsState(
                    targetValue = when {
                        settings.reducedMotion -> 1f
                        isSelected -> 1.04f
                        canSelect -> 1f
                        else -> 0.96f
                    },
                    animationSpec = if (settings.reducedMotion) snap() else tween(160),
                    label = "palette-tile-scale",
                )
                val colorDescription = when {
                    settings.highContrast && isSelected -> {
                        stringResource(R.string.accessibility_current_color_button_with_marker, tile.marker, tile.title)
                    }

                    settings.highContrast && gain > 0 -> {
                        stringResource(R.string.accessibility_color_button_with_marker_gain, tile.marker, tile.title, gain)
                    }

                    settings.highContrast -> {
                        stringResource(R.string.accessibility_color_button_with_marker_no_gain, tile.marker, tile.title)
                    }

                    isSelected -> {
                        stringResource(R.string.accessibility_current_color_button, tile.title)
                    }

                    gain > 0 -> {
                        stringResource(R.string.accessibility_color_button_gain, tile.title, gain)
                    }

                    else -> {
                        stringResource(R.string.accessibility_color_button_no_gain, tile.title)
                    }
                }
                Surface(
                    color = tile.uiColor(settings.highContrast),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Ink else Color.White.copy(alpha = 0.9f),
                    ),
                    shadowElevation = if (canSelect) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .scale(tileScale)
                        .alpha(if (canSelect) 1f else 0.52f)
                        .clickable(enabled = canSelect, role = Role.Button) {
                            onSelect(tile)
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = colorDescription
                            if (!canSelect) {
                                disabled()
                            }
                        },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when {
                                settings.highContrast && isSelected -> Text(
                                    text = "✓ ${tile.marker}",
                                    color = tile.uiMarkerColor(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )

                                settings.highContrast -> Text(
                                    text = tile.marker,
                                    color = tile.uiMarkerColor(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )

                                isSelected -> Text("✓", color = Ink, style = MaterialTheme.typography.titleLarge)
                            }
                            if (gain > 0 && !isSelected) {
                                Text(
                                    text = stringResource(R.string.palette_gain_format, gain),
                                    color = tile.uiMarkerColor(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultPanel(
    won: Boolean,
    level: GameLevel,
    moves: Int,
    previousBest: Int?,
    improvesBest: Boolean,
    capturedPercent: Int,
    nextLevel: GameLevel?,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(
        color = if (won) Color(0xFFE9F6EF) else Color(0xFFFFEFE8),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (won) Success.copy(alpha = 0.4f) else Warning.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ResultIllustration(won = won)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(if (won) R.string.victory_title else R.string.defeat_title),
                style = MaterialTheme.typography.titleLarge,
                color = if (won) Success else Warning,
            )
            Text(
                text = stringResource(if (won) R.string.victory_body else R.string.defeat_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                textAlign = TextAlign.Center,
            )
            if (won) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "★".repeat(LevelCatalog.starsFor(level, moves)),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFC98200),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when {
                        previousBest == null -> stringResource(R.string.result_first_clear_format, moves)
                        improvesBest -> stringResource(R.string.result_new_best_format, moves, previousBest)
                        else -> stringResource(R.string.result_best_kept_format, previousBest)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (improvesBest || previousBest == null) Success else MutedInk,
                    textAlign = TextAlign.Center,
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.result_loss_progress_format, capturedPercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Warning,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.try_again))
                }
                if (won && nextLevel != null) {
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.next_level))
                    }
                } else {
                    Button(
                        onClick = onHome,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.home))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultIllustration(won: Boolean) {
    Image(
        painter = painterResource(if (won) R.drawable.victory_illustration else R.drawable.defeat_illustration),
        contentDescription = stringResource(
            if (won) R.string.victory_visual_description else R.string.defeat_visual_description,
        ),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun SettingsScreen(
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    onBack: () -> Unit,
    onOpenAboutPrivacy: () -> Unit,
    onResetProgress: () -> Unit,
) {
    var resetNotice by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(stringResource(R.string.reset_progress_confirm_title))
            },
            text = {
                Text(stringResource(R.string.reset_progress_confirm_body))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                        resetNotice = true
                    },
                ) {
                    Text(
                        text = stringResource(R.string.reset_progress_confirm_action),
                        color = Warning,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTextButton(R.drawable.ic_nav_back, stringResource(R.string.back), onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(20.dp))
            SettingRow(
                title = stringResource(R.string.haptics),
                body = stringResource(R.string.haptics_desc),
                checked = settings.hapticsEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(hapticsEnabled = it)) },
            )
            SettingRow(
                title = stringResource(R.string.reduced_motion),
                body = stringResource(R.string.reduced_motion_desc),
                checked = settings.reducedMotion,
                onCheckedChange = { onSettingsChange(settings.copy(reducedMotion = it)) },
            )
            SettingRow(
                title = stringResource(R.string.high_contrast),
                body = stringResource(R.string.high_contrast_desc),
                checked = settings.highContrast,
                onCheckedChange = { onSettingsChange(settings.copy(highContrast = it)) },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line)
            OutlinedButton(
                onClick = onOpenAboutPrivacy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.about_privacy))
                    Text(
                        text = stringResource(R.string.about_privacy_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Warning),
            ) {
                Text(stringResource(R.string.reset_progress))
            }
            AnimatedVisibility(resetNotice) {
                Text(
                    text = stringResource(R.string.confirm_reset),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Success,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Line),
            ) {
                Text(
                    text = stringResource(R.string.privacy_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutPrivacyScreen(onBack: () -> Unit) {
    val displayVersion = remember { BuildConfig.VERSION_NAME.removeSuffix("-debug") }
    BackHandler(onBack = onBack)
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTextButton(R.drawable.ic_nav_back, stringResource(R.string.back), onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.privacy_screen_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = stringResource(R.string.version_label, displayVersion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.privacy_screen_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            PrivacyInfoBlock(
                title = stringResource(R.string.privacy_local_title),
                body = stringResource(R.string.privacy_local_body),
            )
            PrivacyInfoBlock(
                title = stringResource(R.string.privacy_no_collection_title),
                body = stringResource(R.string.privacy_no_collection_body),
            )
            PrivacyInfoBlock(
                title = stringResource(R.string.privacy_permissions_title),
                body = stringResource(R.string.privacy_permissions_body),
            )
            PrivacyInfoBlock(
                title = stringResource(R.string.privacy_delete_title),
                body = stringResource(R.string.privacy_delete_body),
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun PrivacyInfoBlock(title: String, body: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MutedInk)
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MutedInk)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = title },
        )
    }
}

@Composable
private fun IconTextButton(
    iconRes: Int,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        color = if (enabled) Color.White else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .size(44.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = description
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.empty_levels),
            modifier = Modifier.padding(16.dp),
            color = MutedInk,
        )
    }
}

@Composable
private fun ErrorScreen(onHome: () -> Unit) {
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.unknown_error),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            ElevatedButton(onClick = onHome, shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.home))
            }
        }
    }
}
