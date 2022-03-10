package basura

import basura.db.guilds
import basura.db.userLocales
import basura.ext.*
import basura.graphql.AniList
import basura.graphql.AniListGraphQL
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.util.*
import javax.sql.DataSource
import kotlin.time.DurationUnit
import kotlin.time.toDuration

val PAGINATOR_TIMEOUT = 60L

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        i18n {
            defaultLocale = Locale.ENGLISH
            localeResolver { guild, _, user ->
                val db by inject<Database>(Database::class.java)

                if (user != null) {
                    val userIdLong = user.id.value.toLong()
                    val localeStr = db.userLocales.firstOrNull { it.discordId eq userIdLong }?.locale
                    if (localeStr != null) {
                        return@localeResolver Locale.forLanguageTag(localeStr)
                    } else {
                        return@localeResolver Locale.getDefault()
                    }
                }

                if (guild != null) {
                    val guildIdLong = guild.id.value.toLong()
                    val localeStr = db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.locale

                    if (localeStr != null) {
                        return@localeResolver Locale.forLanguageTag(localeStr)
                    } else {
                        return@localeResolver Locale.getDefault()
                    }
                }

                return@localeResolver Locale.getDefault()
            }
        }

        extensions {
            add(::About)
            add(::Character)
            add(::Find)
            add(::Guild)
            add(::Language)
            add(::Link)
            add(::Ranking)
            add(::Setting)
            add(::Staff)
            add(::User)
        }

        presence {
            competing("Trash")
        }

        hooks {
            kordShutdownHook = true

            afterKoinSetup {
                loadModule {
                    single<DataSource>(createdAtStart = true) {
                        HikariDataSource(HikariConfig().apply {
                            maximumPoolSize = Runtime.getRuntime().availableProcessors() / 2
                            jdbcUrl = env("DB_URL")
                            username = env("DB_USER")
                            password = env("DB_PASS")
                        })
                    }
                    single {
                        Database.connect(
                            dataSource = get(),
                            dialect = PostgreSqlDialect()
                        )
                    }
                    single {
                        HttpClient(CIO)
                    }
                    single<AniList> {
                        AniListGraphQL()
                    }
                    single {
                        Json {
                            encodeDefaults = false
                            coerceInputValues = true
                            ignoreUnknownKeys = true
                        }
                    }
                }
            }

            created {
                val hikari by inject<DataSource>()

                Flyway.configure()
                    .dataSource(hikari)
                    .load()
                    .migrate()
            }
        }
    }

    bot.start()
}