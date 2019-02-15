package com.l2kt.gameserver.scripting.tasks

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.scripting.ScheduledQuest

class SevenSignsUpdate : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        if (!SevenSigns.getInstance().isSealValidationPeriod)
            SevenSignsFestival.getInstance().saveFestivalData(false)

        SevenSigns.getInstance().saveSevenSignsData()
        SevenSigns.getInstance().saveSevenSignsStatus()
    }

    public override fun onEnd() {}
}