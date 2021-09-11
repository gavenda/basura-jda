package basura

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KProperty

/**
 * Proxies Log4j2 logger into your property.
 */
class Log4j2(val name: String = "") {
    operator fun getValue(thisRef: Any, prop: KProperty<*>): Logger {
        return LogManager.getLogger(thisRef::class.java)
    }

    operator fun getValue(nothing: Nothing?, prop: KProperty<*>): Logger {
        return LogManager.getLogger("basura.$name")
    }
}