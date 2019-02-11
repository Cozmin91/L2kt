package com.l2kt.gameserver.network

import com.l2kt.Config

object FloodProtectors {
    enum class Action constructor(val reuseDelay: Int) {
        ROLL_DICE(Config.ROLL_DICE_TIME),
        HERO_VOICE(Config.HERO_VOICE_TIME),
        SUBCLASS(Config.SUBCLASS_TIME),
        DROP_ITEM(Config.DROP_ITEM_TIME),
        SERVER_BYPASS(Config.SERVER_BYPASS_TIME),
        MULTISELL(Config.MULTISELL_TIME),
        MANUFACTURE(Config.MANUFACTURE_TIME),
        MANOR(Config.MANOR_TIME),
        SENDMAIL(Config.SENDMAIL_TIME),
        CHARACTER_SELECT(Config.CHARACTER_SELECT_TIME),
        GLOBAL_CHAT(Config.GLOBAL_CHAT_TIME),
        TRADE_CHAT(Config.TRADE_CHAT_TIME),
        SOCIAL(Config.SOCIAL_TIME);


        companion object {

            val VALUES_LENGTH = Action.values().size
        }
    }

    /**
     * Try to perform an action according to client FPs value. A 0 reuse delay means the action is always possible.
     * @param client : The client to check protectors on.
     * @param action : The action to track.
     * @return True if the action is possible, False otherwise.
     */
    fun performAction(client: L2GameClient, action: Action): Boolean {
        val reuseDelay = action.reuseDelay
        if (reuseDelay == 0)
            return true

        val value = client.floodProtectors

        synchronized(value) {
            if (value[action.ordinal] > System.currentTimeMillis())
                return false

            value[action.ordinal] = System.currentTimeMillis() + reuseDelay
            return true
        }
    }
}