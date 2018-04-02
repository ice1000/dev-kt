package org.ice1000.devkt;

import com.bulenkov.iconloader.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Provides icon related utils
 *
 * @since v0.0.1
 * @author ice1000
 */
public interface Icons {
	// Kotlin related
	@NotNull Icon KOTLIN = IconLoader.getIcon("/icon/kotlin.png");
	@NotNull Icon KOTLIN_JS = IconLoader.getIcon("/icon/kotlin_js.png");
	@NotNull Icon KOTLIN_MP = IconLoader.getIcon("/icon/kotlin_multiplatform_project_dark.png");
	@NotNull Icon KOTLIN_FILE = IconLoader.getIcon("/icon/kotlin_file.png");
	@NotNull Icon KOTLIN_ANDROID = IconLoader.getIcon("/icon/kotlin_activity.png");
	@NotNull Icon KOTLIN_BIG = IconLoader.getIcon("/icon/kotlin@2x.png");

	@NotNull Icon OPEN = IconLoader.getIcon("/icon/menu-open.png");
	@NotNull Icon CUT = IconLoader.getIcon("/icon/menu-cut_dark.png");
	@NotNull Icon COPY = IconLoader.getIcon("/icon/copy_dark.png");
	@NotNull Icon PASTE = IconLoader.getIcon("/icon/menu-paste.png");
	@NotNull Icon COMPILE = IconLoader.getIcon("/icon/compile_dark.png");
	@NotNull Icon EXECUTE = IconLoader.getIcon("/icon/execute.png");
	@NotNull Icon SETTINGS = IconLoader.getIcon("/icon/Gear.png");
	@NotNull Icon DUMP = IconLoader.getIcon("/icon/dump_dark.png");
	@NotNull Icon EXIT = IconLoader.getIcon("/icon/exit_dark.png");
	@NotNull Icon SAVE = IconLoader.getIcon("/icon/menu-saveall.png");
	@NotNull Icon UNDO = IconLoader.getIcon("/icon/undo.png");
	@NotNull Icon REDO = IconLoader.getIcon("/icon/redo.png");
	@NotNull Icon SYNCHRONIZE = IconLoader.getIcon("/icon/synchronizeFS.png");
	@NotNull Icon JAR = IconLoader.getIcon("/icon/archive.png");

	static @NotNull Image iconToImage(@NotNull final Icon icon) {
		if (icon instanceof ImageIcon) return ((ImageIcon) icon).getImage();
		else {
			BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice()
					.getDefaultConfiguration()
					.createCompatibleImage(icon.getIconWidth(), icon.getIconHeight());
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
		}
	}
}
