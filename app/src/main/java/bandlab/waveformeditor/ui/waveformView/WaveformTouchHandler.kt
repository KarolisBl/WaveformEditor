package bandlab.waveformeditor.ui.waveformView

import android.view.MotionEvent
import bandlab.waveformeditor.toPx
import kotlin.math.roundToInt

private const val TOUCH_AREA_DP = 15

class WaveformTouchHandler {

    private var isDraggingStartSelection = false
    private var isDraggingEndSelection = false

    fun onTouchEvent(
        event: MotionEvent,
        currentStartSelection: Int,
        currentEndSelection: Int,
        maxEndSelection: Int,
        xStepWidth: Float,
    ): WaveformTouchEventResult {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(
                event,
                currentStartSelection,
                currentEndSelection,
                xStepWidth
            )

            MotionEvent.ACTION_UP -> onActionUp()
            MotionEvent.ACTION_MOVE -> return onActionMove(
                event,
                currentStartSelection,
                currentEndSelection,
                maxEndSelection,
                xStepWidth
            )
        }
        return WaveformTouchEventResult.SelectionNotChanged
    }

    private fun onActionDown(
        event: MotionEvent,
        currentStartSelection: Int,
        currentEndSelection: Int,
        xStepWidth: Float,
    ) {
        if (isActionDownInTouchArea(event.x, currentStartSelection, xStepWidth)) {
            isDraggingStartSelection = true
        }
        if (isActionDownInTouchArea(event.x, currentEndSelection, xStepWidth)) {
            isDraggingEndSelection = true
        }
    }

    private fun isActionDownInTouchArea(
        eventX: Float,
        currentSelection: Int,
        xStepWidth: Float
    ) = eventX < currentSelection * xStepWidth + TOUCH_AREA_DP.toPx &&
            eventX > currentSelection * xStepWidth - TOUCH_AREA_DP.toPx

    private fun onActionUp() {
        isDraggingStartSelection = false
        isDraggingEndSelection = false
    }

    private fun onActionMove(
        event: MotionEvent,
        currentStartSelection: Int,
        currentEndSelection: Int,
        maxEndSelection: Int,
        xStepWidth: Float,
    ): WaveformTouchEventResult {
        val pendingSelection = (event.x / xStepWidth).roundToInt()
        if (isDraggingStartSelection) {
            if (pendingSelection in 0 until currentEndSelection) {
                if (currentStartSelection != pendingSelection) {
                    return WaveformTouchEventResult.StartSelectionChanged(pendingSelection)
                }
            }
        } else if (isDraggingEndSelection) {
            if (pendingSelection in (currentStartSelection + 1) until maxEndSelection) {
                if (currentEndSelection != pendingSelection) {
                    return WaveformTouchEventResult.EndSelectionChanged(pendingSelection)
                }
            }
        }
        return WaveformTouchEventResult.SelectionNotChanged
    }
}

