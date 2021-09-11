package basura.command

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class CommandContext(
    val event: SlashCommandEvent,
    private val _guild: Guild? = null,
    private val _invoker: Member? = null,
) {
    val invoker: Member
        get() {
            return _invoker ?: throw IllegalStateException("Message is not in a guild!")
        }
    val guild: Guild
        get() {
            return _guild ?: throw IllegalStateException("Message is not in a guild!")
        }
    val selfMember: Member
        get() {
            return _guild?.selfMember ?: throw IllegalStateException("Message is not in a guild!")
        }
}

val SlashCommandEvent.context: CommandContext
    get() {
        return CommandContext(
            event = this,
            _guild = this.guild,
            _invoker = this.member,
        )
    }

