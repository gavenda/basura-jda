package basura.discord

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KProperty

/**
 * Proxies Log4j2 logger into your property.
 */
object Log4j2 {
    operator fun getValue(thisRef: Any, prop: KProperty<*>): Logger {
        return LogManager.getLogger(thisRef::class.java)
    }
}
