package basura

import basura.command.*
import basura.discord.defaultExceptionHandler
import basura.discord.useCoroutines
import basura.graphql.AniList
import basura.graphql.AniListGraphQL
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.ktorm.database.Database
import java.io.File
import java.net.SocketTimeoutException
import javax.sql.DataSource

val basura = DI {
    bind<DataSource>() with singleton {
        val config = HikariConfig()

        config.jdbcUrl = Environment.DB_URL
        config.username = Environment.DB_USER
        config.password = Environment.DB_PASS

        HikariDataSource(config)
    }
    bind<Database>() with singleton {
        val dataSource by di.instance<DataSource>()

        Database.connect(dataSource)
    }
    bind<OkHttpClient>() with singleton {
        val cacheDir = File("cache")
        val cacheSize: Long = (1024 * 1024) * 5 // 5MB cache

        OkHttpClient.Builder()
            .cache(Cache(cacheDir, cacheSize))
            .build()
    }
    bind<AniList>() with singleton {
        AniListGraphQL()
    }
    bind<Json>() with singleton {
        Json {
            encodeDefaults = false
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    }
    bind<JDA>() with singleton {
        JDABuilder.createLight(Environment.BOT_TOKEN)
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS
            )
            .useCoroutines()
            .build()
            .handleGuildEvents()
            .handleFind()
            .handleLink()
            .handlePing()
            .handleAbout()
            .handleCharacter()
            .handleUser()
            .handleClear()
            .handleStaff()
            .handleSetting()
    }
}

val basuraExceptionHandler: suspend (SlashCommandEvent, Exception) -> Unit = { event, ex ->
    val log by Log4j2("ExceptionHandler")

    when (ex) {
        is SocketTimeoutException -> {
            event.sendLocalizedMessage(LocaleMessage.TimeoutError)
        }
        is ErrorResponseException -> {
            if (ex.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            }
            log.error("An error response was returned", ex)
        }
        else -> {
            defaultExceptionHandler(event, ex)
        }
    }
}