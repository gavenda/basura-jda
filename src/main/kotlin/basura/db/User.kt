package basura.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    val id: Long
    var discordId: Long
    var discordGuildId: Long
    var aniListId: Long
    var aniListUsername: String
}

object Users : Table<User>("user") {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordId = long("discord_id").bindTo { it.discordId }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val aniListId = long("anilist_id").bindTo { it.aniListId }
    val aniListUsername = varchar("anilist_username").bindTo { it.aniListUsername }
}

interface UserLocale : Entity<UserLocale> {
    companion object : Entity.Factory<UserLocale>()

    val id: Long
    var discordId: Long
    var locale: String
}

object UserLocales : Table<UserLocale>("user_locale") {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordId = long("discord_id").bindTo { it.discordId }
    val locale = varchar("locale").bindTo { it.locale }
}

val Database.users get() = this.sequenceOf(Users)
val Database.userLocales get() = this.sequenceOf(UserLocales)