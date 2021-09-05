package basura

import basura.discord.useCoroutines
import basura.graphql.AniList
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.ktorm.database.Database
import javax.sql.DataSource


val kodein = DI {
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
        OkHttpClient()
    }
    bind<AniList>() with singleton {
        AniList()
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
            .useCoroutines()
            .build()
            .apply {
                addFindCommands()
                addLinkCommands()
                addPingCommand()
                addAboutCommand()
                addCharacterCommand()
                addUserCommand()
                addClearCommand()
            }
    }
}