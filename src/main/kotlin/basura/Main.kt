package basura

import kotlinx.coroutines.DelicateCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import javax.sql.DataSource

internal const val GUILD_BOGUS_ID = 369435836627812352

@DelicateCoroutinesApi
fun main() {
    val jda by basura.instance<JDA>()
    val dataSource by basura.instance<DataSource>()

    // Migrate database
    Flyway.configure()
        .dataSource(dataSource)
        .load()
        .migrate()

    // Await and update commands
    jda.awaitReady()
    jda.getGuildById(GUILD_BOGUS_ID)
        ?.updateBasuraCommands()
        ?.queue()
    jda.updateBasuraCommands().queue()

    jda.presence.activity = Activity.competing("Trash")

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() = jda.shutdown()
    })
}