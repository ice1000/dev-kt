package org.ice1000.devkt.ui

import org.ice1000.devkt.useDarculaLaf
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyEvent
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileSystemView
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

class KeyBindings : ItemListener {

	/*
	 *  The content pane should be added to a high level container
   */
	val contentPane: JComponent
	private var menuBar: JMenuBar? = null
	private var table: JTable? = null
	private var comboBox: JComboBox<String>? = null
	private val models: Hashtable<String, DefaultTableModel> = Hashtable()

	/*
     *  Constructor
     */
	init {

		contentPane = JPanel(BorderLayout())
		contentPane.add(buildNorthComponent(), BorderLayout.NORTH)
		contentPane.add(buildCenterComponent(), BorderLayout.CENTER)

		resetComponents()
	}

	/*
     *  A menu can also be added which provides the ability to switch
     *  between different LAF's.
     */
	fun getMenuBar(): JMenuBar? {
		if (menuBar == null)
			menuBar = createMenuBar()

		return menuBar
	}

	/*
     *  This panel is added to the North of the content pane
     */
	private fun buildNorthComponent(): JComponent {
		comboBox = JComboBox()

		val label = JLabel("Select Component:")
		label.setDisplayedMnemonic('S')
		label.labelFor = comboBox

		val panel = JPanel()
		panel.border = EmptyBorder(15, 0, 15, 0)
		panel.add(label)
		panel.add(comboBox)
		return panel
	}

	/*
     *  Check the key name to see if it is the UI property
     */
	private fun checkForUIKey(key: String): String? {
		if (key.endsWith("UI") && key.indexOf(".") == -1) {
			val componentName = key.substring(0, key.length - 2)

			//  Ignore these components

			return if (componentName == "PopupMenuSeparator"
					|| componentName == "ToolBarSeparator"
					|| componentName == "DesktopIcon")
				null
			else
				componentName
		}

		return null
	}

	/*
     **  Build the emtpy table to be added in the Center
     */
	private fun buildCenterComponent(): JComponent {
		val model = DefaultTableModel(COLUMN_NAMES, 0)

		table = object : JTable(model) {
			override fun isCellEditable(row: Int, column: Int): Boolean {
				return false
			}
		}

		table!!.autoCreateColumnsFromModel = false
		table!!.columnModel.getColumn(0).preferredWidth = 200
		table!!.columnModel.getColumn(1).preferredWidth = 200
		table!!.columnModel.getColumn(2).preferredWidth = 200
		table!!.columnModel.getColumn(3).preferredWidth = 200
		val d = table!!.preferredSize
		d.height = 350
		table!!.preferredScrollableViewportSize = d

		table!!.tableHeader.isFocusable = true

		return JScrollPane(table)
	}

	/*
     *  When the LAF is changed we need to reset all the items
     */
	fun resetComponents() {
		models.clear()
		(table!!.model as DefaultTableModel).rowCount = 0

		//		buildItemsMap();

		val comboBoxItems = Vector<String>(50)
		val defaults = UIManager.getLookAndFeelDefaults()

		//  All Swing components will have a UI property.

		for (key in defaults.keys) {
			val componentName = checkForUIKey(key.toString())

			if (componentName != null) {
				comboBoxItems.add(componentName)
			}
		}

		Collections.sort(comboBoxItems)

		comboBox!!.removeItemListener(this)
		comboBox!!.setModel(DefaultComboBoxModel(comboBoxItems))
		comboBox!!.selectedIndex = -1
		comboBox!!.addItemListener(this)
		comboBox!!.requestFocusInWindow()

		if (selectedItem != null)
			comboBox!!.selectedItem = selectedItem
	}

	/**
	 * Create menu bar
	 */
	private fun createMenuBar(): JMenuBar {
		val menuBar = JMenuBar()

		menuBar.add(createFileMenu())
		menuBar.add(createLAFMenu())

		return menuBar
	}

	/**
	 * Create menu items for the Application menu
	 */
	private fun createFileMenu(): JMenu {
		val menu = JMenu("Application")
		menu.setMnemonic('A')

		menu.addSeparator()
		menu.add(ExitAction())

		return menu
	}

	/**
	 * Create menu items for the Look & Feel menu
	 */
	private fun createLAFMenu(): JMenu {
		val bg = ButtonGroup()

		val menu = JMenu("Look & Feel")
		menu.setMnemonic('L')

		val lafId = UIManager.getLookAndFeel().id
		val lafInfo = UIManager.getInstalledLookAndFeels()

		for (i in lafInfo.indices) {
			val laf = lafInfo[i].className
			val name = lafInfo[i].name

			val action = ChangeLookAndFeelAction(laf, name)
			val mi = JRadioButtonMenuItem(action)
			menu.add(mi)
			bg.add(mi)

			if (name == lafId) {
				mi.isSelected = true
			}
		}

		return menu
	}

	/*
     *  Implement the ItemListener interface
     */
	override fun itemStateChanged(e: ItemEvent) {
		val componentName = e.item as String
		changeTableModel(getClassName(componentName))
		selectedItem = componentName
	}

	/*
     *  Use the component name to build the class name
     */
	private fun getClassName(componentName: String): String {
		//  The table header is in a child package

		return if (componentName == "TableHeader")
			PACKAGE + "table.JTableHeader"
		else
			PACKAGE + "J" + componentName
	}

