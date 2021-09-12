package basura

import basura.discord.useCoroutines
import basura.graphql.AniList
import basura.graphql.AniListGraphQL
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.ktorm.database.Database
import java.io.File
import java.util.*
import javax.sql.DataSource

/**
 * Main bot module.
 */
val bot = DI {
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
        JDABuilder.create(Environment.BOT_TOKEN, GatewayIntent.GUILD_MEMBERS)
            .useCoroutines()
            .useSharding(Environment.BOT_SHARD_ID, Environment.BOT_SHARD_TOTAL)
            .disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.VOICE_STATE,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS
            )
            // DND during startup
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setEnableShutdownHook(true)
            .build()
            .bindCommands()
            .bindGuildEvents()
    }
}

/**
 * The bot version.
 */
val version by lazy {
    Properties().apply {
        load(object {}.javaClass.getResourceAsStream("/version.properties"))
    }.getProperty("version") ?: "-"
}
