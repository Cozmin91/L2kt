package com.l2kt.gameserver.model.actor.ai

/**
 * A datatype used as a simple "wish" of an actor, consisting of an [CtrlIntention] and to up to 2 [Object]s of any type.
 */
class Desire {
    var intention: CtrlIntention
        private set

    var firstParameter: Any? = null
        private set
    var secondParameter: Any? = null
        private set

    /**
     * @return true if the current [Desire] got blank parameters.
     */
    val isBlank: Boolean
        get() = intention === CtrlIntention.IDLE && firstParameter == null && secondParameter == null

    init {
        intention = CtrlIntention.IDLE
    }

    override fun toString(): String {
        return "Desire " + intention.toString() + ", with following parameters: " + firstParameter + " and " + secondParameter
    }

    /**
     * Update the current [Desire] with parameters.
     * @param intention : The new [CtrlIntention] to set.
     * @param firstParameter : The first argument to set.
     * @param secondParameter : The second argument to set.
     */
    @Synchronized
    fun update(intention: CtrlIntention, firstParameter: Any?, secondParameter: Any?) {
        this.intention = intention

        this.firstParameter = firstParameter
        this.secondParameter = secondParameter
    }

    /**
     * Update the current [Desire] with parameters taken from another Desire.
     * @param desire : The Desire to use as parameters.
     */
    @Synchronized
    fun update(desire: Desire) {
        intention = desire.intention

        firstParameter = desire.firstParameter
        secondParameter = desire.secondParameter
    }

    /**
     * Reset the current [Desire] parameters.
     */
    @Synchronized
    fun reset() {
        intention = CtrlIntention.IDLE

        firstParameter = null
        secondParameter = null
    }

    /**
     * @param intention : The intention to test.
     * @param param1 : The first Object to test.
     * @param param2 : The second Object to test.
     * @return true if all tested parameters are equal (intention and both parameters).
     */
    fun equals(intention: CtrlIntention, param1: Any, param2: Any): Boolean {
        return this.intention === intention && firstParameter === param1 && secondParameter === param2
    }
}