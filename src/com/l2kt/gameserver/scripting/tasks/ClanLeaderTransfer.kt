package com.l2kt.gameserver.scripting.tasks

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.scripting.ScheduledQuest

class ClanLeaderTransfer : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        for (clan in ClanTable.clans) {
            if (clan.newLeaderId <= 0)
                continue

            val member = clan.getClanMember(clan.newLeaderId)
            if (member == null) {
                clan.setNewLeaderId(0, true)
                continue
            }

            clan.setNewLeader(member)
        }
    }

    public override fun onEnd() {}
}