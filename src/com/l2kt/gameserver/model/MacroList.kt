package com.l2kt.gameserver.model

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SendMacroList
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class MacroList(private val _owner: Player) {
    var revision: Int = 0
        private set
    private var _macroId: Int = 0
    private val _macroses = LinkedHashMap<Int, L2Macro>()

    val allMacroses: Array<L2Macro>
        get() = _macroses.values.toTypedArray()

    init {
        revision = 1
        _macroId = 1000
    }

    fun getMacro(id: Int): L2Macro {
        return _macroses[id - 1]!!
    }

    fun registerMacro(macro: L2Macro) {
        if (macro.id == 0) {
            macro.id = _macroId++

            while (_macroses[macro.id] != null)
                macro.id = _macroId++

            _macroses[macro.id] = macro
            registerMacroInDb(macro)
        } else {
            val old = _macroses.put(macro.id, macro)
            if (old != null)
                deleteMacroFromDb(old)

            registerMacroInDb(macro)
        }
        sendUpdate()
    }

    fun deleteMacro(id: Int) {
        val toRemove = _macroses[id]
        if (toRemove != null)
            deleteMacroFromDb(toRemove)

        _macroses.remove(id)

        val allShortCuts = _owner.allShortCuts
        for ((slot, page, type, id1) in allShortCuts) {
            if (id1 == id && type == L2ShortCut.TYPE_MACRO)
                _owner.deleteShortCut(slot, page)
        }

        sendUpdate()
    }

    fun sendUpdate() {
        revision++
        val all = allMacroses

        if (all.size == 0)
            _owner.sendPacket(SendMacroList(revision, all.size, null))
        else {
            for (m in all)
                _owner.sendPacket(SendMacroList(revision, all.size, m))
        }
    }

    private fun registerMacroInDb(macro: L2Macro) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("INSERT INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)")
                statement.setInt(1, _owner.objectId)
                statement.setInt(2, macro.id)
                statement.setInt(3, macro.icon)
                statement.setString(4, macro.name)
                statement.setString(5, macro.descr)
                statement.setString(6, macro.acronym)

                val sb = StringBuilder(300)
                for (cmd in macro.commands) {
                    StringUtil.append(sb, cmd.type, ",", cmd.d1, ",", cmd.d2)
                    if (cmd.cmd != null && cmd.cmd.length > 0)
                        StringUtil.append(sb, ",", cmd.cmd)

                    sb.append(';')
                }

                if (sb.length > 255)
                    sb.setLength(255)

                statement.setString(7, sb.toString())
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "could not store macro:", e)
        }

    }

    private fun deleteMacroFromDb(macro: L2Macro) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?")
                statement.setInt(1, _owner.objectId)
                statement.setInt(2, macro.id)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "could not delete macro:", e)
        }

    }

    fun restore() {
        _macroses.clear()

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?")
                statement.setInt(1, _owner.objectId)
                val rset = statement.executeQuery()
                while (rset.next()) {
                    val id = rset.getInt("id")
                    val icon = rset.getInt("icon")
                    val name = rset.getString("name")
                    val descr = rset.getString("descr")
                    val acronym = rset.getString("acronym")

                    val commands = ArrayList<L2Macro.L2MacroCmd>()
                    val st1 = StringTokenizer(rset.getString("commands"), ";")

                    while (st1.hasMoreTokens()) {
                        val st = StringTokenizer(st1.nextToken(), ",")
                        if (st.countTokens() < 3)
                            continue

                        val type = Integer.parseInt(st.nextToken())
                        val d1 = Integer.parseInt(st.nextToken())
                        val d2 = Integer.parseInt(st.nextToken())

                        var cmd = ""
                        if (st.hasMoreTokens())
                            cmd = st.nextToken()

                        val mcmd = L2Macro.L2MacroCmd(commands.size, type, d1, d2, cmd)
                        commands.add(mcmd)
                    }

                    val m = L2Macro(id, icon, name, descr, acronym, commands)
                    _macroses[m.id] = m
                }
                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "could not store shortcuts:", e)
        }

    }

    companion object {
        private val _log = Logger.getLogger(MacroList::class.java.name)
    }
}