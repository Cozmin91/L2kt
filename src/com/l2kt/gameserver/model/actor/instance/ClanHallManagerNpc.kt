package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.TeleportLocationData
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.text.SimpleDateFormat
import java.util.*

class ClanHallManagerNpc(objectId: Int, template: NpcTemplate) : Merchant(objectId, template) {

    private var _clanHallId = -1

    /**
     * @return the L2ClanHall this L2Npc belongs to.
     */
    val clanHall: ClanHall?
        get() {
            if (_clanHallId < 0) {
                val temp = ClanHallManager.getNearbyClanHall(x, y, 500)

                if (temp != null)
                    _clanHallId = temp.id

                if (_clanHallId < 0)
                    return null
            }
            return ClanHallManager.getClanHallById(_clanHallId)
        }

    override fun isWarehouse(): Boolean {
        return true
    }

    override fun onBypassFeedback(player: Player, command: String) {
        val condition = validateCondition(player)
        if (condition <= COND_ALL_FALSE)
            return

        if (condition == COND_OWNER) {
            val st = StringTokenizer(command, " ")
            val actualCommand = st.nextToken()

            var `val` = if (st.hasMoreTokens()) st.nextToken() else ""

            if (actualCommand.equals("banish_foreigner", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)

                if (player.clanPrivileges and Clan.CP_CH_DISMISS == Clan.CP_CH_DISMISS) {
                    if (`val`.equals("list", ignoreCase = true))
                        html.setFile("data/html/clanHallManager/banish-list.htm")
                    else if (`val`.equals("banish", ignoreCase = true)) {
                        clanHall!!.banishForeigners()
                        html.setFile("data/html/clanHallManager/banish.htm")
                    }
                } else
                    html.setFile("data/html/clanHallManager/not_authorized.htm")

                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            } else if (actualCommand.equals("manage_vault", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)

                if (player.clanPrivileges and Clan.CP_CL_VIEW_WAREHOUSE == Clan.CP_CL_VIEW_WAREHOUSE) {
                    html.setFile("data/html/clanHallManager/vault.htm")
                    html.replace("%rent%", clanHall!!.lease)
                    html.replace("%date%", SimpleDateFormat("dd-MM-yyyy HH:mm").format(clanHall!!.paidUntil))
                } else
                    html.setFile("data/html/clanHallManager/not_authorized.htm")

                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            } else if (actualCommand.equals("door", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)

                if (player.clanPrivileges and Clan.CP_CH_OPEN_DOOR == Clan.CP_CH_OPEN_DOOR) {
                    if (`val`.equals("open", ignoreCase = true)) {
                        clanHall!!.openCloseDoors(true)
                        html.setFile("data/html/clanHallManager/door-open.htm")
                    } else if (`val`.equals("close", ignoreCase = true)) {
                        clanHall!!.openCloseDoors(false)
                        html.setFile("data/html/clanHallManager/door-close.htm")
                    } else
                        html.setFile("data/html/clanHallManager/door.htm")
                } else
                    html.setFile("data/html/clanHallManager/not_authorized.htm")

                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            } else if (actualCommand.equals("functions", ignoreCase = true)) {
                if (`val`.equals("tele", ignoreCase = true)) {
                    val html = NpcHtmlMessage(objectId)

                    val chf = clanHall!!.getFunction(ClanHall.FUNC_TELEPORT)
                    if (chf == null)
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm")
                    else
                        html.setFile("data/html/clanHallManager/tele" + clanHall!!.location + chf.lvl + ".htm")

                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                } else if (`val`.equals("item_creation", ignoreCase = true)) {
                    if (!st.hasMoreTokens())
                        return

                    val chf = clanHall!!.getFunction(ClanHall.FUNC_ITEM_CREATE)
                    if (chf == null) {
                        val html = NpcHtmlMessage(objectId)
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm")
                        html.replace("%objectId%", objectId)
                        player.sendPacket(html)
                        return
                    }

                    showBuyWindow(player, Integer.parseInt(st.nextToken()) + chf.lvl * 100000)
                } else if (`val`.equals("support", ignoreCase = true)) {
                    val html = NpcHtmlMessage(objectId)

                    val chf = clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)
                    if (chf == null)
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm")
                    else {
                        html.setFile("data/html/clanHallManager/support" + chf.lvl + ".htm")
                        html.replace("%mp%", currentMp.toInt())
                    }
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                } else if (`val`.equals("back", ignoreCase = true))
                    showChatWindow(player)
                else {
                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/clanHallManager/functions.htm")

                    val chfExp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_EXP)
                    if (chfExp != null)
                        html.replace("%xp_regen%", chfExp.lvl)
                    else
                        html.replace("%xp_regen%", "0")

                    val chfHp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_HP)
                    if (chfHp != null)
                        html.replace("%hp_regen%", chfHp.lvl)
                    else
                        html.replace("%hp_regen%", "0")

                    val chfMp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_MP)
                    if (chfMp != null)
                        html.replace("%mp_regen%", chfMp.lvl)
                    else
                        html.replace("%mp_regen%", "0")

