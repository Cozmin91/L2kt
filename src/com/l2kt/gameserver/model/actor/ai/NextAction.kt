package com.l2kt.gameserver.model.actor.ai

/**
 * Class for AI action after some event.
 * @author Yaroslav
 */
class NextAction
/**
 * Single constructor.
 * @param event : After which the NextAction is triggered.
 * @param intention : CtrlIntention of the action.
 * @param runnable :
 */
    (
    /** After which CtrlEvent is this action supposed to run.  */
    /**
     * @return the _event
     */
    val event: CtrlEvent,
    /** What is the intention of the action, e.g. if AI gets this CtrlIntention set, NextAction is canceled.  */
    /**
     * @return the _intention
     */
    val intention: CtrlIntention,
    /** Wrapper for NextAction content.  */
    private val _runnable: Runnable
) {

    /**
     * Do action.
     */
    fun run() {
        _runnable.run()
    }
}