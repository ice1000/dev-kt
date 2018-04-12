package org.ice1000.devkt.ui

import charlie.gensokyo.doNothingOnClose
import charlie.gensokyo.show
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.ui.swing.UIImpl
import java.awt.event.*
import javax.swing.JFrame

class DevKtFrame : JFrame() {
	companion object {
		lateinit var instance: DevKtFrame
	}

	val ui = UIImpl(this)

	init {
		instance = this
		iconImage = GlobalSettings.windowIcon.second
		add(ui.mainPanel)
		addWindowListener(object : WindowAdapter() {
			override fun windowDeactivated(e: WindowEvent?) = GlobalSettings.save()
			override fun windowLostFocus(e: WindowEvent?) = GlobalSettings.save()
			override fun windowClosing(e: WindowEvent?) {
				GlobalSettings.save()
				ui.exit()
			}
		})
		addComponentListener(object : ComponentAdapter() {
			override fun componentMoved(e: ComponentEvent?) {
				GlobalSettings.windowBounds = bounds
			}

			override fun componentResized(e: ComponentEvent?) {
				super.componentResized(e)
				GlobalSettings.windowBounds = bounds
				ui.imageCache = null
			}
		})
		bounds = GlobalSettings.windowBounds
		doNothingOnClose
		show
		with(ui) {
			postInit()
			refreshTitle()
		}
	}
}
