package org.ice1000.devkt


/**
 * @author zxj5470
 * @date 2018/4/3
 */
object SystemInfo {
	private val name: String
		get() = System.getProperty("os.name")

	val isMac: Boolean
		get() = name.startsWith("Mac")
}

object system {
	operator fun set(key: String, value: Any) {
		System.setProperty(key, value.toString())
	}
}