                    html.replace("%npcId%", npcId)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }
            } else if (actualCommand.equals("manage", ignoreCase = true)) {
                if (player.clanPrivileges and Clan.CP_CH_SET_FUNCTIONS == Clan.CP_CH_SET_FUNCTIONS) {
                    if (`val`.equals("recovery", ignoreCase = true)) {
                        if (st.hasMoreTokens()) {
                            if (clanHall!!.ownerId == 0)
                                return

                            `val` = st.nextToken()

                            if (`val`.equals("hp_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "recovery hp 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("mp_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "recovery mp 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("exp_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "recovery exp 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_hp", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Fireplace (HP Recovery Device)")

                                val percent = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (percent) {
                                    20 -> cost = Config.CH_HPREG1_FEE

                                    40 -> cost = Config.CH_HPREG2_FEE

                                    80 -> cost = Config.CH_HPREG3_FEE

                                    100 -> cost = Config.CH_HPREG4_FEE

                                    120 -> cost = Config.CH_HPREG5_FEE

                                    140 -> cost = Config.CH_HPREG6_FEE

                                    160 -> cost = Config.CH_HPREG7_FEE

                                    180 -> cost = Config.CH_HPREG8_FEE

                                    200 -> cost = Config.CH_HPREG9_FEE

                                    220 -> cost = Config.CH_HPREG10_FEE

                                    240 -> cost = Config.CH_HPREG11_FEE

                                    260 -> cost = Config.CH_HPREG12_FEE

                                    else -> cost = Config.CH_HPREG13_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_HPREG_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace(
                                    "%use%",
                                    "Provides additional HP recovery for clan members in the clan hall.<font color=\"00FFFF\">$percent%</font>"
                                )
                                html.replace("%apply%", "recovery hp $percent")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_mp", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Carpet (MP Recovery)")

                                val percent = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (percent) {
                                    5 -> cost = Config.CH_MPREG1_FEE

                                    10 -> cost = Config.CH_MPREG2_FEE

                                    15 -> cost = Config.CH_MPREG3_FEE

                                    30 -> cost = Config.CH_MPREG4_FEE

                                    else -> cost = Config.CH_MPREG5_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_MPREG_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace(
                                    "%use%",
                                    "Provides additional MP recovery for clan members in the clan hall.<font color=\"00FFFF\">$percent%</font>"
                                )
                                html.replace("%apply%", "recovery mp $percent")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_exp", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Chandelier (EXP Recovery Device)")

                                val percent = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (percent) {
                                    5 -> cost = Config.CH_EXPREG1_FEE

                                    10 -> cost = Config.CH_EXPREG2_FEE

                                    15 -> cost = Config.CH_EXPREG3_FEE

                                    25 -> cost = Config.CH_EXPREG4_FEE

                                    35 -> cost = Config.CH_EXPREG5_FEE

                                    40 -> cost = Config.CH_EXPREG6_FEE

                                    else -> cost = Config.CH_EXPREG7_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_EXPREG_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace(
                                    "%use%",
                                    "Restores the Exp of any clan member who is resurrected in the clan hall.<font color=\"00FFFF\">$percent%</font>"
                                )
                                html.replace("%apply%", "recovery exp $percent")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("hp", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val percent = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_HP)
                                if (chf != null && chf.lvl == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "$`val`%")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (percent) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    20 -> fee = Config.CH_HPREG1_FEE

                                    40 -> fee = Config.CH_HPREG2_FEE

                                    80 -> fee = Config.CH_HPREG3_FEE

                                    100 -> fee = Config.CH_HPREG4_FEE

                                    120 -> fee = Config.CH_HPREG5_FEE

                                    140 -> fee = Config.CH_HPREG6_FEE

                                    160 -> fee = Config.CH_HPREG7_FEE

                                    180 -> fee = Config.CH_HPREG8_FEE

                                    200 -> fee = Config.CH_HPREG9_FEE

                                    220 -> fee = Config.CH_HPREG10_FEE

                                    240 -> fee = Config.CH_HPREG11_FEE

                                    260 -> fee = Config.CH_HPREG12_FEE

                                    else -> fee = Config.CH_HPREG13_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_RESTORE_HP,
                                        percent,
                                        fee,
                                        Config.CH_HPREG_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("mp", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val percent = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_MP)
                                if (chf != null && chf.lvl == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "$`val`%")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (percent) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    5 -> fee = Config.CH_MPREG1_FEE

                                    10 -> fee = Config.CH_MPREG2_FEE

                                    15 -> fee = Config.CH_MPREG3_FEE

                                    30 -> fee = Config.CH_MPREG4_FEE

                                    else -> fee = Config.CH_MPREG5_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_RESTORE_MP,
                                        percent,
                                        fee,
                                        Config.CH_MPREG_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("exp", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val percent = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_EXP)
                                if (chf != null && chf.lvl == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "$`val`%")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (percent) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    5 -> fee = Config.CH_EXPREG1_FEE

                                    10 -> fee = Config.CH_EXPREG2_FEE

                                    15 -> fee = Config.CH_EXPREG3_FEE

                                    25 -> fee = Config.CH_EXPREG4_FEE

                                    35 -> fee = Config.CH_EXPREG5_FEE

                                    40 -> fee = Config.CH_EXPREG6_FEE

                                    else -> fee = Config.CH_EXPREG7_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_RESTORE_EXP,
                                        percent,
                                        fee,
                                        Config.CH_EXPREG_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            }
                        } else {
                            val html = NpcHtmlMessage(objectId)
                            html.setFile("data/html/clanHallManager/edit_recovery.htm")

                            val grade = clanHall!!.grade

                            // Restore HP function.
                            val chfHp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_HP)
                            if (chfHp != null) {
                                html.replace(
                                    "%hp_recovery%",
                                    chfHp.lvl.toString() + "%</font> (<font color=\"FFAABB\">" + chfHp.lease + "</font> adenas / " + Config.CH_HPREG_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%hp_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfHp.endTime)
                                )

                                when (grade) {
                                    0 -> html.replace(
                                        "%change_hp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]$hp_grade0"
                                    )

                                    1 -> html.replace(
                                        "%change_hp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]$hp_grade1"
                                    )

                                    2 -> html.replace(
                                        "%change_hp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]$hp_grade2"
                                    )

                                    3 -> html.replace(
                                        "%change_hp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]$hp_grade3"
                                    )
                                }
                            } else {
                                html.replace("%hp_recovery%", "none")
                                html.replace("%hp_period%", "none")

                                when (grade) {
                                    0 -> html.replace("%change_hp%", hp_grade0)

                                    1 -> html.replace("%change_hp%", hp_grade1)

                                    2 -> html.replace("%change_hp%", hp_grade2)

                                    3 -> html.replace("%change_hp%", hp_grade3)
                                }
                            }

                            // Restore exp function.
                            val chfExp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_EXP)
                            if (chfExp != null) {
                                html.replace(
                                    "%exp_recovery%",
                                    chfExp.lvl.toString() + "%</font> (<font color=\"FFAABB\">" + chfExp.lease + "</font> adenas / " + Config.CH_EXPREG_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%exp_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfExp.endTime)
                                )

                                when (grade) {
                                    0 -> html.replace(
                                        "%change_exp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]$exp_grade0"
                                    )

                                    1 -> html.replace(
                                        "%change_exp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]$exp_grade1"
                                    )

                                    2 -> html.replace(
                                        "%change_exp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]$exp_grade2"
                                    )

                                    3 -> html.replace(
                                        "%change_exp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]$exp_grade3"
                                    )
                                }
                            } else {
                                html.replace("%exp_recovery%", "none")
                                html.replace("%exp_period%", "none")

                                when (grade) {
                                    0 -> html.replace("%change_exp%", exp_grade0)

                                    1 -> html.replace("%change_exp%", exp_grade1)

                                    2 -> html.replace("%change_exp%", exp_grade2)

                                    3 -> html.replace("%change_exp%", exp_grade3)
                                }
                            }

                            // Restore MP function.
                            val chfMp = clanHall!!.getFunction(ClanHall.FUNC_RESTORE_MP)
                            if (chfMp != null) {
                                html.replace(
                                    "%mp_recovery%",
                                    chfMp.lvl.toString() + "%</font> (<font color=\"FFAABB\">" + chfMp.lease + "</font> adenas / " + Config.CH_MPREG_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%mp_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfMp.endTime)
                                )

                                when (grade) {
                                    0 -> html.replace(
                                        "%change_mp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]$mp_grade0"
                                    )

                                    1 -> html.replace(
                                        "%change_mp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]$mp_grade1"
                                    )

                                    2 -> html.replace(
                                        "%change_mp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]$mp_grade2"
                                    )

                                    3 -> html.replace(
                                        "%change_mp%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]$mp_grade3"
                                    )
                                }
                            } else {
                                html.replace("%mp_recovery%", "none")
                                html.replace("%mp_period%", "none")

                                when (grade) {
                                    0 -> html.replace("%change_mp%", mp_grade0)

                                    1 -> html.replace("%change_mp%", mp_grade1)

                                    2 -> html.replace("%change_mp%", mp_grade2)

                                    3 -> html.replace("%change_mp%", mp_grade3)
                                }
                            }
                            html.replace("%objectId%", objectId)
                            player.sendPacket(html)
                        }
                    } else if (`val`.equals("other", ignoreCase = true)) {
                        if (st.hasMoreTokens()) {
                            if (clanHall!!.ownerId == 0)
                                return

                            `val` = st.nextToken()

                            if (`val`.equals("item_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "other item 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("tele_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "other tele 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("support_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "other support 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_item", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Magic Equipment (Item Production Facilities)")

                                val stage = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (stage) {
                                    1 -> cost = Config.CH_ITEM1_FEE

                                    2 -> cost = Config.CH_ITEM2_FEE

                                    else -> cost = Config.CH_ITEM3_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_ITEM_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace("%use%", "Allow the purchase of special items at fixed intervals.")
                                html.replace("%apply%", "other item $stage")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_support", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Insignia (Supplementary Magic)")

                                val stage = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (stage) {
                                    1 -> cost = Config.CH_SUPPORT1_FEE

                                    2 -> cost = Config.CH_SUPPORT2_FEE

                                    3 -> cost = Config.CH_SUPPORT3_FEE

                                    4 -> cost = Config.CH_SUPPORT4_FEE

                                    5 -> cost = Config.CH_SUPPORT5_FEE

                                    6 -> cost = Config.CH_SUPPORT6_FEE

                                    7 -> cost = Config.CH_SUPPORT7_FEE

                                    else -> cost = Config.CH_SUPPORT8_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_SUPPORT_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace("%use%", "Enables the use of supplementary magic.")
                                html.replace("%apply%", "other support $stage")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_tele", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Mirror (Teleportation Device)")

                                val stage = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (stage) {
                                    1 -> cost = Config.CH_TELE1_FEE

                                    else -> cost = Config.CH_TELE2_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_TELE_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace(
                                    "%use%",
                                    "Teleports clan members in a clan hall to the target <font color=\"00FFFF\">Stage $stage</font> staging area"
                                )
                                html.replace("%apply%", "other tele $stage")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("item", ignoreCase = true)) {
                                if (clanHall!!.ownerId == 0)
                                    return

                                `val` = st.nextToken()
                                val lvl = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_ITEM_CREATE)
                                if (chf != null && chf.lvl == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "Stage $`val`")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (lvl) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    1 -> fee = Config.CH_ITEM1_FEE

                                    2 -> fee = Config.CH_ITEM2_FEE

                                    else -> fee = Config.CH_ITEM3_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_ITEM_CREATE,
                                        lvl,
                                        fee,
                                        Config.CH_ITEM_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("tele", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val lvl = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_TELEPORT)
                                if (chf != null && chf.lvl == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "Stage $`val`")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (lvl) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    1 -> fee = Config.CH_TELE1_FEE

                                    else -> fee = Config.CH_TELE2_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_TELEPORT,
                                        lvl,
                                        fee,
                                        Config.CH_TELE_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("support", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val lvl = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)
                                if (chf != null && chf.lvl == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "Stage $`val`")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (lvl) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    1 -> fee = Config.CH_SUPPORT1_FEE

                                    2 -> fee = Config.CH_SUPPORT2_FEE

                                    3 -> fee = Config.CH_SUPPORT3_FEE

                                    4 -> fee = Config.CH_SUPPORT4_FEE

                                    5 -> fee = Config.CH_SUPPORT5_FEE

                                    6 -> fee = Config.CH_SUPPORT6_FEE

                                    7 -> fee = Config.CH_SUPPORT7_FEE

                                    else -> fee = Config.CH_SUPPORT8_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_SUPPORT,
                                        lvl,
                                        fee,
                                        Config.CH_SUPPORT_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            }
                        } else {
                            val html = NpcHtmlMessage(objectId)
                            html.setFile("data/html/clanHallManager/edit_other.htm")

                            val chfTel = clanHall!!.getFunction(ClanHall.FUNC_TELEPORT)
                            if (chfTel != null) {
                                html.replace(
                                    "%tele%",
                                    "- Stage " + chfTel.lvl + "</font> (<font color=\"FFAABB\">" + chfTel.lease + "</font> adenas / " + Config.CH_TELE_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%tele_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfTel.endTime)
                                )
                                html.replace(
                                    "%change_tele%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Remove</a>]$tele"
                                )
                            } else {
                                html.replace("%tele%", "none")
                                html.replace("%tele_period%", "none")
                                html.replace("%change_tele%", tele)
                            }

                            val grade = clanHall!!.grade

                            val chfSup = clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)
                            if (chfSup != null) {
                                html.replace(
                                    "%support%",
                                    "- Stage " + chfSup.lvl + "</font> (<font color=\"FFAABB\">" + chfSup.lease + "</font> adenas / " + Config.CH_SUPPORT_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%support_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfSup.endTime)
                                )

                                when (grade) {
                                    0 -> html.replace(
                                        "%change_support%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]$support_grade0"
                                    )

                                    1 -> html.replace(
                                        "%change_support%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]$support_grade1"
                                    )

                                    2 -> html.replace(
                                        "%change_support%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]$support_grade2"
                                    )

                                    3 -> html.replace(
                                        "%change_support%",
                                        "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]$support_grade3"
                                    )
                                }
                            } else {
                                html.replace("%support%", "none")
                                html.replace("%support_period%", "none")

                                when (grade) {
                                    0 -> html.replace("%change_support%", support_grade0)

                                    1 -> html.replace("%change_support%", support_grade1)

                                    2 -> html.replace("%change_support%", support_grade2)

                                    3 -> html.replace("%change_support%", support_grade3)
                                }
                            }

                            val chfCreate = clanHall!!.getFunction(ClanHall.FUNC_ITEM_CREATE)
                            if (chfCreate != null) {
                                html.replace(
                                    "%item%",
                                    "- Stage " + chfCreate.lvl + "</font> (<font color=\"FFAABB\">" + chfCreate.lease + "</font> adenas / " + Config.CH_ITEM_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%item_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfCreate.endTime)
                                )
                                html.replace(
                                    "%change_item%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Remove</a>]$item"
                                )
                            } else {
                                html.replace("%item%", "none")
                                html.replace("%item_period%", "none")
                                html.replace("%change_item%", item)
                            }
                            html.replace("%objectId%", objectId)
                            player.sendPacket(html)
                        }
                    } else if (`val`.equals("deco", ignoreCase = true)) {
                        if (st.hasMoreTokens()) {
                            if (clanHall!!.ownerId == 0)
                                return

                            `val` = st.nextToken()
                            if (`val`.equals("curtains_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "deco curtains 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("fixtures_cancel", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-cancel.htm")
                                html.replace("%apply%", "deco fixtures 0")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_curtains", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Curtains (Decoration)")

                                val stage = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (stage) {
                                    1 -> cost = Config.CH_CURTAIN1_FEE

                                    else -> cost = Config.CH_CURTAIN2_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_CURTAIN_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace("%use%", "These curtains can be used to decorate the clan hall.")
                                html.replace("%apply%", "deco curtains $stage")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("edit_fixtures", ignoreCase = true)) {
                                val html = NpcHtmlMessage(objectId)
                                html.setFile("data/html/clanHallManager/functions-apply.htm")
                                html.replace("%name%", "Front Platform (Decoration)")

                                val stage = Integer.parseInt(st.nextToken())

                                val cost: Int
                                when (stage) {
                                    1 -> cost = Config.CH_FRONT1_FEE

                                    else -> cost = Config.CH_FRONT2_FEE
                                }

                                html.replace(
                                    "%cost%",
                                    cost.toString() + "</font> adenas / " + Config.CH_FRONT_FEE_RATIO / 86400000 + " day</font>)"
                                )
                                html.replace("%use%", "Used to decorate the clan hall.")
                                html.replace("%apply%", "deco fixtures $stage")
                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("curtains", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val lvl = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_DECO_CURTAINS)
                                if (chf != null && chf.lvl == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "Stage $`val`")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (lvl) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    1 -> fee = Config.CH_CURTAIN1_FEE

                                    else -> fee = Config.CH_CURTAIN2_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_DECO_CURTAINS,
                                        lvl,
                                        fee,
                                        Config.CH_CURTAIN_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            } else if (`val`.equals("fixtures", ignoreCase = true)) {
                                `val` = st.nextToken()
                                val lvl = Integer.parseInt(`val`)

                                val html = NpcHtmlMessage(objectId)

                                val chf = clanHall!!.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM)
                                if (chf != null && chf.lvl == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm")
                                    html.replace("%val%", "Stage $`val`")
                                    html.replace("%objectId%", objectId)
                                    player.sendPacket(html)
                                    return
                                }

                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm")

                                val fee: Int
                                when (lvl) {
                                    0 -> {
                                        fee = 0
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm")
                                    }

                                    1 -> fee = Config.CH_FRONT1_FEE

                                    else -> fee = Config.CH_FRONT2_FEE
                                }

                                if (!clanHall!!.updateFunctions(
                                        player,
                                        ClanHall.FUNC_DECO_FRONTPLATEFORM,
                                        lvl,
                                        fee,
                                        Config.CH_FRONT_FEE_RATIO,
                                        chf == null
                                    )
                                )
                                    html.setFile("data/html/clanHallManager/low_adena.htm")
                                else
                                    revalidateDeco(player)

                                html.replace("%objectId%", objectId)
                                player.sendPacket(html)
                            }
                        } else {
                            val html = NpcHtmlMessage(objectId)
                            html.setFile("data/html/clanHallManager/deco.htm")

                            val chfCurtains = clanHall!!.getFunction(ClanHall.FUNC_DECO_CURTAINS)
                            if (chfCurtains != null) {
                                html.replace(
                                    "%curtain%",
                                    "- Stage " + chfCurtains.lvl + "</font> (<font color=\"FFAABB\">" + chfCurtains.lease + "</font> adenas / " + Config.CH_CURTAIN_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%curtain_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfCurtains.endTime)
                                )
                                html.replace(
                                    "%change_curtain%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Remove</a>]$curtains"
                                )
                            } else {
                                html.replace("%curtain%", "none")
                                html.replace("%curtain_period%", "none")
                                html.replace("%change_curtain%", curtains)
                            }

                            val chfPlateform = clanHall!!.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM)
                            if (chfPlateform != null) {
                                html.replace(
                                    "%fixture%",
                                    "- Stage " + chfPlateform.lvl + "</font> (<font color=\"FFAABB\">" + chfPlateform.lease + "</font> adenas / " + Config.CH_FRONT_FEE_RATIO / 86400000 + " day)"
                                )
                                html.replace(
                                    "%fixture_period%",
                                    "Next fee at " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfPlateform.endTime)
                                )
                                html.replace(
                                    "%change_fixture%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Remove</a>]$fixtures"
                                )
                            } else {
                                html.replace("%fixture%", "none")
                                html.replace("%fixture_period%", "none")
                                html.replace("%change_fixture%", fixtures)
                            }
                            html.replace("%objectId%", objectId)
                            player.sendPacket(html)
                        }
                    } else if (`val`.equals("back", ignoreCase = true))
                        showChatWindow(player)
                    else {
                        val html = NpcHtmlMessage(objectId)
                        html.setFile("data/html/clanHallManager/manage.htm")
                        html.replace("%objectId%", objectId)
                        player.sendPacket(html)
                    }
                } else {
                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/clanHallManager/not_authorized.htm")
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }
            } else if (actualCommand.equals("support", ignoreCase = true)) {
                val chf = clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)
                if (chf == null || chf.lvl == 0)
                    return

                if (player.isCursedWeaponEquipped) {
                    // Custom system message
                    player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs")
                    return
                }

                target = player

                try {
                    val id = Integer.parseInt(`val`)
                    val lvl = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 0

                    val skill = SkillTable.getInfo(id, lvl)
                    if (skill!!.skillType === L2SkillType.SUMMON)
                        player.doSimultaneousCast(skill)
                    else {
                        if (skill!!.mpConsume + skill.mpInitialConsume <= currentMp)
                            doCast(skill)
                        else {
                            val html = NpcHtmlMessage(objectId)
                            html.setFile("data/html/clanHallManager/support-no_mana.htm")
                            html.replace("%mp%", currentMp.toInt())
                            html.replace("%objectId%", objectId)
                            player.sendPacket(html)
                            return
                        }
                    }

                    val html = NpcHtmlMessage(objectId)
                    html.setFile("data/html/clanHallManager/support-done.htm")
                    html.replace("%mp%", currentMp.toInt())
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                } catch (e: Exception) {
                    player.sendMessage("Invalid skill, contact your server support.")
                }

            } else if (actualCommand.equals("list_back", ignoreCase = true)) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/clanHallManager/chamberlain.htm")
                html.replace("%npcname%", name)
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            } else if (actualCommand.equals("support_back", ignoreCase = true)) {
                val chf = clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)
                if (chf == null || chf.lvl == 0)
                    return

                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/clanHallManager/support" + clanHall!!.getFunction(ClanHall.FUNC_SUPPORT)!!.lvl + ".htm")
                html.replace("%mp%", status.currentMp.toInt())
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            } else if (actualCommand.equals("goto", ignoreCase = true)) {
                val list = TeleportLocationData.getTeleportLocation(Integer.parseInt(`val`))
                if (list != null && player.reduceAdena("Teleport", list.price, this, true))
                    player.teleToLocation(list, 0)

                player.sendPacket(ActionFailed.STATIC_PACKET)
            } else if (actualCommand.equals("WithdrawC", ignoreCase = true)) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                if (player.clanPrivileges and Clan.CP_CL_VIEW_WAREHOUSE != Clan.CP_CL_VIEW_WAREHOUSE) {
                    player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE)
                    return
                }

                if (player.clan.level == 0)
                    player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE)
                else {
                    player.activeWarehouse = player.clan.warehouse
                    player.sendPacket(WarehouseWithdrawList(player, WarehouseWithdrawList.CLAN))
                }
            } else if (actualCommand.equals("DepositC", ignoreCase = true)) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                if (player.clan != null) {
                    if (player.clan.level == 0)
                        player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE)
                    else {
                        player.activeWarehouse = player.clan.warehouse
                        player.tempInventoryDisable()
                        player.sendPacket(WarehouseDepositList(player, WarehouseDepositList.CLAN))
                    }
                }
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
        var filename = "data/html/clanHallManager/chamberlain-no.htm"

        val condition = validateCondition(player)
        if (condition == COND_OWNER)
            filename = "data/html/clanHallManager/chamberlain.htm"

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    protected fun validateCondition(player: Player): Int {
        if (clanHall == null)
            return COND_ALL_FALSE

        return if (player.clan != null) {
            if (clanHall!!.ownerId == player.clanId) COND_OWNER else COND_OWNER_FALSE

        } else COND_ALL_FALSE
    }

    companion object {
        protected val COND_OWNER_FALSE = 0
        protected val COND_ALL_FALSE = 1
        protected val COND_BUSY_BECAUSE_OF_SIEGE = 2
        protected val COND_OWNER = 3

        private val hp_grade0 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]"
        private val hp_grade1 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]"
        private val hp_grade2 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]"
        private val hp_grade3 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]"
        private val exp_grade0 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]"
        private val exp_grade1 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]"
        private val exp_grade2 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]"
        private val exp_grade3 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
        private val mp_grade0 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
        private val mp_grade1 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
        private val mp_grade2 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]"
        private val mp_grade3 =
            "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]"

        private val tele =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]"
        private val support_grade0 =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]"
        private val support_grade1 =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]"
        private val support_grade2 =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]"
        private val support_grade3 =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]"
        private val item =
            "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]"

        private val curtains =
            "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]"
        private val fixtures =
            "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]"

        private fun revalidateDeco(player: Player) {
            val ch = ClanHallManager.getClanHallByOwner(player.clan) ?: return

            player.sendPacket(ClanHallDecoration(ch))
        }
    }
}