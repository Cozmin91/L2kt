package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Chest
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*

/**
 * This class handles following admin commands:
 *
 *  * hide = makes yourself invisible or visible.
 *  * earthquake = causes an earthquake of a given intensity and duration around you.
 *  * gmspeed = temporary Super Haste effect.
 *  * para/unpara = paralyze/remove paralysis from target.
 *  * para_all/unpara_all = same as para/unpara, affects the whole world.
 *  * polyself/unpolyself = makes you look as a specified mob.
 *  * social = forces an Creature instance to broadcast social action packets.
 *  * effect = forces an Creature instance to broadcast MSU packets.
 *  * abnormal = force changes over an Creature instance's abnormal state.
 *  * play_sound/jukebox = Music broadcasting related commands.
 *  * atmosphere = sky change related commands.
 *
 */
class AdminEffects : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        val st = StringTokenizer(command)
        st.nextToken()

        if (command.startsWith("admin_hide")) {
            if (!activeChar.appearance.invisible) {
                activeChar.appearance.setInvisible()
                activeChar.decayMe()
                activeChar.broadcastUserInfo()
                activeChar.spawnMe()
            } else {
                activeChar.appearance.setVisible()
                activeChar.broadcastUserInfo()
            }
        } else if (command.startsWith("admin_earthquake")) {
            try {
                activeChar.broadcastPacket(
                    Earthquake(
                        activeChar.x,
                        activeChar.y,
                        activeChar.z,
                        Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken())
                    )
                )
            } catch (e: Exception) {
                activeChar.sendMessage("Use: //earthquake <intensity> <duration>")
            }

        } else if (command.startsWith("admin_atmosphere")) {
            try {
                val type = st.nextToken()
                val state = st.nextToken()

                var packet: L2GameServerPacket? = null

                if (type == "ssqinfo") {
                    if (state == "dawn")
                        packet = SSQInfo.DAWN_SKY_PACKET
                    else if (state == "dusk")
                        packet = SSQInfo.DUSK_SKY_PACKET
                    else if (state == "red")
                        packet = SSQInfo.RED_SKY_PACKET
                    else if (state == "regular")
                        packet = SSQInfo.REGULAR_SKY_PACKET
                } else if (type == "sky") {
                    if (state == "night")
                        packet = SunSet.STATIC_PACKET
                    else if (state == "day")
                        packet = SunRise.STATIC_PACKET
                    else if (state == "red")
                        packet = ExRedSky(10)
                } else {
                    activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>")
                    activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>")
                }

                packet?.toAllOnlinePlayers()
            } catch (ex: Exception) {
                activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>")
                activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>")
            }

        } else if (command.startsWith("admin_jukebox")) {
            AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm")
        } else if (command.startsWith("admin_play_sound")) {
            try {
                val sound = command.substring(17)
                val snd = if (sound.contains(".")) PlaySound(sound) else PlaySound(1, sound)

                activeChar.broadcastPacket(snd)
                activeChar.sendMessage("Playing $sound.")
            } catch (e: StringIndexOutOfBoundsException) {
            }

        } else if (command.startsWith("admin_para_all")) {
            for (player in activeChar.getKnownType(Player::class.java)) {
                if (!player.isGM) {
                    player.startAbnormalEffect(0x0800)
                    player.isParalyzed = true
                    player.broadcastPacket(StopMove(player))
                }
            }
        } else if (command.startsWith("admin_unpara_all")) {
            for (player in activeChar.getKnownType(Player::class.java)) {
                player.stopAbnormalEffect(0x0800)
                player.isParalyzed = false
            }
        } else if (command.startsWith("admin_para")) {
            val target = activeChar.target
            if (target is Creature) {

                target.startAbnormalEffect(0x0800)
                target.isParalyzed = true
                target.broadcastPacket(StopMove(target))
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command.startsWith("admin_unpara")) {
            val target = activeChar.target
            if (target is Creature) {

                target.stopAbnormalEffect(0x0800)
                target.isParalyzed = false
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command.startsWith("admin_gmspeed")) {
            try {
                activeChar.stopSkillEffects(7029)

                val `val` = Integer.parseInt(st.nextToken())
                if (`val` > 0 && `val` < 5)
                    activeChar.doCast(SkillTable.getInfo(7029, `val`)!!)
            } catch (e: Exception) {
                activeChar.sendMessage("Use: //gmspeed value (0-4).")
            } finally {
                activeChar.updateEffectIcons()
            }
        } else if (command.startsWith("admin_social")) {
            try {
                val social = Integer.parseInt(st.nextToken())

                if (st.hasMoreTokens()) {
                    val targetOrRadius = st.nextToken()
                    if (targetOrRadius != null) {
                        val player = World.getPlayer(targetOrRadius)
                        if (player != null) {
                            if (performSocial(social, player))
                                activeChar.sendMessage(player.name + " was affected by your social request.")
                            else
                                activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED)
                        } else {
                            val radius = Integer.parseInt(targetOrRadius)

                            for (`object` in activeChar.getKnownTypeInRadius(Creature::class.java, radius))
                                performSocial(social, `object`)

                            activeChar.sendMessage(radius.toString() + " units radius was affected by your social request.")
                        }
                    }
                } else {
                    var obj: WorldObject? = activeChar.target
                    if (obj == null)
                        obj = activeChar

                    if (performSocial(social, obj))
                        activeChar.sendMessage(obj.name + " was affected by your social request.")
                    else
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED)
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]")
            }

        } else if (command.startsWith("admin_abnormal")) {
            try {
                val abnormal = Integer.decode("0x" + st.nextToken())!!

                if (st.hasMoreTokens()) {
                    val targetOrRadius = st.nextToken()
                    if (targetOrRadius != null) {
                        val player = World.getPlayer(targetOrRadius)
                        if (player != null) {
                            if (performAbnormal(abnormal, player))
                                activeChar.sendMessage(player.name + " was affected by your abnormal request.")
                            else
                                activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED)
                        } else {
                            val radius = Integer.parseInt(targetOrRadius)

                            for (`object` in activeChar.getKnownTypeInRadius(Creature::class.java, radius))
                                performAbnormal(abnormal, `object`)

                            activeChar.sendMessage(radius.toString() + " units radius was affected by your abnormal request.")
                        }
                    }
                } else {
                    var obj: WorldObject? = activeChar.target
                    if (obj == null)
                        obj = activeChar

                    if (performAbnormal(abnormal, obj))
                        activeChar.sendMessage(obj.name + " was affected by your abnormal request.")
                    else
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED)
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]")
            }

        } else if (command.startsWith("admin_effect")) {
            try {
                var obj: WorldObject? = activeChar.target
                var level = 1
                var hittime = 1
                val skill = Integer.parseInt(st.nextToken())

                if (st.hasMoreTokens())
                    level = Integer.parseInt(st.nextToken())
                if (st.hasMoreTokens())
                    hittime = Integer.parseInt(st.nextToken())

                if (obj == null)
                    obj = activeChar

                if (obj !is Creature)
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                else {
                    val target = obj as Creature?
                    target!!.broadcastPacket(MagicSkillUse(target, activeChar, skill, level, hittime, 0))
                    activeChar.sendMessage(obj.name + " performs MSU " + skill + "/" + level + " by your request.")
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //effect skill [level | level hittime]")
            }

        }

        if (command.contains("menu")) {
            var filename = "effects_menu.htm"
            if (command.contains("abnormal"))
                filename = "abnormal.htm"
            else if (command.contains("social"))
                filename = "social.htm"

            AdminHelpPage.showHelpPage(activeChar, filename)
        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_hide",
            "admin_earthquake",
            "admin_earthquake_menu",
            "admin_gmspeed",
            "admin_gmspeed_menu",
            "admin_unpara_all",
            "admin_para_all",
            "admin_unpara",
            "admin_para",
            "admin_unpara_all_menu",
            "admin_para_all_menu",
            "admin_unpara_menu",
            "admin_para_menu",
            "admin_social",
            "admin_social_menu",
            "admin_effect",
            "admin_effect_menu",
            "admin_abnormal",
            "admin_abnormal_menu",
            "admin_jukebox",
            "admin_play_sound",
            "admin_atmosphere",
            "admin_atmosphere_menu"
        )

        private fun performAbnormal(action: Int, target: WorldObject): Boolean {
            if (target is Creature) {
                if (target.abnormalEffect and action == action)
                    target.stopAbnormalEffect(action)
                else
                    target.startAbnormalEffect(action)

                return true
            }
            return false
        }

        private fun performSocial(action: Int, target: WorldObject): Boolean {
            if (target is Creature) {
                if (target is Summon || target is Chest || target is Npc && (action < 1 || action > 3) || target is Player && (action < 2 || action > 16))
                    return false

                target.broadcastPacket(SocialAction(target, action))
                return true
            }
            return false
        }
    }
}