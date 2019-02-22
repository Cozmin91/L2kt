package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Gordon behavior. This boss attacks cursed weapons holders at sight.<br></br>
 * When he isn't attacking, he follows a pre-established path around Goddard castle.
 */
class Gordon : L2AttackableAIScript("ai/individual") {
    init {

        val npc = findSpawn(GORDON)
        if (npc != null)
            startQuestTimer("ai_loop", 1000, npc, null, true)
    }

    override fun registerNpcs() {
        addEventIds(GORDON, EventType.ON_KILL, EventType.ON_SPAWN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("ai_loop", ignoreCase = true)) {
            // Doesn't bother about task AI if the NPC is already fighting.
            if (npc?.ai?.desire?.intention == CtrlIntention.ATTACK || npc?.ai?.desire?.intention == CtrlIntention.CAST)
                return null

            // Check if player have Cursed Weapon and is in radius.
            for (pc in npc?.getKnownTypeInRadius(Player::class.java, 5000) ?: emptyList()) {
                if (pc.isCursedWeaponEquipped) {
                    attack(npc as Attackable, pc)
                    return null
                }
            }

            // Test the NPC position and move on new position if current position is reached.
            val currentNode = LOCS[_currentNode]
            if (npc != null) {
                if (npc.isInsideRadius(currentNode.x, currentNode.y, 100, false)) {
                    // Update current node ; if the whole route is done, come back to point 0.
                    _currentNode++
                    if (_currentNode >= LOCS.size)
                        _currentNode = 0

                    npc.setWalking()
                    npc.ai.setIntention(CtrlIntention.MOVE_TO, LOCS[_currentNode])
                } else if (!npc.isMoving) {
                    npc.setWalking()
                    npc.ai.setIntention(CtrlIntention.MOVE_TO, LOCS[_currentNode])
                }
            }
        }
        return super.onAdvEvent(event, npc, player)
    }

    override fun onSpawn(npc: Npc): String? {
        // Initialize current node.
        _currentNode = 0

        // Launch the AI loop.
        startQuestTimer("ai_loop", 1000, npc, null, true)

        return super.onSpawn(npc)
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        cancelQuestTimer("ai_loop", npc, null)

        return super.onKill(npc, killer)
    }

    companion object {
        private const val GORDON = 29095

        private val LOCS = arrayOf(
            Location(141569, -45908, -2387),
            Location(142494, -45456, -2397),
            Location(142922, -44561, -2395),
            Location(143672, -44130, -2398),
            Location(144557, -43378, -2325),
            Location(145839, -43267, -2301),
            Location(147044, -43601, -2307),
            Location(148140, -43206, -2303),
            Location(148815, -43434, -2328),
            Location(149862, -44151, -2558),
            Location(151037, -44197, -2708),
            Location(152555, -42756, -2836),
            Location(154808, -39546, -3236),
            Location(155333, -39962, -3272),
            Location(156531, -41240, -3470),
            Location(156863, -43232, -3707),
            Location(156783, -44198, -3764),
            Location(158169, -45163, -3541),
            Location(158952, -45479, -3473),
            Location(160039, -46514, -3634),
            Location(160244, -47429, -3656),
            Location(159155, -48109, -3665),
            Location(159558, -51027, -3523),
            Location(159396, -53362, -3244),
            Location(160872, -56556, -2789),
            Location(160857, -59072, -2613),
            Location(160410, -59888, -2647),
            Location(158770, -60173, -2673),
            Location(156368, -59557, -2638),
            Location(155188, -59868, -2642),
            Location(154118, -60591, -2731),
            Location(153571, -61567, -2821),
            Location(153457, -62819, -2886),
            Location(152939, -63778, -3003),
            Location(151816, -64209, -3120),
            Location(147655, -64826, -3433),
            Location(145422, -64576, -3369),
            Location(144097, -64320, -3404),
            Location(140780, -61618, -3096),
            Location(139688, -61450, -3062),
            Location(138267, -61743, -3056),
            Location(138613, -58491, -3465),
            Location(138139, -57252, -3517),
            Location(139555, -56044, -3310),
            Location(139107, -54537, -3240),
            Location(139279, -53781, -3091),
            Location(139810, -52687, -2866),
            Location(139657, -52041, -2793),
            Location(139215, -51355, -2698),
            Location(139334, -50514, -2594),
            Location(139817, -49715, -2449),
            Location(139824, -48976, -2263),
            Location(140130, -47578, -2213),
            Location(140483, -46339, -2382),
            Location(141569, -45908, -2387)
        )

        // The current Location node index.
        private var _currentNode: Int = 0

        private fun findSpawn(npcId: Int): Npc? {
            for (spawn in SpawnTable.spawnTable) {
                if (spawn.npcId == npcId)
                    return spawn.npc
            }
            return null
        }
    }
}