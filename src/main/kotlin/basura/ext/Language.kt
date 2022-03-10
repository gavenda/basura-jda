package basura.ext

import basura.AppDispatchers
import basura.action
import basura.db.DbUserLocale
import basura.db.userLocales
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull

class Language : Extension() {
    override val name: String = "language"
    override val bundle: String = "language"

    private val db by inject<Database>()
    override suspend fun setup() {
        ephemeralSlashCommand(::LanguageArgs) {
            name = "language"
            description = "The language you want to show up (will take precedence over server setting)."
            action(AppDispatchers.IO) {
                val userLocale = db.userLocales.firstOrNull { it.discordId eq user.id.value.toLong() }

                if (userLocale != null) {
                    userLocale.locale = arguments.language
                    userLocale.flushChanges()
                } else {
                    db.userLocales.add(DbUserLocale {
                        discordId = user.id.value.toLong()
                        locale = arguments.language
                    })
                }

                respond {
                    content = translate("language.localeChanged")
                }
            }
        }
    }

    inner class LanguageArgs : Arguments() {
        val language by defaultingStringChoice {
            name = "language"
            description = "The language to use."
            defaultValue = "en-US"
            choice("English", "en-US")
        }
    }

}