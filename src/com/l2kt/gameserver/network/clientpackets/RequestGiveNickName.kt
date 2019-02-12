package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestGiveNickName : L2GameClientPacket() {
    private var _target: String = ""
    private var _title: String = ""

    override fun readImpl() {
        _target = readS()
        _title = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!StringUtil.isValidString(_title, "^[a-zA-Z0-9 !@#$&()\\-`.+,/\"]*{0,16}$")) {
            activeChar.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER)
            return
        }

        // Noblesse can bestow a title to themselves
        if (activeChar.isNoble && _target.matches(activeChar.name.toRegex())) {
            activeChar.title = _title
            activeChar.sendPacket(SystemMessageId.TITLE_CHANGED)
            activeChar.broadcastTitleInfo()
        } else {
            // Can the player change/give a title?
            if (activeChar.clanPrivileges and Clan.CP_CL_GIVE_TITLE != Clan.CP_CL_GIVE_TITLE) {
                activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            if (activeChar.clan.level < 3) {
                activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE)
                return
            }

            val member = activeChar.clan.getClanMember(_target)
            if (member != null) {
                val playerMember = member.playerInstance
                if (playerMember != null) {
                    playerMember.title = _title

                    playerMember.sendPacket(SystemMessageId.TITLE_CHANGED)
                    if (activeChar != playerMember)
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2).addCharName(
                                playerMember
                            ).addString(_title)
                        )

                    playerMember.broadcastTitleInfo()
                } else
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            } else
                activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN)
        }
    }
}