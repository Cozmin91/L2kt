package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.gameserver.data.xml.WalkerRouteData
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Walker
import com.l2kt.gameserver.model.location.WalkerLocation
import com.l2kt.gameserver.taskmanager.WalkerTaskManager

/**
 * This AI is used by [Walker]s.<br></br>
 * <br></br>
 * We use basic functionalities of AI, notably [CtrlIntention] MOVE_TO and onEvtArrived() to motion it.<br></br>
 * It retains a List of [WalkerLocation]s which, together, creates a route, and we save the current node index to find the next WalkerLocation. Once the path is complete, we return to index 0.<br></br>
 * <br></br>
 * It is associated to a global task named [WalkerTaskManager] to handle individual WalkerLocation delays.
 */
internal class WalkerAI(creature: Creature) : CreatureAI(creature) {
    private val _route: List<WalkerLocation> = WalkerRouteData.getWalkerRoute((super.actor as Walker).npcId)

    private var _index = 1

    init {

        if (!_route.isEmpty())
            setIntention(CtrlIntention.MOVE_TO, _route[_index])
    }

    override fun onEvtArrived() {
        // Retrieve current node.
        val node = _route[_index]

        if (node.chat != null)
            (super.actor as Walker).broadcastNpcSay(node.chat)

        // We freeze the NPC and store it on WalkerTaskManager, which will release it in the future.
        if (node.delay > 0)
            WalkerTaskManager.add((super.actor as Walker), node.delay)
        else
            moveToNextPoint()
    }

    /**
     * Move the [Walker] to the next [WalkerLocation] of his route.
     */
    fun moveToNextPoint() {
        // Set the next node value.
        if (_index < _route.size - 1)
            _index++
        else
            _index = 0

        // Retrieve next node.
        val node = _route[_index]

        // Running state.
        if (node.doesNpcMustRun())
            (super.actor as Walker).setRunning()
        else
            (super.actor as Walker).setWalking()

        setIntention(CtrlIntention.MOVE_TO, node)
    }
}