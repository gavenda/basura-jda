package basura.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface Guild : Entity<Guild> {
    companion object : Entity.Factory<Guild>()

    val id: Long
    var discordGuildId: Long
    var hentai: Boolean
    var locale: String
}

object Guilds : Table<Guild>("guild") {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val hentai = boolean("hentai").bindTo { it.hentai }
    val locale = varchar("locale").bindTo { it.locale }
}

val Database.guilds get() = this.sequenceOf(Guilds)