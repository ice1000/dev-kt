package org.ice1000.devkt.ui;

import org.ice1000.devkt.openapi.ui.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Provides icon related utils
 *
 * @author ice1000
 * @since v0.0.1
 */
public interface DevKtIcons {
	// Kotlin related
	@NotNull Icon KOTLIN = IconLoader.getIcon("/icon/kotlin/kotlin.png");
	@NotNull Icon KOTLIN_JS = IconLoader.getIcon("/icon/kotlin/kotlin_js.png");
	@NotNull Icon KOTLIN_MP = IconLoader.getIcon("/icon/kotlin/kotlin_multiplatform_project_dark.png");
	@NotNull Icon KOTLIN_FILE = IconLoader.getIcon("/icon/kotlin/kotlin_file.png");
	@NotNull Icon KOTLIN_ANDROID = IconLoader.getIcon("/icon/kotlin/kotlin_activity.png");

	// Project related
	@NotNull Icon OPEN = IconLoader.getIcon("/icon/actions/menu-open.png");
	@NotNull Icon CUT = IconLoader.getIcon("/icon/actions/menu-cut_dark.png");
	@NotNull Icon COPY = IconLoader.getIcon("/icon/actions/copy_dark.png");
	@NotNull Icon PASTE = IconLoader.getIcon("/icon/actions/menu-paste.png");
	@NotNull Icon COMPILE = IconLoader.getIcon("/icon/actions/compile_dark.png");
	@NotNull Icon CLASS = IconLoader.getIcon("/icon/fileTypes/javaClass.png");
	@NotNull Icon EXECUTE = IconLoader.getIcon("/icon/actions/execute.png");
	@NotNull Icon SETTINGS = IconLoader.getIcon("/icon/actions/Gear.png");
	@NotNull Icon DUMP = IconLoader.getIcon("/icon/actions/dump_dark.png");
	@NotNull Icon EXIT = IconLoader.getIcon("/icon/actions/exit_dark.png");
	@NotNull Icon SAVE = IconLoader.getIcon("/icon/actions/menu-saveall.png");
	@NotNull Icon UNDO = IconLoader.getIcon("/icon/actions/undo.png");
	@NotNull Icon REDO = IconLoader.getIcon("/icon/actions/redo.png");
	@NotNull Icon SYNCHRONIZE = IconLoader.getIcon("/icon/actions/synchronizeFS.png");
	@NotNull Icon REFRESH = IconLoader.getIcon("/icon/actions/refresh.png");
	@NotNull Icon JAR = IconLoader.getIcon("/icon/fileTypes/archive.png");
	@NotNull Icon SELECT_ALL = IconLoader.getIcon("/icon/actions/selectall_dark.png");
	@NotNull Icon GRADLE = IconLoader.getIcon("/icon/fileTypes/gradle.png");
	@NotNull Icon MOVE_UP = IconLoader.getIcon("/icon/moveUp.png");
	@NotNull Icon MOVE_DOWN = IconLoader.getIcon("/icon/moveDown.png");
	@NotNull Icon REPLACE = IconLoader.getIcon("/icon/actions/replace_dark.png");
	@NotNull Icon FIND = IconLoader.getIcon("/icon/actions/search_dark.png");

	// Languages
	@NotNull Icon JAVA = IconLoader.getIcon("/icon/fileTypes/java.png");
	@NotNull Icon ANY = IconLoader.getIcon("/icon/fileTypes/any_type.png");

	// Providers
	@NotNull Icon ECLIPSE = IconLoader.getIcon("/icon/alternatives/eclipse_dark.png");
	@NotNull Icon IDEA = IconLoader.getIcon("/icon/alternatives/icon_small_dark.png");
	@NotNull Icon EMACS = IconLoader.getIcon("/icon/alternatives/emacs25.png");
	@NotNull Icon CLION = IconLoader.getIcon("/icon/alternatives/clion.png");
}
