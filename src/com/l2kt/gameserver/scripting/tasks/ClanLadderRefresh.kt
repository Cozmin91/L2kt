package com.l2kt.gameserver.scripting.tasks

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.scripting.ScheduledQuest

class ClanLadderRefresh : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        ClanTable.refreshClansLadder(true)
    }

    public override fun onEnd() {}
}