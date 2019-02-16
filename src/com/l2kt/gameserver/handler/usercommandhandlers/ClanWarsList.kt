package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.sql.PreparedStatement

class ClanWarsList : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        val clan = activeChar.clan
        if (clan == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return false
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement: PreparedStatement

                // Attack List
                if (id == 88)
                    statement =
                            con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)")
                else if (id == 89)
                    statement =
                            con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)")
                else
                    statement =
                            con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)")// War List
                // Under Attack List

                statement.setInt(1, clan.clanId)
                statement.setInt(2, clan.clanId)

                val rset = statement.executeQuery()

                if (rset.first()) {
                    if (id == 88)
                        activeChar.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON)
                    else if (id == 89)
                        activeChar.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU)
                    else
                        activeChar.sendPacket(SystemMessageId.WAR_LIST)

                    var sm: SystemMessage
                    while (rset.next()) {
                        val clanName = rset.getString("clan_name")

                        if (rset.getInt("ally_id") > 0)
                            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(clanName)
                                .addString(rset.getString("ally_name"))
                        else
                            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(clanName)

                        activeChar.sendPacket(sm)
                    }

                    activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER)
                } else {
                    if (id == 88)
                        activeChar.sendPacket(SystemMessageId.YOU_ARENT_IN_CLAN_WARS)
                    else if (id == 89)
                        activeChar.sendPacket(SystemMessageId.NO_CLAN_WARS_VS_YOU)
                    else if (id == 90)
                        activeChar.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR)
                }

                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
        }

        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(88, 89, 90)
    }
}