	/*
     *  Change the TabelModel in the table for the selected component
     */
	private fun changeTableModel(className: String) {
		//  Check if we have already built the table model for this component

		var model: DefaultTableModel? = models[className]

		if (model != null) {
			table!!.model = model
			return
		}

		//  Create an empty table to start with

		model = DefaultTableModel(COLUMN_NAMES, 0)
		table!!.model = model
		models[className] = model

		//  Create an instance of the component so we can get the default
		//  Action map and Input maps

		val component = try {
			//  Hack so I don't have to sign the jar file for usage in
			//  Java Webstart

			if (className.endsWith("JFileChooser")) {
				JFileChooser(DummyFileSystemView())
			} else {
				val o = Class.forName(className).newInstance()
				o as JComponent
			}
		} catch (e: Exception) {
			val row = arrayOf<Any>(e.toString(), "", "", "")
			model.addRow(row)
			return
		}

		//  Not all components have Actions defined

		val actionMap = component.actionMap
		val keys = actionMap.allKeys()

		if (keys == null) {
			val row = arrayOf<Any>("No actions found", "", "", "")
			model.addRow(row)
			return
		}

		//  In some ActionMaps a key of type Object is found (I have no idea why)
		//  which causes a ClassCastExcption when sorting so we will ignore it
		//  by converting that entry to the empty string

		for (i in keys.indices) {
			val key = keys[i]

			if (key is String)
				continue
			else
				keys[i] = ""
		}

		Arrays.sort(keys)

		//  Create a new row in the model for every Action found

		for (i in keys.indices) {
			val key = keys[i]

			if (key !== "") {
				val row = arrayOf(key, "", "", "")
				model.addRow(row)
			}
		}

		//  Now check each InputMap to see if a KeyStroke is bound the the Action

		updateModelForInputMap(model, 1, component.getInputMap(JComponent.WHEN_FOCUSED))
		updateModelForInputMap(model, 2, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW))
		updateModelForInputMap(model, 3, component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT))
	}

	/*
     *  The model is potentially update for each of the 3 different InputMaps
     */
	private fun updateModelForInputMap(model: TableModel, column: Int, inputMap: InputMap?) {
		if (inputMap == null) return

		val keys = inputMap.allKeys() ?: return

//  The InputMap is keyed by KeyStroke, however we want to be able to
		//  access the action names that are bound to a KeyStroke so we will create
		//  a Hashtble that is keyed by action name.
		//  Note that multiple KeyStrokes can be bound to the same action name.

		val actions = Hashtable<Any, String>(keys.size)

		for (i in keys.indices) {
			val key = keys[i]
			val actionName = inputMap.get(key)

			val value = actions[actionName]

			if (value == null) {
				actions[actionName] = key.toString().replace("pressed ", "")
			} else {
				actions[actionName] = value + ", " + key.toString().replace("pressed ", "")
			}
		}

		//  Now we can update the model for those actions that have
		//  KeyStrokes mapped to them

		for (i in 0 until model.rowCount) {
			val o = actions[model.getValueAt(i, 0)]

			if (o != null) {
				model.setValueAt(o.toString(), i, column)
			}
		}
	}

	/*
     *  Change the LAF and recreate the UIManagerDefaults so that the properties
     *  of the new LAF are correctly displayed.
     */
	internal inner class ChangeLookAndFeelAction(private val laf: String, name: String) : AbstractAction() {

		init {
			putValue(Action.NAME, name)
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME))
		}

		override fun actionPerformed(e: ActionEvent) {
			try {
				val mi = e.source as JMenuItem
				val popup = mi.parent as JPopupMenu
				val rootPane = SwingUtilities.getRootPane(popup.invoker)
				val c = rootPane.contentPane.getComponent(0)
				rootPane.contentPane.remove(c)

				UIManager.setLookAndFeel(laf)
				val bindings = KeyBindings()
				rootPane.contentPane.add(bindings.contentPane)
				SwingUtilities.updateComponentTreeUI(rootPane)
				rootPane.requestFocusInWindow()
			} catch (ex: Exception) {
				println("Failed loading L&F: $laf")
				println(ex)
			}

		}
	}

	/*
     *	Close the frame
     */
	internal inner class ExitAction : AbstractAction() {
		init {
			putValue(Action.NAME, "Exit")
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME))
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X)
		}

		override fun actionPerformed(e: ActionEvent) {
			System.exit(0)
		}
	}


	/**
	 * Dummy FileSystemView to get around the Security Exception when trying to
	 * access the File System in Java Webstart.
	 */
	internal inner class DummyFileSystemView : FileSystemView() {
		override fun createNewFolder(containingDir: File): File? {
			return null
		}

		override fun getDefaultDirectory(): File? {
			return null
		}

		override fun getHomeDirectory(): File? {
			return null
		}
	}

	companion object {
		private const val PACKAGE = "javax.swing."

		private val COLUMN_NAMES = arrayOf("Action", "When Focused", "When In Focused Window", "When Ancestor")

		private var selectedItem: String? = null

		/*
     *  Build a GUI using the content pane and menu bar of KeyBindings
     */
		private fun createAndShowGUI() {
			val application = KeyBindings()
			useDarculaLaf()
			val frame = JFrame("Key Bindings")
			frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
			frame.jMenuBar = application.getMenuBar()
			frame.contentPane.add(application.contentPane)
			frame.pack()
			frame.setLocationRelativeTo(null)
			frame.isVisible = true
		}

		/**
		 * KeyBindings Main. Called only if we're an application, not an applet.
		 */
		@JvmStatic
		fun main(args: Array<String>) {
			//		UIManager.put("swing.boldMetal", Boolean.FALSE);

			SwingUtilities.invokeLater { createAndShowGUI() }
		}
	}
}