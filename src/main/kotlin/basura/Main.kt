package basura

import kotlinx.coroutines.DelicateCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import org.apache.logging.log4j.LogManager
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import java.net.SocketException
import javax.sql.DataSource
import kotlin.system.exitProcess

@DelicateCoroutinesApi
fun main() {
    val log = LogManager.getLogger("Main")

    try {
        val jda by basura.instance<JDA>()
        val dataSource by basura.instance<DataSource>()

        // Migrate database
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()

        // Await and update commands
        jda.awaitReady()
        jda.updateBasuraCommands().queue()

        jda.presence.activity = Activity.competing("Trash")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = jda.shutdown()
        })
    } catch (e: SocketException) {
        // Unable to connect, exit
        log.error("Cannot connect", e)
        exitProcess(1)
    } catch (e: Exception) {
        // Something went wrong and we do not know
        log.error("Cannot start", e)
        exitProcess(-1)
    }

}