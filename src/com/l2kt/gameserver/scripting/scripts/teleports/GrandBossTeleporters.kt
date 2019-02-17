package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.data.xml.ScriptData
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.scripts.ai.individual.Antharas
import com.l2kt.gameserver.scripting.scripts.ai.individual.Baium
import com.l2kt.gameserver.scripting.scripts.ai.individual.Sailren
import com.l2kt.gameserver.scripting.scripts.ai.individual.Valakas

/**
 * This script leads behavior of multiple bosses teleporters.
 *
 *  * 13001, Heart of Warding : Teleport into Lair of Antharas
 *  * 29055, Teleportation Cubic : Teleport out of Baium zone
 *  * 31859, Teleportation Cubic : Teleport out of Lair of Antharas
 *  * 31384, Gatekeeper of Fire Dragon : Opening some doors
 *  * 31385, Heart of Volcano : Teleport into Lair of Valakas
 *  * 31540, Watcher of Valakas Klein : Teleport into Hall of Flames
 *  * 31686, Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
 *  * 31687, Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
 *  * 31759, Teleportation Cubic : Teleport out of Lair of Valakas
 *  * 31862, Angelic Vortex : Baium Teleport (3 different HTMs according of situation)
 *  * 32107, Teleportation Cubic : Teleport out of Sailren Nest
 *  * 32109, Shilen's Stone Statue : Teleport to Sailren Nest
 *
 * @author Plim, original python script by Emperorc
 */
