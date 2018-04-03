package org.ice1000.devkt.ui;

import com.bulenkov.iconloader.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Provides icon related utils
 *
 * @author ice1000
 * @since v0.0.1
 */
public interface AllIcons {
	// Kotlin related
	@NotNull Icon KOTLIN = IconLoader.getIcon("/icon/kotlin.png");
	@NotNull Icon KOTLIN_JS = IconLoader.getIcon("/icon/kotlin_js.png");
	@NotNull Icon KOTLIN_MP = IconLoader.getIcon("/icon/kotlin_multiplatform_project_dark.png");
	@NotNull Icon KOTLIN_FILE = IconLoader.getIcon("/icon/kotlin_file.png");
	@NotNull Icon KOTLIN_ANDROID = IconLoader.getIcon("/icon/kotlin_activity.png");

	// Project related
	@NotNull Icon OPEN = IconLoader.getIcon("/icon/menu-open.png");
	@NotNull Icon CUT = IconLoader.getIcon("/icon/menu-cut_dark.png");
	@NotNull Icon COPY = IconLoader.getIcon("/icon/copy_dark.png");
	@NotNull Icon PASTE = IconLoader.getIcon("/icon/menu-paste.png");
	@NotNull Icon COMPILE = IconLoader.getIcon("/icon/compile_dark.png");
	@NotNull Icon CLASS = IconLoader.getIcon("/icon/javaClass.png");
	@NotNull Icon EXECUTE = IconLoader.getIcon("/icon/execute.png");
	@NotNull Icon SETTINGS = IconLoader.getIcon("/icon/Gear.png");
	@NotNull Icon DUMP = IconLoader.getIcon("/icon/dump_dark.png");
	@NotNull Icon EXIT = IconLoader.getIcon("/icon/exit_dark.png");
	@NotNull Icon SAVE = IconLoader.getIcon("/icon/menu-saveall.png");
	@NotNull Icon UNDO = IconLoader.getIcon("/icon/undo.png");
	@NotNull Icon REDO = IconLoader.getIcon("/icon/redo.png");
	@NotNull Icon SYNCHRONIZE = IconLoader.getIcon("/icon/synchronizeFS.png");
	@NotNull Icon JAR = IconLoader.getIcon("/icon/archive.png");

	// Providers
	@NotNull Icon ECLIPSE = IconLoader.getIcon("/icon/eclipse_dark.png");
	@NotNull Icon IDEA = IconLoader.getIcon("/icon/icon_small_dark.png");
	@NotNull Icon EMACS = IconLoader.getIcon("/icon/emacs25.png");
	@NotNull Icon CLION = IconLoader.getIcon("/icon/clion.png");

	@NotNull
	Icon KOTLIN_BIG_ICON = IconLoader.getIcon("/icon/kotlin@288x288.png");
}
