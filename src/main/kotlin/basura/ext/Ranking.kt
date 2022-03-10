package basura.ext

import basura.AppDispatchers
import basura.action
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaFormat
import basura.graphql.anilist.MediaSeason
import basura.sendMediaResult
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

class Ranking : Extension() {
    override val name: String = "ranking"
    override val bundle: String = "ranking"

    private val aniList by inject<AniList>()
    private val db by inject<Database>()
    private val log = KotlinLogging.logger { }

    override suspend fun setup() {
        publicSlashCommand(::RankingArgs) {
            name = "ranking"
            description = "Shows the current ranking based on given parameters."
            action(AppDispatchers.IO) {
                log.info { "Looking up ranking $arguments with [ userId = ${user.id} ]" }
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false

                val mediaSeason = arguments.season?.let { MediaSeason.valueOf(it) }
                val mediaFormat = MediaFormat.valueOf(arguments.format)

                val media = aniList.findMediaByRanking(
                    amount = arguments.amount ?: 10,
                    formatIn = listOf(mediaFormat),
                    season = mediaSeason,
                    seasonYear = arguments.year,
                    allowHentai
                )

                if (media == null || media.isEmpty()) {
                    respond {
                        content = translate("ranking.error.noResultsFromCriteria")
                    }
                } else {
                    sendMediaResult(guild, media)
                }
            }
        }
    }

    inner class RankingArgs : Arguments() {
        val amount by optionalInt {
            name = "amount"
            description = "Number of media to show."
        }
        val season by optionalString {
            name = "season"
            description = "The media season."
        }
        val year by optionalInt {
            name = "year"
            description = "The media year."
        }
        val format by defaultingStringChoice {
            name = "format"
            description = "The media format."
            defaultValue = "TV"

            choice("Manga", "MANGA")
            choice("Movie", "MOVIE")
            choice("Music", "MUSIC")
            choice("Novel", "NOVEL")
            choice("ONA", "ONA")
            choice("Oneshot", "ONE_SHOT")
            choice("OVA", "OVA")
            choice("Special", "SPECIAL")
            choice("TV", "TV")
            choice("TV Short", "TV_SHORT")
        }
    }
}