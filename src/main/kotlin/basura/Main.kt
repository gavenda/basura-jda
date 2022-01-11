package basura

import dev.minn.jda.ktx.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import java.net.SocketException
import javax.sql.DataSource
import kotlin.system.exitProcess

fun main() = runBlocking {
    val log by Log4j2("Main")

    try {
        val jda by bot.instance<JDA>()
        val dataSource by bot.instance<DataSource>()

        // Migrate database
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()

        // Await and update commands
        jda.await<ReadyEvent>()
        // Only update when specified
        if (Environment.BOT_UPDATE_COMMANDS) {
            jda.updateBotCommands().await()
        }
        jda.presence.setPresence(OnlineStatus.ONLINE, Activity.competing("Trash"))
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