class GrandBossTeleporters : Quest(-1, "teleports") {
    init {

        addFirstTalkId(29055, 31862)
        addStartNpc(13001, 29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862, 32107, 32109)
        addTalkId(13001, 29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862, 32107, 32109)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = ""
        var st = player?.getQuestState(qn)
        if (st == null)
            st = newQuestState(player!!)

        st.state = Quest.STATE_STARTED

        if (event.equals("baium", ignoreCase = true)) {
            // Player is mounted on a wyvern, cancel it.
            if (player!!.isFlying)
                htmltext = "31862-05.htm"
            else if (!st.hasQuestItems(4295))
                htmltext = "31862-03.htm"
            else {
                st.takeItems(4295, 1)

                // allow entry for the player for the next 30 secs.
                ZoneManager.getZoneById(110002, BossZone::class.java).allowPlayerEntry(player, 30)
                player.teleToLocation(BAIUM_IN, 0)
            }// All is ok, take the item and teleport the player inside.
            // Player hasn't blooded fabric, cancel it.
        } else if (event.equals("baium_story", ignoreCase = true))
            htmltext = "31862-02.htm"
        else if (event.equals("baium_exit", ignoreCase = true))
            player!!.teleToLocation(Rnd[BAIUM_OUT], 100)
        else if (event.equals("31540", ignoreCase = true)) {
            if (st.hasQuestItems(7267)) {
                st.takeItems(7267, 1)
                player!!.teleToLocation(183813, -115157, -3303, 0)
                st["allowEnter"] = "1"
            } else
                htmltext = "31540-06.htm"
        }
        return htmltext
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        var st = player.getQuestState(qn)
        if (st == null)
            st = newQuestState(player)

        st.state = Quest.STATE_STARTED

        when (npc.npcId) {
            29055 -> htmltext = "29055-01.htm"

            31862 -> {
                val status = GrandBossManager.getBossStatus(29020)
                if (status == Baium.AWAKE.toInt())
                    htmltext = "31862-01.htm"
                else if (status == Baium.DEAD.toInt())
                    htmltext = "31862-04.htm"
                else
                    htmltext = "31862-00.htm"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name) ?: return null

        st.state = Quest.STATE_STARTED

        val status: Int
        when (npc.npcId) {
            13001 -> {
                status = GrandBossManager.getBossStatus(Antharas.ANTHARAS)
                if (status == Antharas.FIGHTING.toInt())
                    htmltext = "13001-02.htm"
                else if (status == Antharas.DEAD.toInt())
                    htmltext = "13001-01.htm"
                else if (status == Antharas.DORMANT.toInt() || status == Antharas.WAITING.toInt()) {
                    if (st.hasQuestItems(3865)) {
                        st.takeItems(3865, 1)
                        ZoneManager.getZoneById(110001, BossZone::class.java).allowPlayerEntry(player, 30)

                        player.teleToLocation(175300 + Rnd[-350, 350], 115180 + Rnd[-1000, 1000], -7709, 0)

                        if (status == Antharas.DORMANT.toInt()) {
                            GrandBossManager.setBossStatus(Antharas.ANTHARAS, Antharas.WAITING.toInt())
                            ScriptData.getQuest("Antharas")!!.startQuestTimer(
                                "beginning",
                                Config.WAIT_TIME_ANTHARAS.toLong(),
                                null,
                                null,
                                false
                            )
                        }
                    } else
                        htmltext = "13001-03.htm"
                }
            }

            31859 -> player.teleToLocation(79800 + Rnd[600], 151200 + Rnd[1100], -3534, 0)

            31385 -> {
                status = GrandBossManager.getBossStatus(Valakas.VALAKAS)
                if (status == 0 || status == 1) {
                    if (_valakasPlayersCount >= 200)
                        htmltext = "31385-03.htm"
                    else if (st.getInt("allowEnter") == 1) {
                        st.unset("allowEnter")
                        ZoneManager.getZoneById(110010, BossZone::class.java).allowPlayerEntry(player, 30)

                        player.teleToLocation(204328, -111874, 70, 300)

                        _valakasPlayersCount++

                        if (status == Valakas.DORMANT.toInt()) {
                            GrandBossManager.setBossStatus(Valakas.VALAKAS, Valakas.WAITING.toInt())
                            ScriptData.getQuest("Valakas")!!.startQuestTimer(
                                "beginning",
                                Config.WAIT_TIME_VALAKAS.toLong(),
                                null,
                                null,
                                false
                            )
                        }
                    } else
                        htmltext = "31385-04.htm"
                } else if (status == 2)
                    htmltext = "31385-02.htm"
                else
                    htmltext = "31385-01.htm"
            }

            31384 -> DoorData.getDoor(24210004)!!.openMe()

            31686 -> DoorData.getDoor(24210006)!!.openMe()

            31687 -> DoorData.getDoor(24210005)!!.openMe()

            31540 -> if (_valakasPlayersCount < 50)
                htmltext = "31540-01.htm"
            else if (_valakasPlayersCount < 100)
                htmltext = "31540-02.htm"
            else if (_valakasPlayersCount < 150)
                htmltext = "31540-03.htm"
            else if (_valakasPlayersCount < 200)
                htmltext = "31540-04.htm"
            else
                htmltext = "31540-05.htm"

            31759 -> player.teleToLocation(150037, -57720, -2976, 250)

            32107 -> player.teleToLocation(Rnd[SAILREN_OUT], 100)

            32109 -> if (!player.isInParty)
                htmltext = "32109-03.htm"
            else if (!player.party!!.isLeader(player))
                htmltext = "32109-01.htm"
            else {
                if (st.hasQuestItems(8784)) {
                    status = GrandBossManager.getBossStatus(Sailren.SAILREN)
                    if (status == Sailren.DORMANT.toInt()) {
                        val party = player.party!!.members

                        // Check players conditions.
                        for (member in party) {
                            if (member.level < 70)
                                return "32109-06.htm"

                            if (!MathUtil.checkIfInRange(1000, player, member, true))
                                return "32109-07.htm"
                        }

                        // Take item from party leader.
                        st.takeItems(8784, 1)

                        val nest = ZoneManager.getZoneById(110011, BossZone::class.java)

                        // Teleport players.
                        for (member in party) {
                                nest.allowPlayerEntry(member, 30)
                                member.teleToLocation(SAILREN_IN, 100)
                        }
                        GrandBossManager.setBossStatus(Sailren.SAILREN, Sailren.FIGHTING.toInt())
                        ScriptData.getQuest("Sailren")!!.startQuestTimer("beginning", 60000, null, null, false)
                    } else if (status == Sailren.DEAD.toInt())
                        htmltext = "32109-04.htm"
                    else
                        htmltext = "32109-05.htm"
                } else
                    htmltext = "32109-02.htm"
            }
        }

        return htmltext
    }

    companion object {
        private const val qn = "GrandBossTeleporters"

        private val BAIUM_IN = Location(113100, 14500, 10077)
        private val BAIUM_OUT =
            arrayOf(Location(108784, 16000, -4928), Location(113824, 10448, -5164), Location(115488, 22096, -5168))

        private val SAILREN_IN = Location(27333, -6835, -1970)
        private val SAILREN_OUT =
            arrayOf(Location(10610, -24035, -3676), Location(10703, -24041, -3673), Location(10769, -24107, -3672))

        private var _valakasPlayersCount = 0
    }
}