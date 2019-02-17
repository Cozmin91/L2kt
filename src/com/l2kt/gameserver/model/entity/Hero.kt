package com.l2kt.gameserver.model.entity

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

object Hero {

    private val _heroes = HashMap<Int, StatsSet>()
    private val _completeHeroes = HashMap<Int, StatsSet>()

    private val _heroCounts = HashMap<Int, StatsSet>()
    private val _heroFights = HashMap<Int, List<StatsSet>>()
    private val _fights = ArrayList<StatsSet>()

    private val _heroDiaries = HashMap<Int, MutableList<StatsSet>>()
    private val _heroMessages = HashMap<Int, String>()
    private val _diary = ArrayList<StatsSet>()

    private val _log = Logger.getLogger(Hero::class.java.name)

    private const val GET_HEROES =
        "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id AND heroes.played = 1"
    private const val GET_ALL_HEROES =
        "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id"
    private const val UPDATE_ALL = "UPDATE heroes SET played = 0"
    private const val INSERT_HERO = "INSERT INTO heroes (char_id, class_id, count, played, active) VALUES (?,?,?,?,?)"
    private const val UPDATE_HERO = "UPDATE heroes SET count = ?, played = ?, active = ? WHERE char_id = ?"
    private const val GET_CLAN_ALLY =
        "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.obj_Id = ?"

    private const val DELETE_ITEMS =
        "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_Id FROM characters WHERE accesslevel > 0)"

    private const val GET_DIARIES = "SELECT * FROM  heroes_diary WHERE char_id=? ORDER BY time ASC"
    private const val UPDATE_DIARIES = "INSERT INTO heroes_diary (char_id, time, action, param) values(?,?,?,?)"

    const val COUNT = "count"
    const val PLAYED = "played"
    const val CLAN_NAME = "clan_name"
    const val CLAN_CREST = "clan_crest"
    const val ALLY_NAME = "ally_name"
    const val ALLY_CREST = "ally_crest"
    const val ACTIVE = "active"

    const val ACTION_RAID_KILLED = 1
    const val ACTION_HERO_GAINED = 2
    const val ACTION_CASTLE_TAKEN = 3

    val heroes: Map<Int, StatsSet>
        get() = _heroes

    val allHeroes: Map<Int, StatsSet>
        get() = _completeHeroes

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                var statement = con.prepareStatement(GET_HEROES)
                var rset = statement.executeQuery()
                val statement2 = con.prepareStatement(GET_CLAN_ALLY)
                var rset2: ResultSet? = null

                while (rset.next()) {
                    val hero = StatsSet()
                    val charId = rset.getInt(Olympiad.CHAR_ID)
                    hero[Olympiad.CHAR_NAME] = rset.getString(Olympiad.CHAR_NAME)
                    hero[Olympiad.CLASS_ID] = rset.getInt(Olympiad.CLASS_ID).toDouble()
                    hero[COUNT] = rset.getInt(COUNT).toDouble()
                    hero[PLAYED] = rset.getInt(PLAYED).toDouble()
                    hero[ACTIVE] = rset.getInt(ACTIVE).toDouble()

                    loadFights(charId)
                    loadDiary(charId)
                    loadMessage(charId)

                    statement2.setInt(1, charId)
                    rset2 = statement2.executeQuery()

                    if (rset2!!.next()) {
                        val clanId = rset2.getInt("clanid")
                        val allyId = rset2.getInt("allyId")

                        var clanName = ""
                        var allyName: String? = ""
                        var clanCrest = 0
                        var allyCrest = 0

                        if (clanId > 0) {
                            clanName = ClanTable.getClan(clanId)!!.name
                            clanCrest = ClanTable.getClan(clanId)!!.crestId

                            if (allyId > 0) {
                                allyName = ClanTable.getClan(clanId)!!.allyName
                                allyCrest = ClanTable.getClan(clanId)!!.allyCrestId
                            }
                        }

                        hero[CLAN_CREST] = clanCrest.toDouble()
                        hero[CLAN_NAME] = clanName
                        hero[ALLY_CREST] = allyCrest.toDouble()
                        hero[ALLY_NAME] = allyName
                    }

                    rset2.close()
                    statement2.clearParameters()

                    _heroes[charId] = hero
                }

