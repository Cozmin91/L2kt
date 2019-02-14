package com.l2kt.gameserver.model.pledge

data class ClanInfo(val clan: Clan) {
    val total: Int = clan.membersCount
    val online: Int = clan.onlineMembersCount
}