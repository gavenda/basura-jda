package basura

import kotlinx.coroutines.DelicateCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import java.net.SocketException
import javax.sql.DataSource
import kotlin.system.exitProcess

@DelicateCoroutinesApi
fun main() {
    val log by Log4j2("Main")

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
        // Only update when specified
        if (Environment.BOT_UPDATE_COMMANDS) {
            jda.updateBasuraCommands().queue()
        }

        jda.presence.activity = Activity.competing("Trash")
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