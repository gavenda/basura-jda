package basura.command

import basura.Command
import basura.basuraExceptionHandler
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA

fun JDA.addStaffCommand() {
    onCommand(Command.STAFF, basuraExceptionHandler) {

    }
}