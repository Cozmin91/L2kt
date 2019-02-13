package com.l2kt.gameserver.model

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.network.serverpackets.ExAutoSoulShot
import com.l2kt.gameserver.network.serverpackets.ShortCutInit
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class ShortCuts(private val _owner: Player?) {
    private val _shortCuts = TreeMap<Int, L2ShortCut>()

    val allShortCuts: Array<L2ShortCut>
        get() = _shortCuts.values.toTypedArray()

    fun getShortCut(slot: Int, page: Int): L2ShortCut? {
        var sc: L2ShortCut? = _shortCuts[slot + page * MAX_SHORTCUTS_PER_BAR]

        // verify shortcut
        if (sc != null && sc.type == L2ShortCut.TYPE_ITEM) {
            if (_owner!!.inventory!!.getItemByObjectId(sc.id) == null) {
                deleteShortCut(sc.slot, sc.page)
                sc = null
            }
        }
        return sc
    }

    @Synchronized
    fun registerShortCut(shortcut: L2ShortCut) {
        // verify shortcut
        if (shortcut.type == L2ShortCut.TYPE_ITEM) {
            val item = _owner!!.inventory!!.getItemByObjectId(shortcut.id) ?: return

            if (item.isEtcItem)
                shortcut.sharedReuseGroup = item.etcItem!!.sharedReuseGroup
        }
        val oldShortCut = _shortCuts.put(shortcut.slot + shortcut.page * MAX_SHORTCUTS_PER_BAR, shortcut)
        registerShortCutInDb(shortcut, oldShortCut)
    }

    private fun registerShortCutInDb(shortcut: L2ShortCut, oldShortCut: L2ShortCut?) {
        if (oldShortCut != null)
            deleteShortCutFromDb(oldShortCut)

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)")
                statement.setInt(1, _owner!!.objectId)
                statement.setInt(2, shortcut.slot)
                statement.setInt(3, shortcut.page)
                statement.setInt(4, shortcut.type)
                statement.setInt(5, shortcut.id)
                statement.setInt(6, shortcut.level)
                statement.setInt(7, _owner.classIndex)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Could not store character shortcut: " + e.message, e)
        }

    }

    /**
     * @param slot
     * @param page
     */
    @Synchronized
    fun deleteShortCut(slot: Int, page: Int) {
        val old = _shortCuts.remove(slot + page * 12)
        if (old == null || _owner == null)
            return

        deleteShortCutFromDb(old)
        if (old.type == L2ShortCut.TYPE_ITEM) {
            val item = _owner.inventory!!.getItemByObjectId(old.id)

            if (item != null && item.itemType === EtcItemType.SHOT) {
                if (_owner.removeAutoSoulShot(item.itemId))
                    _owner.sendPacket(ExAutoSoulShot(item.itemId, 0))
            }
        }

        _owner.sendPacket(ShortCutInit(_owner))

        for (shotId in _owner.autoSoulShot)
            _owner.sendPacket(ExAutoSoulShot(shotId, 1))
    }

    @Synchronized
    fun deleteShortCutByObjectId(objectId: Int) {
        for ((slot, page, type, id) in _shortCuts.values) {
            if (type == L2ShortCut.TYPE_ITEM && id == objectId) {
                deleteShortCut(slot, page)
                break
            }
        }
    }

    /**
     * @param shortcut
     */
    private fun deleteShortCutFromDb(shortcut: L2ShortCut) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?")
                statement.setInt(1, _owner!!.objectId)
                statement.setInt(2, shortcut.slot)
                statement.setInt(3, shortcut.page)
                statement.setInt(4, _owner.classIndex)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Could not delete character shortcut: " + e.message, e)
        }

    }

    fun restore() {
        _shortCuts.clear()
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?")
                statement.setInt(1, _owner!!.objectId)
                statement.setInt(2, _owner.classIndex)

                val rset = statement.executeQuery()

                while (rset.next()) {
                    val slot = rset.getInt("slot")
                    val page = rset.getInt("page")
                    val type = rset.getInt("type")
                    val id = rset.getInt("shortcut_id")
                    val level = rset.getInt("level")

                    val sc = L2ShortCut(slot, page, type, id, level, 1)
                    _shortCuts[slot + page * MAX_SHORTCUTS_PER_BAR] = sc
                }

                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Could not restore character shortcuts: " + e.message, e)
        }

        // verify shortcuts
        for (sc in allShortCuts) {
            if (sc.type == L2ShortCut.TYPE_ITEM) {
                val item = _owner!!.inventory!!.getItemByObjectId(sc.id)
                if (item == null)
                    deleteShortCut(sc.slot, sc.page)
                else if (item.isEtcItem)
                    sc.sharedReuseGroup = item.etcItem!!.sharedReuseGroup
            }
        }
    }

    companion object {
        private val _log = Logger.getLogger(ShortCuts::class.java.name)

        private const val MAX_SHORTCUTS_PER_BAR = 12
    }
}