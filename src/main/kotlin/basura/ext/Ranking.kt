package basura.ext

import basura.PAGINATOR_TIMEOUT
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaFormat
import basura.graphql.anilist.MediaSeason
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import respondingStandardPaginator

class Ranking : Extension() {
    override val name: String = "ranking"
    override val bundle: String = "ranking"

    private val aniList by inject<AniList>()
    private val db by inject<Database>()

    override suspend fun setup() {
        publicSlashCommand(::RankingArgs) {
            name = "ranking"
            description = "Shows the current ranking based on given parameters."
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false

                val mediaSeason = arguments.season?.let { MediaSeason.valueOf(it) }
                val mediaFormat = MediaFormat.valueOf(arguments.format)

                val media = aniList.findMediaByRanking(
                    amount = arguments.amount,
                    formatIn = listOf(mediaFormat),
                    season = mediaSeason,
                    seasonYear = arguments.year,
                    allowHentai
                )

                if (media == null) {
                    respond {
                        content = translate("ranking.error.noResultsFromCriteria")
                    }
                } else {
                    val mediaList = basura.lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = basura.aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        media.forEach {
                            page {
                                apply(basura.embed.createMediaEmbed(it, mediaList, aniToDiscordName))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }
    }

    inner class RankingArgs : Arguments() {
        val amount by int {
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