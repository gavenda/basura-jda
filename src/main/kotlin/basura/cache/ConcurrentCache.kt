package basura.cache

import java.util.concurrent.ConcurrentHashMap

class ConcurrentCache<K : Any, V> : Cache<K, V> {
    private val cache = ConcurrentHashMap<K, V>()

    override val size: Int
        get() = cache.size

    override fun set(key: K, value: V) {
        this.cache[key] = value
    }

    override fun remove(key: K) = this.cache.remove(key)

    override fun get(key: K) = this.cache[key]

    override fun clear() = this.cache.clear()

    override fun getOrPut(key: K, defaultValue: () -> V): V = this.cache.getOrPut(key, defaultValue)
}