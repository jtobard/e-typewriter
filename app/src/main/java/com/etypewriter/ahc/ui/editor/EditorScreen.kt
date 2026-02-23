package com.etypewriter.ahc.ui.editor

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.etypewriter.ahc.ui.theme.SheetDeskGray
import com.etypewriter.ahc.util.SoundManager

private const val FIXED_LINE_INDEX = 2
/** Una pulgada en dp (Android: 160 dp ≈ 1 inch). */
private val ONE_INCH_DP = 96.dp
private const val BELL_TRIGGER_CHARS = 63
/** Page height in lines (A4-like). */
private const val LINES_PER_PAGE = 70

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onSaveFile: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val lifecycleOwner = LocalLifecycleOwner.current

    val soundManager = viewModel.soundManager

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                soundManager.startHum()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                soundManager.pauseHum()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Consume Back so that double-space/double-Enter (which can send Back on some devices) doesn't leave the editor.
    BackHandler { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopBar(
            fileName = viewModel.fileName,
            hasUnsavedChanges = viewModel.hasUnsavedChanges,
            onNew = onNewFile,
            onOpen = onOpenFile,
            onSave = onSaveFile,
            typography = typography.titleMedium,
        )

        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

        TypewriterEditor(
            viewModel = viewModel,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            textStyle = typography.bodyLarge.copy(color = colors.onBackground),
            placeholderStyle = typography.bodyLarge.copy(
                color = colors.outline.copy(alpha = 0.5f)
            ),
            cursorColor = colors.onBackground,
            soundManager = soundManager
        )

        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

        StatusBar(
            text = viewModel.text,
            typography = typography.labelSmall,
        )
    }
}

@Composable
private fun TypewriterEditor(
    viewModel: EditorViewModel,
    modifier: Modifier,
    textStyle: TextStyle,
    placeholderStyle: TextStyle,
    cursorColor: androidx.compose.ui.graphics.Color,
    soundManager: SoundManager,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val estimatedLineHeightPx = with(density) { textStyle.lineHeight.toPx() }
    val oneInchPx = with(density) { ONE_INCH_DP.toPx() }

    val width70CharsPx = remember(textStyle) {
        textMeasurer.measure(
            text = "M".repeat(EditorViewModel.CHARS_PER_LINE),
            style = textStyle,
        ).size.width
    }
    val sheetWidthPx = 2 * oneInchPx + width70CharsPx

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val cursorOffset = viewModel.textFieldValue.selection.start
    val layout = textLayoutResult
    val contentHeightPx = layout?.size?.height?.toFloat() ?: estimatedLineHeightPx

    val targetTranslationY: Float
    val targetTranslationX: Float
    val screenCenterY = boxSize.height / 2f

    if (layout != null && layout.lineCount > 0) {
        val cursorLine = layout.getLineForOffset(
            cursorOffset.coerceAtMost(layout.layoutInput.text.length)
        )
        val cursorRect = layout.getCursorRect(
            cursorOffset.coerceAtMost(layout.layoutInput.text.length)
        )

        val fieldVisibleHeight = (boxSize.height.toFloat() - oneInchPx).coerceAtLeast(0f)
        val internalScrollPx = (layout.getLineBottom(cursorLine) - fieldVisibleHeight).coerceAtLeast(0f)

        targetTranslationX = boxSize.width / 2f - oneInchPx - cursorRect.left
        targetTranslationY = screenCenterY - oneInchPx - layout.getLineTop(cursorLine) + internalScrollPx
    } else {
        targetTranslationX = boxSize.width / 2f - oneInchPx
        targetTranslationY = screenCenterY - oneInchPx
    }

    val animatedTranslationY by animateFloatAsState(
        targetValue = targetTranslationY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "typewriter-y",
    )
    val animatedTranslationX by animateFloatAsState(
        targetValue = targetTranslationX,
        animationSpec = snap(),
        label = "typewriter-x",
    )

    val pageHeightPx = oneInchPx + LINES_PER_PAGE * estimatedLineHeightPx + oneInchPx
    val sheetHeightPx = (contentHeightPx + oneInchPx + boxSize.height)
        .coerceAtLeast(pageHeightPx)
        .coerceAtLeast(boxSize.height.toFloat())

    Box(
        modifier = modifier
            .clipToBounds()
            .background(SheetDeskGray)
            .onSizeChanged { boxSize = it }
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(with(density) { sheetWidthPx.toDp() })
                .height(with(density) { sheetHeightPx.toDp() })
                .drawBehind {
                    drawRect(
                        color = Color.White,
                        size = Size(size.width, sheetHeightPx),
                    )
                }
                .graphicsLayer {
                    translationX = animatedTranslationX
                    translationY = animatedTranslationY
                }
        ) {
            if (viewModel.text.isEmpty()) {
                Text(
                    text = "Start typing...",
                    style = placeholderStyle,
                    modifier = Modifier.padding(
                        start = ONE_INCH_DP,
                        top = ONE_INCH_DP,
                    )
                )
            }
            BasicTextField(
                value = viewModel.textFieldValue,
                onValueChange = { newValue ->
                    val oldText = viewModel.textFieldValue.text
                    if (newValue.text != oldText && newValue.text.length > oldText.length) {
                        val newNewlines = newValue.text.count { it == '\n' }
                        val oldNewlines = oldText.count { it == '\n' }
                        if (newNewlines > oldNewlines) {
                            soundManager.playReturnSound()
                        } else {
                            soundManager.playKeySound()
                        }
                        soundManager.triggerHapticFeedback()

                        val cursor = newValue.selection.start
                        val textBeforeCursor = newValue.text.take(cursor)
                        val lastNewlineIndex = textBeforeCursor.lastIndexOf('\n')
                        val currentLineLength = cursor - (lastNewlineIndex + 1)
                        if (currentLineLength == BELL_TRIGGER_CHARS) {
                            soundManager.playBellSound()
                        }
                    }
                    viewModel.onTextFieldValueChange(newValue)
                },
                modifier = Modifier
                    .padding(
                        start = ONE_INCH_DP,
                        end = ONE_INCH_DP,
                        top = ONE_INCH_DP,
                    )
                    .fillMaxWidth()
                    .fillMaxHeight(),
                textStyle = textStyle,
                cursorBrush = SolidColor(cursorColor),
                onTextLayout = { textLayoutResult = it },
            )
        }
    }
}

@Composable
private fun TopBar(
    fileName: String,
    hasUnsavedChanges: Boolean,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    typography: TextStyle,
) {
    val colors = MaterialTheme.colorScheme
    val displayName = if (hasUnsavedChanges) "$fileName *" else fileName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = displayName,
            style = typography,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )

        IconButton(onClick = onNew) {
            Icon(
                imageVector = Icons.Outlined.CreateNewFolder,
                contentDescription = "New file",
                tint = colors.onBackground,
            )
        }
        IconButton(onClick = onOpen) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = "Open file",
                tint = colors.onBackground,
            )
        }
        IconButton(onClick = onSave) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = "Save file",
                tint = colors.onBackground,
            )
        }
    }
}

@Composable
private fun StatusBar(
    text: String,
    typography: TextStyle,
) {
    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val charCount = text.length
    val lineCount = if (text.isEmpty()) 1 else text.lines().size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "$lineCount lines", style = typography)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$wordCount words", style = typography)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$charCount chars", style = typography)
    }
}
