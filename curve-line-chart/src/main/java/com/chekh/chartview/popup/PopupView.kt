package com.chekh.chartview.popup

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.chekh.chartview.extensions.classLoader
import com.chekh.chartview.extensions.getFullClassName
import com.chekh.chartview.model.IntersectionPoint
import java.lang.reflect.InvocationTargetException

/**
 * This view is a representation of the popup window with intersection points
 */
abstract class PopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Binding of intersections
     * @param xPixel the coordinate of the abscissa intersection.
     * [xPixel] equals null if there are no intersections.
     * @param intersections list of intersections with curve lines
     */
    abstract fun bind(@Px xPixel: Float?, intersections: List<IntersectionPoint>)

    internal companion object {

        private val CONSTRUCTOR_SIGNATURE = arrayOf(
            Context::class.java,
            AttributeSet::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )

        /**
         * Instantiate a PopupView if specified in the attributes
         */
        @Suppress("ThrowsCount", "SpreadOperator")
        @Throws(IllegalStateException::class)
        operator fun invoke(
            parent: ViewGroup,
            className: String?,
            attrs: AttributeSet?,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int
        ): PopupView? {
            val fullClassName = className?.takeUnless { it.isBlank() }?.let {
                parent.context.getFullClassName(it)
            } ?: return null
            return try {
                val popupViewClass = Class
                    .forName(fullClassName, false, parent.classLoader)
                    .asSubclass(PopupView::class.java)

                val constructor = try {
                    popupViewClass.getConstructor(*CONSTRUCTOR_SIGNATURE)
                } catch (cause: NoSuchMethodException) {
                    try {
                        popupViewClass.getConstructor()
                    } catch (exception: NoSuchMethodException) {
                        exception.initCause(cause)
                        throw IllegalStateException(
                            "${attrs?.positionDescription}: Error creating PopupView: $fullClassName",
                            exception
                        )
                    }
                }.apply { isAccessible = true }
                constructor.newInstance(parent.context, attrs, defStyleAttr, defStyleRes)
            } catch (exception: ClassNotFoundException) {
                throw IllegalStateException(
                    "${attrs?.positionDescription}: Unable to find PopupView: $fullClassName",
                    exception
                )
            } catch (exception: InvocationTargetException) {
                throw IllegalStateException(
                    "${attrs?.positionDescription}: Could not instantiate the PopupView: $fullClassName",
                    exception
                )
            } catch (exception: InstantiationException) {
                throw IllegalStateException(
                    "${attrs?.positionDescription}: Could not instantiate the PopupView: $fullClassName",
                    exception
                )
            } catch (exception: IllegalAccessException) {
                throw IllegalStateException(
                    "${attrs?.positionDescription}: Cannot access non-public PopupView: $fullClassName",
                    exception
                )
            } catch (exception: ClassCastException) {
                throw IllegalStateException(
                    "${attrs?.positionDescription}: Class is not a PopupView: $fullClassName",
                    exception
                )
            }
        }
    }
}
