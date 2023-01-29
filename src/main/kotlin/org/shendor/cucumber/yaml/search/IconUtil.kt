package org.shendor.cucumber.yaml.search

import com.intellij.ui.JBColor
import com.intellij.ui.LayeredIcon
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import icons.CucumberIcons
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants

object CommonIcons {
    val COUNT_BASE = CucumberIcons.Cucumber
}

fun addText(base: Icon, text: String, size: Float, position: Int, foreground: Color): Icon {
    val icon = LayeredIcon(2)
    icon.setIcon(base, 0)
    icon.setIcon(textToIcon(text, JLabel(), JBUIScale.scale(size), foreground), 1, position)
    return icon
}

fun textToIcon(text: String, component: Component, fontSize: Float, foreground: Color): Icon {
    val font: Font = JBFont.create(JBUI.Fonts.label().deriveFont(fontSize))
    val metrics = component.getFontMetrics(font)
    val width = metrics.stringWidth(text) + JBUI.scale(4)
    val height = metrics.height
    return object : Icon {
        override fun paintIcon(c: Component?, graphics: Graphics, x: Int, y: Int) {
            val g = graphics.create()
            try {
                GraphicsUtil.setupAntialiasing(g)
                g.font = font
                UIUtil.drawStringWithHighlighting(
                    g,
                    text,
                    x + JBUI.scale(2),
                    y + height - JBUI.scale(1),
                    foreground,
                    JBColor.background()
                )
            } finally {
                g.dispose()
            }
        }

        override fun getIconWidth(): Int {
            return width
        }

        override fun getIconHeight(): Int {
            return height
        }
    }
}

private var numberIcons: MutableMap<String, Icon> = HashMap()
fun getNumberIcon(index: Int, foreground: Color): Icon {
    val key = "$index/$foreground"
    var icon = numberIcons[key]
    if (icon == null) {
        icon = addText(
            CommonIcons.COUNT_BASE,
            if (index == 0) "-" else ("" + index),
            10f,
            SwingConstants.CENTER,
            foreground
        )
        numberIcons[key] = icon
    }
    return icon
}
