/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UNCHECKED_CAST", "unused")

package org.jetbrains.kotlin.com.intellij.util.containers

import org.jetbrains.kotlin.com.intellij.util.ArrayUtil
import java.util.*

/**
 * Renamed to [IdeaQueue] to avoid conflicting with [Queue] which is insufficient
 * for providing full support for [)][org.jetbrains.kotlin.com.intellij.lexer.LookAheadLexer]
 *
 * @param <T>
 */
class IdeaQueue<T>(initialCapacity: Int) {
	private var myArray: Array<Any?> = if (initialCapacity > 0) arrayOfNulls(initialCapacity) else ArrayUtil.EMPTY_OBJECT_ARRAY
	private var myFirst: Int = 0
	private var myLast: Int = 0
	// if true, elements are located at myFirst..myArray.length and 0..myLast
	// otherwise, they are at myFirst..myLast
	private var isWrapped: Boolean = false
	val isEmpty: Boolean get() = size() == 0

	fun addLast(`object`: T) {
		val currentSize = size()
		if (currentSize == myArray.size) {
			myArray = normalize(Math.max(currentSize * 3 / 2, 10))
			myFirst = 0
			myLast = currentSize
			isWrapped = false
		}
		myArray[myLast] = `object`
		myLast++
		if (myLast == myArray.size) {
			isWrapped = !isWrapped
			myLast = 0
		}
	}

	fun removeLast(): T {
		if (myLast == 0) {
			isWrapped = !isWrapped
			myLast = myArray.size
		}
		myLast--
		val result = myArray[myLast] as T
		myArray[myLast] = null
		return result
	}

	fun peekLast(): T {
		var last = myLast
		if (last == 0) last = myArray.size
		return myArray[last - 1] as T
	}

	fun size() = if (isWrapped) myArray.size - myFirst + myLast else myLast - myFirst
	fun toList() = normalize(size()).toList() as List<T>
	fun toArray() = normalize(size())

	fun pullFirst(): T {
		val result = peekFirst()
		myArray[myFirst] = null
		myFirst++
		if (myFirst == myArray.size) {
			myFirst = 0
			isWrapped = !isWrapped
		}
		return result
	}

	fun peekFirst(): T {
		if (isEmpty) throw IndexOutOfBoundsException("queue is empty")
		return myArray[myFirst] as T
	}

	private fun copyFromTo(first: Int, last: Int, result: Array<out Any?>, destinationPos: Int): Int {
		val length = last - first
		System.arraycopy(myArray, first, result, destinationPos, length)
		return length
	}

	private fun normalize(capacity: Int) = normalize(arrayOfNulls(capacity))
	private fun normalize(result: Array<Any?>): Array<Any?> {
		if (isWrapped) {
			val tailLength = copyFromTo(myFirst, myArray.size, result, 0)
			copyFromTo(0, myLast, result, tailLength)
		} else copyFromTo(myFirst, myLast, result, 0)
		return result
	}

	fun clear() {
		Arrays.fill(myArray, null)
		myLast = 0
		myFirst = myLast
		isWrapped = false
	}

	operator fun set(index: Int, value: T): T {
		var arrayIndex = myFirst + index
		if (isWrapped && arrayIndex >= myArray.size) arrayIndex -= myArray.size
		val old = myArray[arrayIndex]
		myArray[arrayIndex] = value
		return old as T
	}

	operator fun get(index: Int): T {
		var arrayIndex = myFirst + index
		if (isWrapped && arrayIndex >= myArray.size) arrayIndex -= myArray.size
		return myArray[arrayIndex] as T
	}

	override fun toString() = when {
		isEmpty -> "<empty>"
		isWrapped -> "[ ${sub(myFirst, myArray.size)} ||| ${sub(0, myLast)} ]"
		else -> "[ ${sub(myFirst, myLast)} ]"
	}

	private fun sub(start: Int, end: Int): Any = if (start == end) "" else Arrays.asList<Any>(*myArray).subList(start, end)
}
