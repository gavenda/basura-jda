package basura

import basura.discord.interaction.choice
import basura.discord.interaction.command
import basura.discord.interaction.option
import basura.discord.interaction.updateCommands
import basura.graphql.anilist.MediaFormat
import basura.graphql.anilist.MediaSeason
import kotlinx.coroutines.DelicateCoroutinesApi
import net.dv8tion.jda.api.entities.Guild

object Command {
    const val RANKING = "ranking"
    const val ANIME = "anime"
    const val MANGA = "manga"
    const val FIND = "find"
    const val ABOUT = "about"
    const val PING = "ping"
    const val USER = "user"
    const val LINK = "link"
    const val CHARACTER = "character"
    const val UNLINK = "unlink"
}

@DelicateCoroutinesApi
fun Guild.updateBasuraCommands() = updateCommands {
    command(
        name = Command.ANIME,
        description = "Looks up the name of the anime."
    ) {
        option<String>(
            name = "query",
            description = "Name of the anime.",
            required = true
        )
    }
    command(
        name = Command.MANGA,
        description = "Looks up the name of the manga."
    ) {
        option<String>(
            name = "query",
            description = "Name of the manga.",
            required = true
        )
    }
    command(
        name = Command.FIND,
        description = "Looks up the name of the media."
    ) {
        option<String>(
            name = "query",
            description = "Name of the media.",
            required = true
        )
    }
    command(
        name = Command.CHARACTER,
        description = "Looks up the name of a character."
    ) {
        option<String>(
            name = "query",
            description = "Name of the character.",
            required = true
        )
    }
    command(
        name = Command.USER,
        description = "Looks up the statistics of a user's AniList."
    ) {
        option<String>(
            name = "username",
            description = "AniList username, defaults to your own if linked."
        )
    }
    command(
        name = Command.ABOUT,
        description = "Shows more information about this bot."
    )
    command(
        name = Command.PING,
        description = "Bot latency (in milliseconds) to Discord's servers."
    )
    command(
        name = Command.LINK,
        description = "Link your Discord account to your AniList account."
    ) {
        option<String>(
            name = "username",
            description = "Your AniList username.",
            required = true
        )
    }
    command(
        name = Command.UNLINK,
        description = "Unlink your AniList account from your Discord account."
    )
    command(
        name = Command.RANKING,
        description = "Shows the current ranking based on given parameters."
    ) {
        option<Int> (
            name = "amount",
            description = "Number of media to show.",
            required = true
        )
        option<String>(
            name = "season",
            description = "The media season."
        ) {
            choice(MediaSeason.WINTER.displayName, MediaSeason.WINTER.name)
            choice(MediaSeason.SPRING.displayName, MediaSeason.SPRING.name)
            choice(MediaSeason.SUMMER.displayName, MediaSeason.SUMMER.name)
            choice(MediaSeason.FALL.displayName, MediaSeason.FALL.name)
        }
        option<Int>(
            name = "year",
            description = "The media year."
        )
        option<String>(
            name = "format",
            description = "The media format."
        ) {
            choice(MediaFormat.TV.displayName, MediaFormat.TV.name)
            choice(MediaFormat.TV_SHORT.displayName, MediaFormat.TV_SHORT.name)
            choice(MediaFormat.OVA.displayName, MediaFormat.OVA.name)
            choice(MediaFormat.ONA.displayName, MediaFormat.ONA.name)
            choice(MediaFormat.SPECIAL.displayName, MediaFormat.SPECIAL.name)
            choice(MediaFormat.MOVIE.displayName, MediaFormat.MOVIE.name)
            choice(MediaFormat.MUSIC.displayName, MediaFormat.MUSIC.name)
            choice(MediaFormat.MANGA.displayName, MediaFormat.MANGA.name)
            choice(MediaFormat.ONE_SHOT.displayName, MediaFormat.ONE_SHOT.name)
            choice(MediaFormat.NOVEL.displayName, MediaFormat.NOVEL.name)
        }
    }
}