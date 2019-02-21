package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*
import java.util.concurrent.ScheduledFuture

/**
 * @author Layane
 */
class CabalBuffer(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    private var _aiTask: ScheduledFuture<*>? = null
    protected var _step = 0 // Flag used to delay chat broadcast.

    init {

        _aiTask = ThreadPool.scheduleAtFixedRate(CabaleAI(this), 5000, 5000)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Calculate the distance between the Player and the L2Npc
            if (!canInteract(player)) {
                // Notify the Player AI with INTERACT
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            } else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)
            }
        }
    }

    /**
     * For each known player in range, cast either the positive or negative buff. <BR></BR>
     * The stats affected depend on the player type, either a fighter or a mystic. <BR></BR>
     * <BR></BR>
     * Curse of Destruction (Loser)<BR></BR>
     * - Fighters: -25% Accuracy, -25% Effect Resistance<BR></BR>
     * - Mystics: -25% Casting Speed, -25% Effect Resistance<BR></BR>
     * <BR></BR>
     * Blessing of Prophecy (Winner)<BR></BR>
     * - Fighters: +25% Max Load, +25% Effect Resistance<BR></BR>
     * - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<BR></BR>
     */
    private inner class CabaleAI(private val _caster: CabalBuffer) : Runnable {

        override fun run() {
            var isBuffAWinner = false
            var isBuffALoser = false

            val winningCabal = SevenSigns.cabalHighestScore

            // Defines which cabal is the loser.
            var losingCabal = CabalType.NORMAL
            if (winningCabal === CabalType.DAWN)
                losingCabal = CabalType.DUSK
            else if (winningCabal === CabalType.DUSK)
                losingCabal = CabalType.DAWN

            // Those lists store players for the shout.
            val playersList = ArrayList<Player>()
            val gmsList = ArrayList<Player>()

            for (player in getKnownTypeInRadius(Player::class.java, 900)) {
                if (player.isGM)
                    gmsList.add(player)
                else
                    playersList.add(player)

                val playerCabal = SevenSigns.getPlayerCabal(player.objectId)
                if (playerCabal === CabalType.NORMAL)
                    continue

                if (!isBuffAWinner && playerCabal === winningCabal && _caster.npcId == SevenSigns.ORATOR_NPC_ID) {
                    isBuffAWinner = true
                    handleCast(player, if (!player.isMageClass) 4364 else 4365)
                } else if (!isBuffALoser && playerCabal === losingCabal && _caster.npcId == SevenSigns.PREACHER_NPC_ID) {
                    isBuffALoser = true
                    handleCast(player, if (!player.isMageClass) 4361 else 4362)
                }

                // Buff / debuff only 1 ppl per round.
                if (isBuffAWinner && isBuffALoser)
                    break
            }

            // Autochat every 60sec. The actual AI cycle is 5sec, so delay it of 12 steps.
            if (_step >= 12) {
                if (!playersList.isEmpty() || !gmsList.isEmpty()) {
                    // Pickup a random message from string arrays.
                    var text: String
                    if (_caster.collisionHeight > 30)
                        text = Rnd[MESSAGES_LOSER] ?: ""
                    else
                        text = Rnd[MESSAGES_WINNER] ?: ""

                    if (text!!.indexOf("%player_cabal_winner%") > -1) {
                        for (nearbyPlayer in playersList) {
                            if (SevenSigns.getPlayerCabal(nearbyPlayer.objectId) === winningCabal) {
                                text = text!!.replace("%player_cabal_winner%", nearbyPlayer.name)
                                break
                            }
                        }
                    } else if (text.indexOf("%player_cabal_loser%") > -1) {
                        for (nearbyPlayer in playersList) {
                            if (SevenSigns.getPlayerCabal(nearbyPlayer.objectId) === losingCabal) {
                                text = text!!.replace("%player_cabal_loser%", nearbyPlayer.name)
                                break
                            }
                        }
                    }

                    if (!text.contains("%player_")) {
                        val cs = CreatureSay(objectId, Say2.ALL, name, text)

                        for (nearbyPlayer in playersList)
                            nearbyPlayer.sendPacket(cs)

                        for (nearbyGM in gmsList)
                            nearbyGM.sendPacket(cs)
                    }
                }
                _step = 0
            } else
                _step++
        }

        private fun handleCast(player: Player, skillId: Int) {
            val skillLevel = if (player.level > 40) 1 else 2

            val skill = SkillTable.getInfo(skillId, skillLevel)
            if (player.getFirstEffect(skill) == null) {
                skill!!.getEffects(_caster, player)
                broadcastPacket(MagicSkillUse(_caster, player, skill.id, skillLevel, skill.hitTime, 0))
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                        skillId
                    )
                )
            }
        }
    }

    override fun deleteMe() {
        if (_aiTask != null) {
            _aiTask!!.cancel(true)
            _aiTask = null
        }
        super.deleteMe()
    }

    companion object {
        protected val MESSAGES_LOSER = arrayOf(
            "%player_cabal_loser%! All is lost! Prepare to meet the goddess of death!",
            "%player_cabal_loser%! You bring an ill wind!",
            "%player_cabal_loser%! You might as well give up!",
            "A curse upon you!",
            "All is lost! Prepare to meet the goddess of death!",
            "All is lost! The prophecy of destruction has been fulfilled!",
            "The prophecy of doom has awoken!",
            "This world will soon be annihilated!"
        )

        protected val MESSAGES_WINNER = arrayOf(
            "%player_cabal_winner%! I bestow on you the authority of the abyss!",
            "%player_cabal_winner%, Darkness shall be banished forever!",
            "%player_cabal_winner%, the time for glory is at hand!",
            "All hail the eternal twilight!",
            "As foretold in the prophecy of darkness, the era of chaos has begun!",
            "The day of judgment is near!",
            "The prophecy of darkness has been fulfilled!",
            "The prophecy of darkness has come to pass!"
        )
    }
}