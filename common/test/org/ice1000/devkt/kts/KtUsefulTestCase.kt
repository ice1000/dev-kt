package org.ice1000.devkt.kts

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileSystemUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.util.ReflectionUtil
import com.intellij.util.ThrowableRunnable
import com.intellij.util.containers.hash.HashMap
import com.intellij.util.ui.UIUtil
import junit.framework.TestCase
import org.jetbrains.kotlin.types.FlexibleTypeImpl
import org.jetbrains.kotlin.utils.rethrow
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class KtUsefulTestCase : TestCase() {
	private val myTestRootDisposable = TestDisposable()
	private lateinit var myTempDir: String
	private var oldDisposerDebug: Boolean = false

	/**
	 * @return true for a test which performs A LOT of computations.
	 * Such test should typically avoid performing expensive checks, e.g. data structure consistency complex validations.
	 * If you want your test to be treated as "Stress", please mention one of these words in its name: "Stress", "Slow".
	 * For example: `public void testStressPSIFromDifferentThreads()`
	 */

	private val isStressTest: Boolean
		get() = isStressTest(name, javaClass.name)

	@Throws(Exception::class)
	override fun setUp() {
		super.setUp()
		var testName = FileUtil.sanitizeFileName(name)
		if (StringUtil.isEmptyOrSpaces(testName)) testName = ""
		testName = File(testName).name // in case the test name contains file separators
		myTempDir = File(ORIGINAL_TEMP_DIR, TEMP_DIR_MARKER + testName).path
		FileUtil.resetCanonicalTempPathCache(myTempDir)
		val isStressTest = isStressTest
		ApplicationInfoImpl.setInStressTest(isStressTest)
		// turn off Disposer debugging for performance tests
		oldDisposerDebug = Disposer.setDebugMode(Disposer.isDebugMode() && !isStressTest)
	}

	@Throws(Exception::class)
	override fun tearDown() {
		try {
			Disposer.dispose(myTestRootDisposable)
			cleanupSwingDataStructures()
			cleanupDeleteOnExitHookList()
		} finally {
			Disposer.setDebugMode(oldDisposerDebug)
			FileUtil.resetCanonicalTempPathCache(ORIGINAL_TEMP_DIR)
			FileUtil.delete(File(myTempDir))
		}

		UIUtil.removeLeakingAppleListeners()
		super.tearDown()
	}

	@Throws(Throwable::class)
	override fun runTest() {
		var throwable: Throwable? = null
		val completed = AtomicBoolean(false)
		val runnable = Runnable {
			try {
				super.runTest()
				completed.set(true)
			} catch (e: InvocationTargetException) {
				e.fillInStackTrace()
				throwable = e.targetException
			} catch (e: IllegalAccessException) {
				e.fillInStackTrace()
				throwable = e
			} catch (e: Throwable) {
				throwable = e
			}
		}
		invokeTestRunnable(runnable)
		if (throwable != null) throw throwable!!
		if (!completed.get()) throw IllegalStateException("test didn't start")
	}

	@Throws(Throwable::class)
	private fun defaultRunBare() {
		var exception: Throwable? = null
		try {
			val setupStart = System.nanoTime()
			setUp()
			val setupCost = (System.nanoTime() - setupStart) / 1000000
			logPerClassCost(setupCost, TOTAL_SETUP_COST_MILLIS)
			runTest()
			TestLoggerFactory.onTestFinished(true)
		} catch (running: Throwable) {
			TestLoggerFactory.onTestFinished(false)
			exception = running
		} finally {
			try {
				val teardownStart = System.nanoTime()
				tearDown()
				val teardownCost = (System.nanoTime() - teardownStart) / 1000000
				logPerClassCost(teardownCost, TOTAL_TEARDOWN_COST_MILLIS)
			} catch (tearingDown: Throwable) {
				if (exception == null) exception = tearingDown
			}

		}
		if (exception != null) throw exception
	}

	/**
	 * Logs the setup cost grouped by test fixture class (superclass of the current test class).
	 *
	 * @param cost setup cost in milliseconds
	 */
	private fun logPerClassCost(cost: Long, costMap: MutableMap<String, Long>) {
		val superclass = javaClass.superclass
		val oldCost = costMap[superclass.name]
		val newCost = if (oldCost == null) cost else oldCost + cost
		costMap[superclass.name] = newCost
	}

	@Throws(Throwable::class)
	override fun runBare() = this.defaultRunBare()

	inner class TestDisposable : Disposable {
		override fun dispose() = Unit
		override fun toString(): String {
			val testName = name
			return this@KtUsefulTestCase.javaClass.toString() + if (StringUtil.isEmpty(testName)) "" else ".test$testName"
		}
	}

	companion object {
		private const val TEMP_DIR_MARKER = "unitTest_"

		private val ORIGINAL_TEMP_DIR = FileUtil.getTempDirectory()

		private val TOTAL_SETUP_COST_MILLIS = HashMap<String, Long>()
		private val TOTAL_TEARDOWN_COST_MILLIS = HashMap<String, Long>()

		init {
			Logger.setFactory(TestLoggerFactory::class.java)
		}

		init {
			// Radar #5755208: Command line Java applications need a way to launch without a Dock icon.
			System.setProperty("apple.awt.UIElement", "true")

			FlexibleTypeImpl.RUN_SLOW_ASSERTIONS = true
		}

		private fun resetApplicationToNull(old: Application?) {
			if (old != null) return
			resetApplicationToNull()
		}

		private fun resetApplicationToNull() {
			try {
				val ourApplicationField = ApplicationManager::class.java.getDeclaredField("ourApplication")
				ourApplicationField.isAccessible = true
				ourApplicationField.set(null, null)
			} catch (e: Exception) {
				throw rethrow(e)
			}

		}

		private val DELETE_ON_EXIT_HOOK_DOT_FILES: MutableSet<String>
		private val DELETE_ON_EXIT_HOOK_CLASS: Class<*>

		init {
			val aClass: Class<*>
			try {
				aClass = Class.forName("java.io.DeleteOnExitHook")
			} catch (e: Exception) {
				throw RuntimeException(e)
			}

			val files = ReflectionUtil.getStaticFieldValue(aClass, Set::class.java, "files") as Set<String>
			DELETE_ON_EXIT_HOOK_CLASS = aClass
			DELETE_ON_EXIT_HOOK_DOT_FILES = files.toMutableSet()
		}

		private fun cleanupDeleteOnExitHookList() {
			// try to reduce file set retained by java.io.DeleteOnExitHook
			var list: List<String> = emptyList()
			synchronized(DELETE_ON_EXIT_HOOK_CLASS) {
				if (DELETE_ON_EXIT_HOOK_DOT_FILES.isEmpty()) return
				list = ArrayList(DELETE_ON_EXIT_HOOK_DOT_FILES)
			}
			for (i in list.indices.reversed()) {
				val path = list[i]
				if (FileSystemUtil.getAttributes(path) == null || File(path).delete()) {
					synchronized(DELETE_ON_EXIT_HOOK_CLASS) {
						DELETE_ON_EXIT_HOOK_DOT_FILES.remove(path)
					}
				}
			}
		}

		@Throws(Exception::class)
		private fun cleanupSwingDataStructures() {
			val manager = (ReflectionUtil.getDeclaredMethod(Class.forName("javax.swing.KeyboardManager"), "getCurrentManager")!!)(null)
			val componentKeyStrokeMap = ReflectionUtil.getField(manager.javaClass,
					manager, Hashtable::class.java, "componentKeyStrokeMap")
			componentKeyStrokeMap.clear()
			ReflectionUtil.getField(manager.javaClass, manager, Hashtable::class.java, "containerMap").clear()
		}

		private fun invokeTestRunnable(runnable: Runnable) {
			EdtTestUtil.runInEdtAndWait(ThrowableRunnable(runnable::run))
		}

		private fun isPerformanceTest(testName: String?, className: String?): Boolean =
				testName != null && testName.contains("Performance") || className != null && className.contains("Performance")

		private fun isStressTest(testName: String, className: String): Boolean =
				isPerformanceTest(testName, className) || containsStressWords(testName) || containsStressWords(className)

		private fun containsStressWords(name: String?): Boolean =
				name != null && (name.contains("Stress") || name.contains("Slow"))
	}

}