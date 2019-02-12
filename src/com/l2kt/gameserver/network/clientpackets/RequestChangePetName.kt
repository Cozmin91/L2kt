package com.l2kt.gameserver.network.clientpackets

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.network.SystemMessageId
import java.sql.SQLException

class RequestChangePetName : L2GameClientPacket() {

    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        // No active pet.
        if (!player.hasPet())
            return

        // Name length integrity check.
        if (_name.length < 2 || _name.length > 8) {
            player.sendPacket(SystemMessageId.NAMING_PETNAME_UP_TO_8CHARS)
            return
        }

        // Pet is already named.
        val pet = player.pet as Pet
        if (pet.name != null) {
            player.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET)
            return
        }

        // Invalid name pattern.
        if (!StringUtil.isValidString(_name, "^[A-Za-z0-9]{2,8}$")) {
            player.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS)
            return
        }

        // Name is a npc name.
        if (NpcData.getInstance().getTemplateByName(_name) != null)
            return

        // Name already exists on another pet.
        if (doesPetNameExist(_name)) {
            player.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET)
            return
        }

        pet.name = _name
        pet.sendPetInfosToOwner()
    }

    companion object {
        private const val SEARCH_NAME = "SELECT name FROM pets WHERE name=?"

        /**
         * @param name : The name to search.
         * @return true if such name already exists on database, false otherwise.
         */
        private fun doesPetNameExist(name: String): Boolean {
            var result = true

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(SEARCH_NAME).use { ps ->
                        ps.setString(1, name)

                        ps.executeQuery().use { rs -> result = rs.next() }
                    }
                }
            } catch (e: SQLException) {
                L2GameClientPacket.LOGGER.error("Couldn't check existing petname.", e)
            }

            return result
        }
    }
}