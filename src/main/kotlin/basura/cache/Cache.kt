/**
 * Copyright 2020 kezhenxu94
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

package basura.cache

/**
 * A Generic K,V [Cache] defines the basic operations to a cache.
 */
interface Cache<K : Any, V> {
    /**
     * The number of the items that are currently cached.
     */
    val size: Int

    /**
     * Cache a [value] with a given [key]
     */
    operator fun set(key: K, value: V)

    /**
     * Get the cached value of a given [key], or null if it's not cached or evicted.
     */
    operator fun get(key: K): V?

    /**
     * Returns the value for the given [key]. If the [key] is not found in the cache,
     * calls the [defaultValue] function, puts its result into the cache under the given [key] and returns it.
    *  Note that the operation is not guaranteed to be atomic unless the underlying cache is guaranteed atomic.
     */
    fun getOrPut(key: K, defaultValue: () -> V): V

    /**
     * Remove the value of the [key] from the cache, and return the removed value, or null if it's not cached at all.
     */
    fun remove(key: K): V?

    /**
     * Remove all the items in the cache.
     */
    fun clear()
}