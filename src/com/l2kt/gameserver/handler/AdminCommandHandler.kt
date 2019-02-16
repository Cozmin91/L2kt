package com.l2kt.gameserver.handler

import com.l2kt.gameserver.handler.admincommandhandlers.*
import java.util.*

object AdminCommandHandler {
    private val _entries = HashMap<Int, IAdminCommandHandler>()

    init {
        registerHandler(AdminAdmin())
        registerHandler(AdminAnnouncements())
        registerHandler(AdminBan())
        registerHandler(AdminBookmark())
        registerHandler(AdminBuffs())
        registerHandler(AdminCamera())
        registerHandler(AdminCreateItem())
        registerHandler(AdminCursedWeapons())
        registerHandler(AdminDelete())
        registerHandler(AdminDoorControl())
        registerHandler(AdminEditChar())
        registerHandler(AdminEditNpc())
        registerHandler(AdminEffects())
        registerHandler(AdminEnchant())
        registerHandler(AdminExpSp())
        registerHandler(AdminGeoEngine())
        registerHandler(AdminGm())
        registerHandler(AdminGmChat())
        registerHandler(AdminHeal())
        registerHandler(AdminHelpPage())
        registerHandler(AdminInvul())
        registerHandler(AdminKick())
        registerHandler(AdminKnownlist())
        registerHandler(AdminLevel())
        registerHandler(AdminMaintenance())
        registerHandler(AdminMammon())
        registerHandler(AdminManor())
        registerHandler(AdminMenu())
        registerHandler(AdminMovieMaker())
        registerHandler(AdminOlympiad())
        registerHandler(AdminPetition())
        registerHandler(AdminPForge())
        registerHandler(AdminPledge())
        registerHandler(AdminPolymorph())
        registerHandler(AdminRes())
        registerHandler(AdminRideWyvern())
        registerHandler(AdminShop())
        registerHandler(AdminSiege())
        registerHandler(AdminSkill())
        registerHandler(AdminSpawn())
        registerHandler(AdminTarget())
        registerHandler(AdminTeleport())
        registerHandler(AdminZone())
    }

    private fun registerHandler(handler: IAdminCommandHandler) {
        for (id in handler.adminCommandList)
            _entries[id.hashCode()] = handler
    }

    fun getHandler(adminCommand: String): IAdminCommandHandler? {
        var command = adminCommand

        if (adminCommand.indexOf(" ") != -1)
            command = adminCommand.substring(0, adminCommand.indexOf(" "))

        return _entries[command.hashCode()]
    }

    fun size(): Int {
        return _entries.size
    }
}