                rset.close()
                statement.close()

                statement = con.prepareStatement(GET_ALL_HEROES)
                rset = statement.executeQuery()

                while (rset.next()) {
                    val hero = StatsSet()
                    val charId = rset.getInt(Olympiad.CHAR_ID)
                    hero[Olympiad.CHAR_NAME] = rset.getString(Olympiad.CHAR_NAME)
                    hero[Olympiad.CLASS_ID] = rset.getInt(Olympiad.CLASS_ID).toDouble()
                    hero[COUNT] = rset.getInt(COUNT).toDouble()
                    hero[PLAYED] = rset.getInt(PLAYED).toDouble()
                    hero[ACTIVE] = rset.getInt(ACTIVE).toDouble()

                    statement2.setInt(1, charId)
                    rset2 = statement2.executeQuery()

                    if (rset2!!.next()) {
                        val clanId = rset2.getInt("clanid")
                        val allyId = rset2.getInt("allyId")

                        var clanName = ""
                        var allyName: String? = ""
                        var clanCrest = 0
                        var allyCrest = 0

                        if (clanId > 0) {
                            clanName = ClanTable.getClan(clanId)!!.name
                            clanCrest = ClanTable.getClan(clanId)!!.crestId

                            if (allyId > 0) {
                                allyName = ClanTable.getClan(clanId)!!.allyName
                                allyCrest = ClanTable.getClan(clanId)!!.allyCrestId
                            }
                        }

                        hero[CLAN_CREST] = clanCrest.toDouble()
                        hero[CLAN_NAME] = clanName
                        hero[ALLY_CREST] = allyCrest.toDouble()
                        hero[ALLY_NAME] = allyName
                    }

                    rset2.close()
                    statement2.clearParameters()

                    _completeHeroes[charId] = hero
                }

