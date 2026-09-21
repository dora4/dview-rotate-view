package dora.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import dora.widget.rotateview.R

class DoraRotateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var shadowRadius = 0
    private var paint = Paint()
    private var middleRect = RectF()
    private var innerRect = RectF()
    private var albumPathRect = RectF()
    private var albumTextPath = Path()
    private var density = 0f

    // -------------------------
    // Attributes
    // -------------------------
    private var appName: String = "APP_NAME"
    private var albumText: String = "ALBUM_TEXT"
    private var appSlogan: String = "APP_SLOGAN"
    private var copyright: String = "COPYRIGHT"
    private var textColor: Int = DEFAULT_TEXT_COLOR
    private var outerTextColor: Int = DEFAULT_TEXT_COLOR
    private var outerTextSize: Int = sp2px(ALBUM_CIRCLE_TEXT_SIZE_SP)
    private var innerTextSize: Int = sp2px(ALBUM_CIRCLE_TEXT_SIZE_SMALL_SP)

    private var innerCircleRadius: Int = dp2px(DEFAULT_INNER_CIRCLE_RADIUS_DP)
    private var outerCircleRadius: Int = dp2px(DEFAULT_OUTER_CIRCLE_RADIUS_DP)
    private var albumTextCircleRadius: Int = dp2px(DEFAULT_ALBUM_TEXT_CIRCLE_RADIUS_DP)
    private var appNameTextYOffset: Int = dp2px(APP_NAME_TEXT_Y_OFFSET)
    private var appSloganTextYOffset: Int = dp2px(APP_SLOGAN_TEXT_Y_OFFSET)
    private var copyrightTextYOffset: Int = dp2px(COPYRIGHT_TEXT_Y_OFFSET)

    // Animation
    private lateinit var rotateAnimator: ObjectAnimator
    private var lastAnimationValue: Long = 0

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context,
                     attrs: AttributeSet? = null,
                     defStyleAttr: Int = 0) {
        initAttrs(context, attrs, defStyleAttr)
        density = context.resources.displayMetrics.density

        val shadowXOffset = (density * X_OFFSET).toInt()
        val shadowYOffset = (density * Y_OFFSET).toInt()
        shadowRadius = (density * SHADOW_RADIUS).toInt()

        val circle: ShapeDrawable
        if (elevationSupported()) {
            circle = ShapeDrawable(OvalShape())
            ViewCompat.setElevation(this, SHADOW_ELEVATION * density)
        } else {
            val oval: OvalShape = OvalShadow(shadowRadius)
            circle = ShapeDrawable(oval)
            ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, circle.paint)
            circle.paint.setShadowLayer(
                shadowRadius.toFloat(),
                shadowXOffset.toFloat(),
                shadowYOffset.toFloat(),
                KEY_SHADOW_COLOR
            )
            val padding = shadowRadius
            setPadding(padding, padding, padding, padding)
        }

        circle.paint.isAntiAlias = true
        circle.paint.color = textColor
        background = circle

        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        paint.color = textColor
        paint.textSize = ALBUM_CIRCLE_TEXT_SIZE_SP * density
        outerTextColor = textColor

        // Rotate animation
        rotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
        rotateAnimator.duration = 10000
        rotateAnimator.interpolator = LinearInterpolator()
        rotateAnimator.repeatMode = ValueAnimator.RESTART
        rotateAnimator.repeatCount = ValueAnimator.INFINITE
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) {
        context.withStyledAttributes(
            attrs, R.styleable.DoraRotateView, defStyleAttr, 0
        ) {
            appName = getString(R.styleable.DoraRotateView_dview_rv_appName) ?: appName
            albumText = getString(R.styleable.DoraRotateView_dview_rv_albumText) ?: albumText
            appSlogan = getString(R.styleable.DoraRotateView_dview_rv_appSlogan) ?: appSlogan
            copyright = getString(R.styleable.DoraRotateView_dview_rv_copyRight) ?: copyright
            textColor = getColor(R.styleable.DoraRotateView_dview_rv_textColor, textColor)

            outerTextSize = getDimension(
                R.styleable.DoraRotateView_dview_rv_outerTextSize,
                outerTextSize.toFloat()
            ).toInt()
            innerTextSize = getDimension(
                R.styleable.DoraRotateView_dview_rv_innerTextSize,
                innerTextSize.toFloat()
            ).toInt()
            innerCircleRadius = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_innerCircleRadius, innerCircleRadius)
            outerCircleRadius = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_outerCircleRadius, outerCircleRadius)
            albumTextCircleRadius = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_albumTextCircleRadius, albumTextCircleRadius)
            appNameTextYOffset = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_appNameTextYOffset, appNameTextYOffset)
            appSloganTextYOffset = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_sloganTextYOffset, appSloganTextYOffset)
            copyrightTextYOffset = getDimensionPixelOffset(R.styleable.DoraRotateView_dview_rv_copyRightTextYOffset, copyrightTextYOffset)
        }
    }

    private fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, context.resources.displayMetrics
        ).toInt()
    }

    private fun sp2px(spVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal, context.resources.displayMetrics
        ).toInt()
    }

    private fun px2sp(pxVal: Int): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return pxVal / scale
    }

    private fun elevationSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 21
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!elevationSupported()) {
            setMeasuredDimension(
                measuredWidth + shadowRadius * 2,
                measuredHeight + shadowRadius * 2
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = MIDDLE_RECT_COLOR
        canvas.drawOval(middleRect, paint)

        paint.color = INNER_RECT_COLOR
        canvas.drawOval(innerRect, paint)

        // Outer text (albumText)
        paint.textSize = px2sp(outerTextSize)
        paint.color = outerTextColor
        canvas.drawTextOnPath(albumText, albumTextPath, 2 * density, 2 * density, paint)

        // Center text
        paint.textSize = px2sp(innerTextSize)
        val centerY = height / 2f
        canvas.drawText(appName, width / 2f, centerY + appNameTextYOffset, paint)
        canvas.drawText(appSlogan, width / 2f, centerY + appSloganTextYOffset, paint)
        canvas.drawText(copyright, width / 2f, centerY + copyrightTextYOffset, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f

        val middleR = outerCircleRadius.toFloat()
        middleRect.set(cx - middleR, cy - middleR, cx + middleR, cy + middleR)

        val innerR = innerCircleRadius.toFloat()
        innerRect.set(cx - innerR, cy - innerR, cx + innerR, cy + innerR)

        val albumR = albumTextCircleRadius.toFloat()
        albumPathRect.set(cx - albumR, cy - albumR, cx + albumR, cy + albumR)

        albumTextPath.reset()
        albumTextPath.addOval(albumPathRect, Path.Direction.CW)
    }

    // -------------------------
    // Animation
    // -------------------------
    fun startRotateAnimation() {
        rotateAnimator.cancel()
        rotateAnimator.start()
    }

    fun cancelRotateAnimation() {
        lastAnimationValue = 0
        rotateAnimator.cancel()
    }

    fun pauseRotateAnimation() {
        lastAnimationValue = rotateAnimator.currentPlayTime
        rotateAnimator.cancel()
    }

    fun resumeRotateAnimation() {
        rotateAnimator.start()
        rotateAnimator.currentPlayTime = lastAnimationValue
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rotateAnimator.cancel()
    }

    // -------------------------
    // Support Code Setters
    // -------------------------
    fun setAppName(name: String) {
        appName = name
        invalidate()
    }

    fun setAlbumText(text: String) {
        albumText = text
        invalidate()
    }

    fun setAppSlogan(slogan: String) {
        appSlogan = slogan
        invalidate()
    }

    fun setCopyright(text: String) {
        copyright = text
        invalidate()
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        outerTextColor = color
        invalidate()
    }

    fun setOuterTextSize(textSize: Int) {
        outerTextSize = textSize
        invalidate()
    }

    fun setOuterTextSizeInSp(textSize: Float) {
        outerTextSize = sp2px(textSize)
        invalidate()
    }

    fun setInnerTextSize(textSize: Int) {
        innerTextSize = textSize
        invalidate()
    }

    fun setInnerTextSizeInSp(textSize: Float) {
        innerTextSize = sp2px(textSize)
        invalidate()
    }

    fun setInnerCircleRadius(radius: Int) {
        innerCircleRadius = radius
        invalidate()
    }

    fun setOuterCircleRadius(radius: Int) {
        outerCircleRadius = radius
        invalidate()
    }

    fun setAlbumTextCircleRadius(radius: Int) {
        albumTextCircleRadius = radius
        invalidate()
    }

    fun setAppNameTextYOffset(offset: Int) {
        appNameTextYOffset = offset
        invalidate()
    }

    fun setAppSloganTextYOffset(offset: Int) {
        appSloganTextYOffset = offset
        invalidate()
    }

    fun setCopyrightTextYOffset(offset: Int) {
        copyrightTextYOffset = offset
        invalidate()
    }

    fun setInnerCircleRadiusInDp(radius: Float) {
        innerCircleRadius = dp2px(radius)
        invalidate()
    }

    fun setOuterCircleRadiusInDp(radius: Float) {
        outerCircleRadius = dp2px(radius)
        invalidate()
    }

    fun setAlbumTextCircleRadiusInDp(radius: Float) {
        albumTextCircleRadius = dp2px(radius)
        invalidate()
    }

    fun setAppNameTextYOffsetInDp(offset: Float) {
        appNameTextYOffset = dp2px(offset)
        invalidate()
    }

    fun setAppSloganTextYOffsetInDp(offset: Float) {
        appSloganTextYOffset = dp2px(offset)
        invalidate()
    }

    fun setCopyrightTextYOffsetInDp(offset: Float) {
        copyrightTextYOffset = dp2px(offset)
        invalidate()
    }

    // -------------------------
    // Oval shadow for pre-L devices
    // -------------------------
    private inner class OvalShadow(shadowRadius: Int) : OvalShape() {

        private var radialGradient: RadialGradient? = null
        private val shadowPaint: Paint = Paint()

        init {
            this@DoraRotateView.shadowRadius = shadowRadius
            updateRadialGradient(rect().width().toInt())
        }

        override fun onResize(width: Float, height: Float) {
            super.onResize(width, height)
            updateRadialGradient(width.toInt())
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            val viewWidth = this@DoraRotateView.width
            val viewHeight = this@DoraRotateView.height
            canvas.drawCircle(
                viewWidth / 2f,
                viewHeight / 2f,
                viewWidth / 2f,
                shadowPaint
            )
            canvas.drawCircle(
                viewWidth / 2f,
                viewHeight / 2f,
                viewWidth / 2f - shadowRadius,
                paint
            )
        }

        private fun updateRadialGradient(diameter: Int) {
            radialGradient = RadialGradient(
                diameter / 2f, diameter / 2f,
                shadowRadius.toFloat(), intArrayOf(FILL_SHADOW_COLOR, Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            shadowPaint.shader = radialGradient
        }
    }

    companion object {
        private const val KEY_SHADOW_COLOR = 0x1E000000
        private const val FILL_SHADOW_COLOR = 0x3D000000
        private const val X_OFFSET = 0f
        private const val Y_OFFSET = 1.75f
        private const val SHADOW_RADIUS = 24f
        private const val SHADOW_ELEVATION = 16
        private const val DEFAULT_TEXT_COLOR = -0xc3a088
        private const val MIDDLE_RECT_COLOR = -0xb38e74
        private const val INNER_RECT_COLOR = 0x4FD8D8D8

        private const val APP_NAME_TEXT_Y_OFFSET = -4f

        private const val APP_SLOGAN_TEXT_Y_OFFSET = 4f

        private const val COPYRIGHT_TEXT_Y_OFFSET = 12f

        private const val ALBUM_CIRCLE_TEXT_COLOR = 0xFF634234.toInt()
        private const val ALBUM_CIRCLE_TEXT_SIZE_SP = 14f
        private const val ALBUM_CIRCLE_TEXT_SIZE_SMALL_SP = 12f

        private const val DEFAULT_INNER_CIRCLE_RADIUS_DP = 36f

        private const val DEFAULT_OUTER_CIRCLE_RADIUS_DP = 44f
        private const val DEFAULT_ALBUM_TEXT_CIRCLE_RADIUS_DP = 32f
    }
}
