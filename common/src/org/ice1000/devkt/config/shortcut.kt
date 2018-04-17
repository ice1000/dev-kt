@file:Suppress("EnumEntryName")

package org.ice1000.devkt.config

import org.ice1000.devkt.config.Key.Companion.ALT_DOWN_MASK
import org.ice1000.devkt.config.Key.Companion.CTRL_DOWN_MASK
import org.ice1000.devkt.config.Key.Companion.SHIFT_DOWN_MASK
import java.awt.event.KeyEvent

class ShortCut {
	companion object Modifiers {
		fun valueOf(str: String) = try {
			str.split("|").let { (keyCode, modifier) ->
				ShortCut(modifier.toInt(), Key.valueOf(keyCode))
			}
		} catch (e: Throwable) {
			null
		}
	}

	val isControl: Boolean
	val isAlt: Boolean
	val isShift: Boolean
	val key: Key
	val modifier: Int

	constructor(isControl: Boolean, isAlt: Boolean, isShift: Boolean, keyCode: Key) {
		this.isControl = isControl
		this.isAlt = isAlt
		this.isShift = isShift
		this.key = keyCode
		this.modifier = (if (isControl) CTRL_DOWN_MASK else 0) or
				(if (isAlt) ALT_DOWN_MASK else 0) or
				(if (isShift) SHIFT_DOWN_MASK else 0)
	}

	/**
	 * @param modifier Int
	 * @param key Int
	 * @constructor use this constructor to prevent `ctrl` being transformed into `meta` in Mac
	 */
	constructor(modifier: Int, key: Key) {
		this.modifier = modifier
		this.key = key
		this.isControl = modifier and CTRL_DOWN_MASK != 0
		this.isShift = modifier and SHIFT_DOWN_MASK != 0
		this.isAlt = modifier and ALT_DOWN_MASK != 0
	}

	fun check(e: KeyEvent) = e.modifiers == modifier

	override fun toString(): String = "$key|$modifier"
}

enum class Key {
	A,
	B,
	C,
	D,
	E,
	F,
	G,
	H,
	I,
	J,
	K,
	L,
	M,
	N,
	O,
	P,
	Q,
	R,
	S,
	T,
	U,
	V,
	W,
	X,
	Y,
	Z,
	`0`,
	`1`,
	`2`,
	`3`,
	`4`,
	`5`,
	`6`,
	`7`,
	`8`,
	SLASH,
	ENTER;

	companion object {
		const val SHIFT_MASK = 1
		const val CTRL_MASK = 2
		const val META_MASK = 4
		const val ALT_MASK = 8
		const val ALT_GRAPH_MASK = 32
		const val SHIFT_DOWN_MASK = 64
		const val CTRL_DOWN_MASK = 128
		const val META_DOWN_MASK = 256
		const val ALT_DOWN_MASK = 512
	}
}
