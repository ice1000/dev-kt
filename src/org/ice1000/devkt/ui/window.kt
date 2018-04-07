package org.ice1000.devkt.ui

import charlie.gensokyo.doNothingOnClose
import charlie.gensokyo.show
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.mac
import java.awt.event.*
import javax.swing.JFrame

class DevKtFrame : JFrame() {
	companion object InstanceHolder {
		lateinit var instance: DevKtFrame
	}

	val ui = UIImpl(this)

	init {
		instance = this
		GlobalSettings.windowIcon.second.also {
			iconImage = it
			if (mac) MacSpecific.app.dockIconImage = it
		}
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
		ui.postInit()
		ui.refreshTitle()
	}
}
