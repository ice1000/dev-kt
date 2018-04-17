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
package org.jetbrains.kotlin.com.intellij.util.containers

import org.jetbrains.annotations.Contract
import org.jetbrains.kotlin.com.intellij.openapi.util.Key

/**
 * @author peter
 */
abstract class ImmutableUserMap private constructor() {
	abstract operator fun <T> get(key: Key<T>): T?

	fun <T> put(key: Key<T>, value: T): ImmutableUserMap = ImmutableUserMapImpl(key, value, this)

	private class ImmutableUserMapImpl<V>(private val myKey: Key<V>, private val myValue: V, private val myNext: ImmutableUserMap) : ImmutableUserMap() {
		@Suppress("UNCHECKED_CAST")
		override fun <T> get(key: Key<T>) = if (key == myKey) myValue as T else myNext[key]
	}

	companion object {
		@JvmField
		val EMPTY: ImmutableUserMap = object : ImmutableUserMap() {
			@Contract(pure = true)
			override fun <T> get(key: Key<T>): T? = null
		}
	}
}
