package io.github.codingfulalt.cairn.screenshots

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.feature.widget.WidgetContent
import io.github.codingfulalt.cairn.feature.widget.WidgetHabit
import io.github.codingfulalt.cairn.feature.widget.WidgetState
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** Renders the home screen widget for the README, the same way a launcher would. */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w393dp-h852dp-xhdpi")
class WidgetScreenshotTest {
    private val state =
        WidgetState(
            progress = DayProgress(LocalDate.of(2026, 9, 10), scheduled = 5, completed = 2),
            habits =
                listOf(
                    WidgetHabit(1, "Drink water", HabitColor.Sky.swatch, count = 4, goal = 6),
                    WidgetHabit(2, "Morning run", HabitColor.Moss.swatch, count = 0, goal = 1),
                    WidgetHabit(3, "Write 3 lines", HabitColor.Sand.swatch, count = 0, goal = 1),
                    WidgetHabit(
                        4,
                        "Read 10 pages",
                        HabitColor.Lavender.swatch,
                        count = 1,
                        goal = 1,
                    ),
                    WidgetHabit(5, "Meditate", HabitColor.Mint.swatch, count = 1, goal = 1),
                ),
        )

    @Test
    fun widget() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val size = DpSize(300.dp, 360.dp)
            val result =
                GlanceRemoteViews().compose(context, size) {
                    WidgetContent(state, scrollable = false)
                }

            val density = context.resources.displayMetrics.density
            val width = (size.width.value * density).toInt()
            val height = (size.height.value * density).toInt()
            val margin = (20 * density).toInt()

            // a plain dark "home screen" behind the widget
            val screen =
                FrameLayout(
                    context,
                ).apply { setBackgroundColor(Color.parseColor("#2A2C30")) }
            val widget = result.remoteViews.apply(context, screen)
            screen.addView(
                widget,
                FrameLayout
                    .LayoutParams(
                        width,
                        height,
                    ).apply { setMargins(margin, margin, margin, margin) },
            )
            val fullWidth = width + margin * 2
            val fullHeight = height + margin * 2
            // lists fill in after the first layout, so lay out, let the main looper run, then again
            repeat(2) {
                screen.measure(
                    View.MeasureSpec.makeMeasureSpec(fullWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(fullHeight, View.MeasureSpec.EXACTLY),
                )
                screen.layout(0, 0, fullWidth, fullHeight)
                shadowOf(Looper.getMainLooper()).idle()
            }
            val bitmap = Bitmap.createBitmap(fullWidth, fullHeight, Bitmap.Config.ARGB_8888)
            screen.draw(Canvas(bitmap))
            bitmap.captureRoboImage("../docs/screenshots/widget.png")
        }
}
