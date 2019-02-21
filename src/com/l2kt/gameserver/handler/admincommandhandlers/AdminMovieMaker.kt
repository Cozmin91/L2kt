package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.manager.MovieMakerManager
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * @author KKnD
 */
class AdminMovieMaker : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_movie") {
            MovieMakerManager.mainHtm(activeChar)
        } else if (command.startsWith("admin_playseqq")) {
            try {
                MovieMakerManager.playSequence(Integer.parseInt(command.substring(15)), activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("You entered an invalid sequence id.")
                MovieMakerManager.mainHtm(activeChar)
                return false
            }

        } else if (command == "admin_addseq") {
            MovieMakerManager.addSequence(activeChar)
        } else if (command.startsWith("admin_delsequence")) {
            try {
                MovieMakerManager.deleteSequence(Integer.parseInt(command.substring(18)), activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("You entered an invalid sequence id.")
                MovieMakerManager.mainHtm(activeChar)
                return false
            }

        } else if (command.startsWith("admin_broadcast")) {
            try {
                MovieMakerManager.broadcastSequence(Integer.parseInt(command.substring(16)), activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("You entered an invalid sequence id.")
                MovieMakerManager.mainHtm(activeChar)
                return false
            }

        } else if (command == "admin_playmovie") {
            MovieMakerManager.playMovie(0, activeChar)
        } else if (command == "admin_broadmovie") {
            MovieMakerManager.playMovie(1, activeChar)
        } else if (command.startsWith("admin_editsequence")) {
            try {
                MovieMakerManager.editSequence(Integer.parseInt(command.substring(19)), activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("You entered an invalid sequence id.")
                MovieMakerManager.mainHtm(activeChar)
                return false
            }

        } else {
            val args = command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
            if (args.size < 10) {
                activeChar.sendMessage("Some arguments are missing.")
                return false
            }

            val targ = if (activeChar.target != null) activeChar.target.objectId else activeChar.objectId

            if (command.startsWith("admin_addsequence")) {
                MovieMakerManager.addSequence(
                    activeChar,
                    Integer.parseInt(args[1]),
                    targ,
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7]),
                    Integer.parseInt(args[8]),
                    Integer.parseInt(args[9])
                )
            } else if (command.startsWith("admin_playsequence")) {
                MovieMakerManager.playSequence(
                    activeChar,
                    targ,
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7]),
                    Integer.parseInt(args[8])
                )
            } else if (command.startsWith("admin_updatesequence")) {
                MovieMakerManager.updateSequence(
                    activeChar,
                    Integer.parseInt(args[1]),
                    targ,
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7]),
                    Integer.parseInt(args[8]),
                    Integer.parseInt(args[9])
                )
            }
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_addseq",
            "admin_playseqq",
            "admin_delsequence",
            "admin_editsequence",
            "admin_addsequence",
            "admin_playsequence",
            "admin_movie",
            "admin_updatesequence",
            "admin_broadcast",
            "admin_playmovie",
            "admin_broadmovie"
        )
    }
}