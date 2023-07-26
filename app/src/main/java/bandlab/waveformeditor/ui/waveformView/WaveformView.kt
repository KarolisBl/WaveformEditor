package bandlab.waveformeditor.ui.waveformView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import bandlab.waveformeditor.R
import bandlab.waveformeditor.toPx

private const val SELECTION_ARC_SWEEP_ANGLE = 180f
private const val SELECTION_ARC_RADIUS_DP = 20

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waveformData: List<Pair<Float, Float>> = emptyList()
    private var currentStartSelection = 0
    private var currentEndSelection = 0
    private var waveformLinesFillPath = Path()
    private val selectionArcRadius = SELECTION_ARC_RADIUS_DP.toPx
    private val waveformTouchHandler = WaveformTouchHandler()
    private var onSelectionChanged: ((startSelection: Int, endSelection: Int) -> Unit)? = null

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_background)
    }

    private val waveformFillPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
    }

    private val selectionBackgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_selection_background)
    }

    private val selectionLinePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_selection_line)
        strokeWidth = 2.toPx
    }

    private val selectionArcPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_selection_line)
    }

    fun updateData(
        waveformData: List<Pair<Float, Float>>,
        currentStartSelection: Int,
        currentEndSelection: Int
    ) {
        waveformLinesFillPath = Path()
        this.waveformData = waveformData
        this.currentStartSelection = currentStartSelection
        this.currentEndSelection = currentEndSelection
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            drawBackground()
            addUpperLinesToFillPath()
            addLowerLinesToFillPath()
            drawPath(waveformLinesFillPath, waveformFillPaint)
            drawStartSelection()
            drawEndSelection()
        }
    }

    private fun Canvas.drawBackground() {
        drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    private fun addUpperLinesToFillPath() {
        addLinesToFillPath { it.second }
    }

    private fun addLowerLinesToFillPath() {
        addLinesToFillPath { it.first }
    }

    private fun addLinesToFillPath(getRelevantCoordinate: (Pair<Float, Float>) -> Float) {
        waveformLinesFillPath.moveTo(0f, height / 2f)
        waveformData.forEachIndexed { index, pair ->
            waveformLinesFillPath.lineTo(
                getXStepWidth() * index.toFloat(),
                getYStepHeight(getRelevantCoordinate(pair))
            )
        }
        waveformLinesFillPath.lineTo(width.toFloat(), height / 2f)
    }

    private fun Canvas.drawStartSelection() {
        drawRect(
            0f,
            0f,
            currentStartSelection * getXStepWidth(),
            height.toFloat(),
            selectionBackgroundPaint
        )

        val lineX = currentStartSelection * getXStepWidth() + 1.toPx
        drawLine(
            lineX,
            0f,
            lineX,
            height.toFloat(),
            selectionLinePaint
        )

        drawArc(
            getSelectionArcLeftCoordinates(currentStartSelection),
            0f,
            getSelectionArcRightCoordinates(currentStartSelection),
            selectionArcRadius,
            270f,
            SELECTION_ARC_SWEEP_ANGLE,
            true,
            selectionArcPaint
        )
    }

    private fun Canvas.drawEndSelection() {
        drawRect(
            currentEndSelection * getXStepWidth(),
            0f,
            width.toFloat(),
            height.toFloat(),
            selectionBackgroundPaint
        )

        val lineX = currentEndSelection * getXStepWidth() - 1.toPx
        drawLine(
            lineX,
            0f,
            lineX,
            height.toFloat(),
            selectionLinePaint
        )

        drawArc(
            getSelectionArcLeftCoordinates(currentEndSelection),
            height - selectionArcRadius,
            getSelectionArcRightCoordinates(currentEndSelection),
            height.toFloat(),
            90f,
            SELECTION_ARC_SWEEP_ANGLE,
            true,
            selectionArcPaint
        )
    }

    private fun getSelectionArcLeftCoordinates(selection: Int): Float {
        return selection * getXStepWidth() - selectionArcRadius / 1.5f
    }

    private fun getSelectionArcRightCoordinates(selection: Int): Float {
        return selection * getXStepWidth() + selectionArcRadius / 1.5f
    }

    private fun getXStepWidth(): Float {
        return width / (waveformData.size - 1).toFloat()
    }

    private fun getYStepHeight(value: Float): Float {
        return height / 2 + height / 2 * -value
    }

    fun setOnSelectionChangedListener(
        onSelectionChanged: (startSelection: Int, endSelection: Int) -> Unit
    ) {
        this.onSelectionChanged = onSelectionChanged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchEventResult = waveformTouchHandler.onTouchEvent(
            event = event,
            currentStartSelection = currentStartSelection,
            currentEndSelection = currentEndSelection,
            maxEndSelection = waveformData.size,
            xStepWidth = getXStepWidth(),
        )
        when (touchEventResult) {
            is WaveformTouchEventResult.StartSelectionChanged -> {
                currentStartSelection = touchEventResult.selection
                onSelectionChanged()
                invalidate()
            }

            is WaveformTouchEventResult.EndSelectionChanged -> {
                currentEndSelection = touchEventResult.selection
                onSelectionChanged()
                invalidate()
            }

            WaveformTouchEventResult.SelectionNotChanged -> { /* DO NOTHING */
            }
        }
        return super.onTouchEvent(event)
    }

    private fun onSelectionChanged() {
        onSelectionChanged?.let { it(currentStartSelection, currentEndSelection) }
    }
}