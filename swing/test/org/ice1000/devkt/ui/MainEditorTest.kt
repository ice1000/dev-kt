package org.ice1000.devkt.ui

import org.junit.Test


/**
 * @author zxj5470
 * @date 2018/4/7
 */
class MainEditorTest {
	@Test
	fun testReplace() {
		val java = "java -cp xxxxxx devkt.Main"
		val javaExe = "/usr/bin/java"
		val fileContent = java.replaceFirst("java", javaExe).replaceFirst(" devkt.", " ")
		println(fileContent)
	}
}