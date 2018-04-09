package org.ice1000.devkt.kts;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileSystemUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.ui.UIUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.types.FlexibleTypeImpl;
import org.jetbrains.kotlin.utils.ExceptionUtilsKt;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KtUsefulTestCase extends TestCase {
	private static final String TEMP_DIR_MARKER = "unitTest_";

	private static final String ORIGINAL_TEMP_DIR = FileUtil.getTempDirectory();

	private static final Map<String, Long> TOTAL_SETUP_COST_MILLIS = new HashMap<>();
	private static final Map<String, Long> TOTAL_TEARDOWN_COST_MILLIS = new HashMap<>();

	private Application application;

	static {
		Logger.setFactory(TestLoggerFactory.class);
	}

	@NotNull private final Disposable myTestRootDisposable = new TestDisposable();

	private String myTempDir;

	static {
		// Radar #5755208: Command line Java applications need a way to launch without a Dock icon.
		System.setProperty("apple.awt.UIElement", "true");

		FlexibleTypeImpl.RUN_SLOW_ASSERTIONS = true;
	}

	private boolean oldDisposerDebug;

	@Override protected void setUp() throws Exception {
		application = ApplicationManager.getApplication();

		super.setUp();

		String testName = FileUtil.sanitizeFileName(getTestName(true));
		if (StringUtil.isEmptyOrSpaces(testName)) testName = "";
		testName = new File(testName).getName(); // in case the test name contains file separators
		myTempDir = new File(ORIGINAL_TEMP_DIR, TEMP_DIR_MARKER + testName).getPath();
		FileUtil.resetCanonicalTempPathCache(myTempDir);
		boolean isStressTest = isStressTest();
		ApplicationInfoImpl.setInStressTest(isStressTest);
		// turn off Disposer debugging for performance tests
		oldDisposerDebug = Disposer.setDebugMode(Disposer.isDebugMode() && !isStressTest);
	}

	@Override protected void tearDown() throws Exception {
		try {
			Disposer.dispose(myTestRootDisposable);
			cleanupSwingDataStructures();
			cleanupDeleteOnExitHookList();
		} finally {
			Disposer.setDebugMode(oldDisposerDebug);
			FileUtil.resetCanonicalTempPathCache(ORIGINAL_TEMP_DIR);
			FileUtil.delete(new File(myTempDir));
		}

		UIUtil.removeLeakingAppleListeners();
		super.tearDown();

		resetApplicationToNull(application);

		application = null;
	}

	private static void resetApplicationToNull(Application old) {
		if (old != null) return;
		resetApplicationToNull();
	}

	private static void resetApplicationToNull() {
		try {
			Field ourApplicationField = ApplicationManager.class.getDeclaredField("ourApplication");
			ourApplicationField.setAccessible(true);
			ourApplicationField.set(null, null);
		} catch (Exception e) {
			throw ExceptionUtilsKt.rethrow(e);
		}
	}

	private static final Set<String> DELETE_ON_EXIT_HOOK_DOT_FILES;
	private static final Class DELETE_ON_EXIT_HOOK_CLASS;

	static {
		Class<?> aClass;
		try {
			aClass = Class.forName("java.io.DeleteOnExitHook");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set<String> files = ReflectionUtil.getStaticFieldValue(aClass, Set.class, "files");
		DELETE_ON_EXIT_HOOK_CLASS = aClass;
		DELETE_ON_EXIT_HOOK_DOT_FILES = files;
	}

	private static void cleanupDeleteOnExitHookList() {
		// try to reduce file set retained by java.io.DeleteOnExitHook
		List<String> list;
		synchronized (DELETE_ON_EXIT_HOOK_CLASS) {
			if (DELETE_ON_EXIT_HOOK_DOT_FILES.isEmpty()) return;
			list = new ArrayList<>(DELETE_ON_EXIT_HOOK_DOT_FILES);
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			String path = list.get(i);
			if (FileSystemUtil.getAttributes(path) == null || new File(path).delete()) {
				synchronized (DELETE_ON_EXIT_HOOK_CLASS) {
					DELETE_ON_EXIT_HOOK_DOT_FILES.remove(path);
				}
			}
		}
	}

	private static void cleanupSwingDataStructures() throws Exception {
		Object manager = ReflectionUtil.getDeclaredMethod(Class.forName("javax.swing.KeyboardManager"), "getCurrentManager")
				.invoke(null);
		Map componentKeyStrokeMap = ReflectionUtil.getField(manager.getClass(),
				manager,
				Hashtable.class,
				"componentKeyStrokeMap");
		componentKeyStrokeMap.clear();
		Map containerMap = ReflectionUtil.getField(manager.getClass(), manager, Hashtable.class, "containerMap");
		containerMap.clear();
	}

	@Override protected void runTest() throws Throwable {
		Throwable[] throwables = new Throwable[1];

		AtomicBoolean completed = new AtomicBoolean(false);
		Runnable runnable = () -> {
			try {
				super.runTest();
				completed.set(true);
			} catch (InvocationTargetException e) {
				e.fillInStackTrace();
				throwables[0] = e.getTargetException();
			} catch (IllegalAccessException e) {
				e.fillInStackTrace();
				throwables[0] = e;
			} catch (Throwable e) {
				throwables[0] = e;
			}
		};

		invokeTestRunnable(runnable);

		if (throwables[0] != null) {
			throw throwables[0];
		}
		if (!completed.get()) {
			throw new IllegalStateException("test didn't start");
		}
	}

	private static void invokeTestRunnable(@NotNull Runnable runnable) {
		EdtTestUtil.runInEdtAndWait(runnable::run);
	}

	private void defaultRunBare() throws Throwable {
		Throwable exception = null;
		try {
			long setupStart = System.nanoTime();
			setUp();
			long setupCost = (System.nanoTime() - setupStart) / 1000000;
			logPerClassCost(setupCost, TOTAL_SETUP_COST_MILLIS);

			runTest();
			TestLoggerFactory.onTestFinished(true);
		} catch (Throwable running) {
			TestLoggerFactory.onTestFinished(false);
			exception = running;
		} finally {
			try {
				long teardownStart = System.nanoTime();
				tearDown();
				long teardownCost = (System.nanoTime() - teardownStart) / 1000000;
				logPerClassCost(teardownCost, TOTAL_TEARDOWN_COST_MILLIS);
			} catch (Throwable tearingDown) {
				if (exception == null) exception = tearingDown;
			}
		}
		if (exception != null) throw exception;
	}

	/**
	 * Logs the setup cost grouped by test fixture class (superclass of the current test class).
	 *
	 * @param cost setup cost in milliseconds
	 */
	private void logPerClassCost(long cost, Map<String, Long> costMap) {
		Class<?> superclass = getClass().getSuperclass();
		Long oldCost = costMap.get(superclass.getName());
		long newCost = oldCost == null ? cost : oldCost + cost;
		costMap.put(superclass.getName(), newCost);
	}

	@Override public void runBare() throws Throwable {
		this.defaultRunBare();
	}

	private String getTestName(boolean lowercaseFirstLetter) {
		return getTestName(getName(), lowercaseFirstLetter);
	}

	private static String getTestName(String name, boolean lowercaseFirstLetter) {
		return name;
	}

	private static boolean isPerformanceTest(@Nullable String testName, @Nullable String className) {
		return testName != null && testName.contains("Performance") ||
				className != null && className.contains("Performance");
	}

	/**
	 * @return true for a test which performs A LOT of computations.
	 * Such test should typically avoid performing expensive checks, e.g. data structure consistency complex validations.
	 * If you want your test to be treated as "Stress", please mention one of these words in its name: "Stress", "Slow".
	 * For example: {@code public void testStressPSIFromDifferentThreads()}
	 */

	private boolean isStressTest() {
		return isStressTest(getName(), getClass().getName());
	}

	private static boolean isStressTest(String testName, String className) {
		return isPerformanceTest(testName, className) || containsStressWords(testName) || containsStressWords(className);
	}

	private static boolean containsStressWords(@Nullable String name) {
		return name != null && (name.contains("Stress") || name.contains("Slow"));
	}

	public class TestDisposable implements Disposable {
		@Override public void dispose() {
		}

		@Override public String toString() {
			String testName = getTestName(false);
			return KtUsefulTestCase.this.getClass() + (StringUtil.isEmpty(testName) ? "" : ".test" + testName);
		}
	}

}