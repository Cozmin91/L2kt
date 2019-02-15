package com.l2kt.gameserver.scripting.tasks

import com.l2kt.gameserver.Shutdown
import com.l2kt.gameserver.scripting.ScheduledQuest

class ServerRestart : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        Shutdown(PERIOD, true).start()
    }

    public override fun onEnd() {}

    companion object {
        private const val PERIOD = 600 // 10 minutes
    }
}