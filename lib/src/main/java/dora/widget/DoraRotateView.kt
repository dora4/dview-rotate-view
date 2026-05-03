package dora.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import dora.widget.rotateview.R

class DoraRotateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var density = resources.displayMetrics.density
    private var shadowRadius = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val middleRect = RectF()
    private val innerRect = RectF()
    private val albumPathRect = RectF()
    private val albumTextPath = Path()

    // -------------------------
    // Text
    // -------------------------
    private var appName = "APP_NAME"
    private var albumText = "ALBUM_TEXT"
    private var appSlogan = "APP_SLOGAN"
    private var copyRight = "COPY_RIGHT"

    private var outerTextSize = 4.5f * density
    private var innerTextSize = 4f * density

    // -------------------------
    // Colors（全部可适配暗色）
    // -------------------------
    @ColorInt private var textColor = getColor(R.color.dview_text_color)
    @ColorInt private var middleColor = getColor(R.color.dview_middle_color)
    @ColorInt private var innerColor = getColor(R.color.dview_inner_color)

    // -------------------------
    // Animation
    // -------------------------
    private val rotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f).apply {
        duration = 10000
        interpolator = LinearInterpolator()
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
    }
    private var lastAnimationValue: Long = 0

    init {
        initAttrs(context, attrs)
        initShadowBackground()
    }

    // -------------------------
    // 初始化属性
    // -------------------------
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DoraRotateView)

        appName = ta.getString(R.styleable.DoraRotateView_dview_rv_appName) ?: appName
        albumText = ta.getString(R.styleable.DoraRotateView_dview_rv_albumText) ?: albumText
        appSlogan = ta.getString(R.styleable.DoraRotateView_dview_rv_appSlogan) ?: appSlogan
        copyRight = ta.getString(R.styleable.DoraRotateView_dview_rv_copyRight) ?: copyRight

        textColor = ta.getColor(
            R.styleable.DoraRotateView_dview_rv_textColor,
            textColor
        )

        outerTextSize = ta.getDimension(
            R.styleable.DoraRotateView_dview_rv_outerTextSize,
            outerTextSize
        )

        innerTextSize = ta.getDimension(
            R.styleable.DoraRotateView_dview_rv_innerTextSize,
            innerTextSize
        )

        ta.recycle()
    }

    // -------------------------
    // 阴影背景
    // -------------------------
    private fun initShadowBackground() {
        val circle: ShapeDrawable

        if (Build.VERSION.SDK_INT >= 21) {
            circle = ShapeDrawable(OvalShape())
            ViewCompat.setElevation(this, 16 * density)
        } else {
            val oval = OvalShadow((24 * density).toInt())
            circle = ShapeDrawable(oval)
            ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, circle.paint)
            circle.paint.setShadowLayer(
                shadowRadius.toFloat(),
                0f,
                1.75f * density,
                0x1E000000
            )
        }

        circle.paint.color = textColor
        background = circle
    }

    // -------------------------
    // 绘制
    // -------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // middle
        paint.color = middleColor
        canvas.drawOval(middleRect, paint)

        // inner
        paint.color = innerColor
        canvas.drawOval(innerRect, paint)

        // outer text
        paint.color = textColor
        paint.textSize = outerTextSize
        paint.textAlign = Paint.Align.CENTER
        canvas.drawTextOnPath(albumText, albumTextPath, 2 * density, 2 * density, paint)

        // center text
        paint.textSize = innerTextSize
        canvas.drawText(appName, width / 2f, height / 2f, paint)
        canvas.drawText(appSlogan, width / 2f, height / 2f + 4 * density, paint)
        canvas.drawText(copyRight, width / 2f, height / 2f + 12 * density, paint)
    }

    // -------------------------
    // 尺寸变化
    // -------------------------
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val middleSize = 80 * density
        val innerSize = 64 * density
        val pathSize = 56 * density

        middleRect.set(0f, 0f, middleSize, middleSize)
        innerRect.set(0f, 0f, innerSize, innerSize)
        albumPathRect.set(0f, 0f, pathSize, pathSize)

        middleRect.offset(w / 2 - middleSize / 2, h / 2 - middleSize / 2)
        innerRect.offset(w / 2 - innerSize / 2, h / 2 - innerSize / 2)
        albumPathRect.offset(w / 2 - pathSize / 2, h / 2 - pathSize / 2)

        albumTextPath.reset()
        albumTextPath.addOval(albumPathRect, Path.Direction.CW)
    }

    // -------------------------
    // 动画控制
    // -------------------------
    fun startRotateAnimation() {
        rotateAnimator.start()
    }

    fun pauseRotateAnimation() {
        lastAnimationValue = rotateAnimator.currentPlayTime
        rotateAnimator.cancel()
    }

    fun resumeRotateAnimation() {
        rotateAnimator.start()
        rotateAnimator.currentPlayTime = lastAnimationValue
    }

    fun cancelRotateAnimation() {
        lastAnimationValue = 0
        rotateAnimator.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rotateAnimator.cancel()
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        invalidate()
    }

    fun setMiddleColor(@ColorInt color: Int) {
        middleColor = color
        invalidate()
    }

    fun setInnerColor(@ColorInt color: Int) {
        innerColor = color
        invalidate()
    }

    fun setAppName(name: String) {
        appName = name
        invalidate()
    }

    fun setAlbumText(text: String) {
        albumText = text
        albumTextPath.reset()
        albumTextPath.addOval(albumPathRect, Path.Direction.CW)
        invalidate()
    }

    fun setAppSlogan(slogan: String) {
        appSlogan = slogan
        invalidate()
    }

    fun setCopyRight(text: String) {
        copyRight = text
        invalidate()
    }

    private fun getColor(resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    // -------------------------
    // 阴影类
    // -------------------------
    private inner class OvalShadow(shadowRadius: Int) : OvalShape() {
        private val shadowPaint = Paint()

        init {
            this@DoraRotateView.shadowRadius = shadowRadius
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            val cx = width / 2f
            val cy = height / 2f

            shadowPaint.shader = RadialGradient(
                cx, cy,
                shadowRadius.toFloat(),
                intArrayOf(0x3D000000, Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )

            canvas.drawCircle(cx, cy, cx, shadowPaint)
            canvas.drawCircle(cx, cy, cx - shadowRadius, paint)
        }
    }
}