                statement2.close()
                rset.close()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "Hero: Couldnt load heroes: " + e.message, e)
        }

        _log.info("Hero: Loaded " + _heroes.size + " heroes.")
        _log.info("Hero: Loaded " + _completeHeroes.size + " all time heroes.")
    }

    private fun calcFightTime(fightTime: Long): String {
        var currentFightTime = fightTime
        val format = String.format("%%0%dd", 2)
        currentFightTime /= 1000
        val seconds = String.format(format, currentFightTime % 60)
        val minutes = String.format(format, currentFightTime % 3600 / 60)
        return "$minutes:$seconds"
    }

    private fun deleteItemsInDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(DELETE_ITEMS)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "Hero: Couldn't delete items on db: " + e.message, e)
        }

    }

    /**
     * Restore hero message from Db.
     * @param charId
     */
    fun loadMessage(charId: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("SELECT message FROM heroes WHERE char_id=?")
                statement.setInt(1, charId)

                val rset = statement.executeQuery()
                rset.next()

                _heroMessages[charId] = rset.getString("message")

                rset.close()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "Hero: Couldnt load hero message for char_id: $charId", e)
        }

    }

    fun loadDiary(charId: Int) {
        var entries = 0

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(GET_DIARIES)
                statement.setInt(1, charId)

                val rset = statement.executeQuery()
                while (rset.next()) {
                    val entry = StatsSet()

                    val time = rset.getLong("time")
                    val action = rset.getInt("action")
                    val param = rset.getInt("param")

                    entry["date"] = SimpleDateFormat("yyyy-MM-dd HH").format(time)

                    if (action == ACTION_RAID_KILLED) {
                        val template = NpcData.getTemplate(param)
                        if (template != null)
                            entry["action"] = template.name + " was defeated"
                    } else if (action == ACTION_HERO_GAINED)
                        entry["action"] = "Gained Hero status"
                    else if (action == ACTION_CASTLE_TAKEN) {
                        val castle = CastleManager.getCastleById(param)
                        if (castle != null)
                            entry["action"] = castle.name + " Castle was successfuly taken"
                    }
                    _diary.add(entry)

                    entries++
                }
                rset.close()
                statement.close()

                _heroDiaries[charId] = _diary

                _log.info("Hero: Loaded " + entries + " diary entries for hero: " + PlayerInfoTable.getPlayerName(charId))
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "Hero: Couldnt load hero diary for char_id: " + charId + ", " + e.message, e)
        }

    }

    fun loadFights(charId: Int) {
        val heroCountData = StatsSet()

        val data = Calendar.getInstance()
        data.set(Calendar.DAY_OF_MONTH, 1)
        data.set(Calendar.HOUR_OF_DAY, 0)
        data.set(Calendar.MINUTE, 0)
        data.set(Calendar.MILLISECOND, 0)

        val from = data.timeInMillis
        var numberOfFights = 0
        var victories = 0
        var losses = 0
        var draws = 0

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC")
                statement.setInt(1, charId)
                statement.setInt(2, charId)
                statement.setLong(3, from)
                val rset = statement.executeQuery()

                while (rset.next()) {
                    val charOneId = rset.getInt("charOneId")
                    val charOneClass = rset.getInt("charOneClass")
                    val charTwoId = rset.getInt("charTwoId")
                    val charTwoClass = rset.getInt("charTwoClass")
                    val winner = rset.getInt("winner")
                    val start = rset.getLong("start")
                    val time = rset.getInt("time")
                    val classed = rset.getInt("classed")

                    if (charId == charOneId) {
                        val name = PlayerInfoTable.getPlayerName(charTwoId)
                        val cls = PlayerData.getClassNameById(charTwoClass)
                        if (name != null && cls != null) {
                            val fight = StatsSet()
                            fight["oponent"] = name
                            fight["oponentclass"] = cls

                            fight["time"] = calcFightTime(time.toLong())
                            fight["start"] = SimpleDateFormat("yyyy-MM-dd HH:mm").format(start)

                            fight["classed"] = classed.toDouble()
                            if (winner == 1) {
                                fight["result"] = "<font color=\"00ff00\">victory</font>"
                                victories++
                            } else if (winner == 2) {
                                fight["result"] = "<font color=\"ff0000\">loss</font>"
                                losses++
                            } else if (winner == 0) {
                                fight["result"] = "<font color=\"ffff00\">draw</font>"
                                draws++
                            }

                            _fights.add(fight)

                            numberOfFights++
                        }
                    } else if (charId == charTwoId) {
                        val name = PlayerInfoTable.getPlayerName(charOneId)
                        val cls = PlayerData.getClassNameById(charOneClass)
                        if (name != null && cls != null) {
                            val fight = StatsSet()
                            fight["oponent"] = name
                            fight["oponentclass"] = cls

                            fight["time"] = calcFightTime(time.toLong())
                            fight["start"] = SimpleDateFormat("yyyy-MM-dd HH:mm").format(start)

                            fight["classed"] = classed.toDouble()
                            if (winner == 1) {
                                fight["result"] = "<font color=\"ff0000\">loss</font>"
                                losses++
                            } else if (winner == 2) {
                                fight["result"] = "<font color=\"00ff00\">victory</font>"
                                victories++
                            } else if (winner == 0) {
                                fight["result"] = "<font color=\"ffff00\">draw</font>"
                                draws++
                            }

                            _fights.add(fight)

                            numberOfFights++
                        }
                    }
                }
                rset.close()
                statement.close()

                heroCountData["victory"] = victories.toDouble()
                heroCountData["draw"] = draws.toDouble()
                heroCountData["loss"] = losses.toDouble()

                _heroCounts[charId] = heroCountData
                _heroFights[charId] = _fights

                _log.info("Hero: Loaded " + numberOfFights + " fights for: " + PlayerInfoTable.getPlayerName(charId))
            }
        } catch (e: SQLException) {
            _log.log(
                Level.WARNING,
                "Hero: Couldnt load hero fights history for char_id: " + charId + ", " + e.message,
                e
            )
        }

    }

    fun getHeroByClass(classid: Int): Int {
        if (!_heroes.isEmpty()) {
            for ((key, value) in _heroes) {
                if (value.getInteger(Olympiad.CLASS_ID) == classid)
                    return key
            }
        }
        return 0
    }

    fun resetData() {
        _heroDiaries.clear()
        _heroFights.clear()
        _heroCounts.clear()
        _heroMessages.clear()
    }

    fun showHeroDiary(activeChar: Player, heroclass: Int, charid: Int, page: Int) {
        if (!_heroDiaries.containsKey(charid))
            return

        val perpage = 10

        val mainList = _heroDiaries[charid]!!

        val html = NpcHtmlMessage(0)
        html.setFile("data/html/olympiad/herodiary.htm")
        html.replace("%heroname%", PlayerInfoTable.getPlayerName(charid)!!)
        html.replace("%message%", _heroMessages[charid] ?: "")
        html.disableValidation()

        if (!mainList.isEmpty()) {
            val list = ArrayList<StatsSet>()
            list.addAll(mainList)
            Collections.reverse(list)

            var color = true
            var counter = 0
            var breakat = 0

            val sb = StringBuilder(500)
            for (i in (page - 1) * perpage until list.size) {
                breakat = i
                val _diaryentry = list[i]
                StringUtil.append(
                    sb,
                    "<tr><td>",
                    if (color) "<table width=270 bgcolor=\"131210\">" else "<table width=270>",
                    "<tr><td width=270><font color=\"LEVEL\">",
                    _diaryentry.getString("date"),
                    ":xx</font></td></tr><tr><td width=270>",
                    _diaryentry.getString("action"),
                    "</td></tr><tr><td>&nbsp;</td></tr></table></td></tr>"
                )
                color = !color

                counter++
                if (counter >= perpage)
                    break
            }

            if (breakat < list.size - 1)
                html.replace(
                    "%buttprev%",
                    "<button value=\"Prev\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                )
            else
                html.replace("%buttprev%", "")

            if (page > 1)
                html.replace(
                    "%buttnext%",
                    "<button value=\"Next\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                )
            else
                html.replace("%buttnext%", "")

            html.replace("%list%", sb.toString())
        } else {
            html.replace("%list%", "")
            html.replace("%buttprev%", "")
            html.replace("%buttnext%", "")
        }
        activeChar.sendPacket(html)
    }

    fun showHeroFights(activeChar: Player, heroclass: Int, charid: Int, page: Int) {
        if (!_heroFights.containsKey(charid))
            return

        val perpage = 20
        var win = 0
        var loss = 0
        var draw = 0

        val list = _heroFights[charid]!!

        val html = NpcHtmlMessage(0)
        html.setFile("data/html/olympiad/herohistory.htm")
        html.replace("%heroname%", PlayerInfoTable.getPlayerName(charid)!!)
        html.disableValidation()

        if (!list.isEmpty()) {
            if (_heroCounts.containsKey(charid)) {
                val _herocount = _heroCounts[charid]!!
                win = _herocount.getInteger("victory")
                loss = _herocount.getInteger("loss")
                draw = _herocount.getInteger("draw")
            }

            var color = true
            var counter = 0
            var breakat = 0

            val sb = StringBuilder(500)
            for (i in (page - 1) * perpage until list.size) {
                breakat = i
                val fight = list.get(i)
                StringUtil.append(
                    sb,
                    "<tr><td>",
                    if (color) "<table width=270 bgcolor=\"131210\">" else "<table width=270><tr><td width=220><font color=\"LEVEL\">",
                    fight.getString("start"),
                    "</font>&nbsp;&nbsp;",
                    fight.getString("result"),
                    "</td><td width=50 align=right>",
                    if (fight.getInteger("classed") > 0) "<font color=\"FFFF99\">cls</font>" else "<font color=\"999999\">non-cls<font>",
                    "</td></tr><tr><td width=220>vs ",
                    fight.getString("oponent"),
                    " (",
                    fight.getString("oponentclass"),
                    ")</td><td width=50 align=right>(",
                    fight.getString("time"),
                    ")</td></tr><tr><td colspan=2>&nbsp;</td></tr></table></td></tr>"
                )
                color = !color

                counter++
                if (counter >= perpage)
                    break
            }

            if (breakat < list.size - 1)
                html.replace(
                    "%buttprev%",
                    "<button value=\"Prev\" action=\"bypass _match?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                )
            else
                html.replace("%buttprev%", "")

            if (page > 1)
                html.replace(
                    "%buttnext%",
                    "<button value=\"Next\" action=\"bypass _match?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                )
            else
                html.replace("%buttnext%", "")

            html.replace("%list%", sb.toString())
        } else {
            html.replace("%list%", "")
            html.replace("%buttprev%", "")
            html.replace("%buttnext%", "")
        }

        html.replace("%win%", win)
        html.replace("%draw%", draw)
        html.replace("%loos%", loss)

        activeChar.sendPacket(html)
    }

    @Synchronized
    fun computeNewHeroes(newHeroes: List<StatsSet>) {
        updateHeroes(true)

        if (!_heroes.isEmpty()) {
            for (hero in _heroes.values) {
                val name = hero.getString(Olympiad.CHAR_NAME)

                val player = World.getPlayer(name) ?: continue

                player.isHero = false

                // Unequip hero items, if found.
                for (i in 0 until Inventory.PAPERDOLL_TOTALSLOTS) {
                    val equippedItem = player.inventory!!.getPaperdollItem(i)
                    if (equippedItem != null && equippedItem.isHeroItem)
                        player.inventory!!.unEquipItemInSlot(i)
                }

                // Check inventory items.
                for (item in player.inventory!!.getAvailableItems(false, true)) {
                    if (!item.isHeroItem)
                        continue

                    player.destroyItem("Hero", item, null, true)
                }

                player.broadcastUserInfo()
            }
        }

        if (newHeroes.isEmpty()) {
            _heroes.clear()
            return
        }

        val heroes = HashMap<Int, StatsSet>()

        for (hero in newHeroes) {
            val charId = hero.getInteger(Olympiad.CHAR_ID)

            if (_completeHeroes.containsKey(charId)) {
                val oldHero = _completeHeroes[charId]!!
                val count = oldHero.getInteger(COUNT)
                oldHero[COUNT] = (count + 1).toDouble()
                oldHero[PLAYED] = 1.0
                oldHero[ACTIVE] = 0.0

                heroes[charId] = oldHero
            } else {
                val newHero = StatsSet()
                newHero[Olympiad.CHAR_NAME] = hero.getString(Olympiad.CHAR_NAME)
                newHero[Olympiad.CLASS_ID] = hero.getInteger(Olympiad.CLASS_ID).toDouble()
                newHero[COUNT] = 1.0
                newHero[PLAYED] = 1.0
                newHero[ACTIVE] = 0.0

                heroes[charId] = newHero
            }
        }

        deleteItemsInDb()

        _heroes.clear()
        _heroes.putAll(heroes)

        heroes.clear()

        updateHeroes(false)
    }

    fun updateHeroes(setDefault: Boolean) {
        try {
            L2DatabaseFactory.connection.use { con ->
                if (setDefault) {
                    val statement = con.prepareStatement(UPDATE_ALL)
                    statement.execute()
                    statement.close()
                } else {
                    var statement: PreparedStatement

                    for ((heroId, hero) in _heroes) {

                        if (!_completeHeroes.containsKey(heroId)) {
                            statement = con.prepareStatement(INSERT_HERO)
                            statement.setInt(1, heroId)
                            statement.setInt(2, hero.getInteger(Olympiad.CLASS_ID))
                            statement.setInt(3, hero.getInteger(COUNT))
                            statement.setInt(4, hero.getInteger(PLAYED))
                            statement.setInt(5, hero.getInteger(ACTIVE))
                            statement.execute()
                            statement.close()

                            statement = con.prepareStatement(GET_CLAN_ALLY)
                            statement.setInt(1, heroId)
                            val rset = statement.executeQuery()

                            if (rset.next()) {
                                val clanId = rset.getInt("clanid")
                                val allyId = rset.getInt("allyId")

                                var clanName = ""
                                var allyName: String? = ""
                                var clanCrest = 0
                                var allyCrest = 0

                                if (clanId > 0) {
                                    clanName = ClanTable.getClan(clanId)!!.name
                                    clanCrest = ClanTable.getClan(clanId)!!.crestId

                                    if (allyId > 0) {
                                        allyName = ClanTable.getClan(clanId)!!.allyName
                                        allyCrest = ClanTable.getClan(clanId)!!.allyCrestId
                                    }
                                }

                                hero[CLAN_CREST] = clanCrest.toDouble()
                                hero[CLAN_NAME] = clanName
                                hero[ALLY_CREST] = allyCrest.toDouble()
                                hero[ALLY_NAME] = allyName
                            }

                            rset.close()
                            statement.close()

                            _heroes[heroId] = hero
                            _completeHeroes[heroId] = hero
                        } else {
                            statement = con.prepareStatement(UPDATE_HERO)
                            statement.setInt(1, hero.getInteger(COUNT))
                            statement.setInt(2, hero.getInteger(PLAYED))
                            statement.setInt(3, hero.getInteger(ACTIVE))
                            statement.setInt(4, heroId)
                            statement.execute()
                            statement.close()
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _log.log(Level.WARNING, "Hero: Couldnt update heroes: " + e.message, e)
        }

    }

    fun setHeroGained(charId: Int) {
        setDiaryData(charId, ACTION_HERO_GAINED, 0)
    }

    fun setRBkilled(charId: Int, npcId: Int) {
        setDiaryData(charId, ACTION_RAID_KILLED, npcId)

        val template = NpcData.getTemplate(npcId)

        if (_heroDiaries.containsKey(charId) && template != null) {
            // Get Data
            val list = _heroDiaries[charId]!!

            // Clear old data
            _heroDiaries.remove(charId)

            // Prepare new data
            val entry = StatsSet()
            entry["date"] = SimpleDateFormat("yyyy-MM-dd HH").format(System.currentTimeMillis())
            entry["action"] = template.name + " was defeated"

            // Add to old list
            list.add(entry)

            // Put new list into diary
            _heroDiaries[charId] = list
        }
    }

    fun setCastleTaken(charId: Int, castleId: Int) {
        setDiaryData(charId, ACTION_CASTLE_TAKEN, castleId)

        val castle = CastleManager.getCastleById(castleId)

        if (_heroDiaries.containsKey(charId) && castle != null) {
            // Get Data
            val list = _heroDiaries[charId]!!

            // Clear old data
            _heroDiaries.remove(charId)

            // Prepare new data
            val entry = StatsSet()
            entry["date"] = SimpleDateFormat("yyyy-MM-dd HH").format(System.currentTimeMillis())
            entry["action"] = castle.name + " Castle was successfuly taken"

            // Add to old list
            list.add(entry)

            // Put new list into diary
            _heroDiaries[charId] = list
        }
    }

    fun setDiaryData(charId: Int, action: Int, param: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(UPDATE_DIARIES)
                statement.setInt(1, charId)
                statement.setLong(2, System.currentTimeMillis())
                statement.setInt(3, action)
                statement.setInt(4, param)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.SEVERE, "Hero: SQL exception while saving DiaryData.", e)
        }

    }

    /**
     * Set new hero message for hero
     * @param player the player instance
     * @param message String to set
     */
    fun setHeroMessage(player: Player, message: String) {
        _heroMessages[player.objectId] = message
    }

    /**
     * Update hero message in database
     * @param charId character objid
     */
    fun saveHeroMessage(charId: Int) {
        if (_heroMessages[charId] == null)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?;")
                statement.setString(1, _heroMessages[charId])
                statement.setInt(2, charId)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.SEVERE, "Hero: SQL exception while saving HeroMessage.", e)
        }

    }

    /**
     * Saving task for [Hero]<BR></BR>
     * Save all hero messages to DB.
     */
    fun shutdown() {
        for (charId in _heroMessages.keys)
            saveHeroMessage(charId)
    }

    fun isActiveHero(id: Int): Boolean {
        val entry = _heroes[id]

        return entry != null && entry.getInteger(ACTIVE) == 1
    }

    fun isInactiveHero(id: Int): Boolean {
        val entry = _heroes[id]

        return entry != null && entry.getInteger(ACTIVE) == 0
    }

    fun activateHero(player: Player) {
        val hero = _heroes[player.objectId]!!
        hero[ACTIVE] = 1.0

        _heroes[player.objectId] = hero

        player.isHero = true
        player.broadcastPacket(SocialAction(player, 16))
        player.broadcastUserInfo()

        val clan = player.clan
        if (clan != null && clan.level >= 5) {
            val name = hero.getString("char_name")

            clan.addReputationScore(1000)
            clan.broadcastToOnlineMembers(
                PledgeShowInfoUpdate(clan),
                SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS).addString(
                    name
                ).addNumber(1000)
            )
        }

        // Set Gained hero and reload data
        setHeroGained(player.objectId)
        loadFights(player.objectId)
        loadDiary(player.objectId)
        _heroMessages[player.objectId] = ""

        updateHeroes(false)
    }
}