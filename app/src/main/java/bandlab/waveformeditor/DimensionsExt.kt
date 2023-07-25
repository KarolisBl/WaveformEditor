package bandlab.waveformeditor

import android.content.res.Resources
import android.util.TypedValue

val Int.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )