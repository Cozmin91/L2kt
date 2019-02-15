package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.BufferManager.forEach
import com.l2kt.gameserver.model.holder.BuffSkillHolder
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads and stores available [BuffSkillHolder]s for the integrated scheme buffer.<br></br>
 * Loads and stores players' buff schemes into _schemesTable (under a String name and a List of Integer skill ids).
 */
object BufferManager : IXmlReader {

    private val _schemesTable = ConcurrentHashMap<Int, HashMap<String, ArrayList<Int>>>()
    private val _availableBuffs = LinkedHashMap<Int, BuffSkillHolder>()
    private const val LOAD_SCHEMES = "SELECT * FROM buffer_schemes"
    private const val DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes"
    private const val INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)"

    /**
     * @return a list of all buff types available.
     */
    val skillTypes: List<String>
        get() {
            val skillTypes = ArrayList<String>()
            for (skill in _availableBuffs.values) {
                if (!skillTypes.contains(skill.type))
                    skillTypes.add(skill.type)
            }
            return skillTypes
        }

    val availableBuffs: Map<Int, BuffSkillHolder>
        get() = _availableBuffs

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/bufferSkills.xml")
        IXmlReader.LOGGER.info("Loaded {} available buffs.", _availableBuffs.size)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_SCHEMES).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val schemeList = ArrayList<Int>()

                            val skills =
                                rs.getString("skills").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            for (skill in skills) {
                                // Don't feed the skills list if the list is empty.
                                if (skill.isEmpty())
                                    break

                                val skillId = Integer.valueOf(skill)

                                // Integrity check to see if the skillId is available as a buff.
                                if (_availableBuffs.containsKey(skillId))
                                    schemeList.add(skillId)
                            }

                            setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Failed to load schemes data.", e)
        }

    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "category") { categoryNode ->
                val category = parseString(categoryNode.attributes, "type")
                forEach(categoryNode, "buff") { buffNode ->
                    val attrs = buffNode.attributes
                    val skillId = parseInteger(attrs, "id")!!
                    _availableBuffs[skillId] = BuffSkillHolder(
                        skillId,
                        parseInteger(attrs, "price")!!,
                        category,
                        parseString(attrs, "desc")
                    )
                }
            }
        }
    }

    fun saveSchemes() {
        try {
            L2DatabaseFactory.connection.use { con ->
                // Delete all entries from database.
                var ps = con.prepareStatement(DELETE_SCHEMES)
                ps.execute()
                ps.close()

                ps = con.prepareStatement(INSERT_SCHEME)

                // Save _schemesTable content.
                for ((key, value) in _schemesTable) {
                    for ((key1, value1) in value) {
                        // Build a String composed of skill ids seperated by a ",".
                        val sb = StringBuilder()
                        for (skillId in value1)
                            StringUtil.append(sb, skillId, ",")

                        // Delete the last "," : must be called only if there is something to delete !
                        if (sb.length > 0)
                            sb.setLength(sb.length - 1)

                        ps.setInt(1, key)
                        ps.setString(2, key1)
                        ps.setString(3, sb.toString())
                        ps.addBatch()
                    }
                }
                ps.executeBatch()
                ps.close()
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Failed to save schemes data.", e)
        }

    }

    fun setScheme(playerId: Int, schemeName: String, list: ArrayList<Int>) {
        if (!_schemesTable.containsKey(playerId))
            _schemesTable[playerId] = HashMap()
        else if (_schemesTable[playerId]!!.size >= Config.BUFFER_MAX_SCHEMES)
            return

        _schemesTable[playerId]!![schemeName] = list
    }

    /**
     * @param playerId : The player objectId to check.
     * @return the list of schemes for a given player.
     */
    fun getPlayerSchemes(playerId: Int): Map<String, ArrayList<Int>>? {
        return _schemesTable[playerId]
    }

    /**
     * @param playerId : The player objectId to check.
     * @param schemeName : The scheme name to check.
     * @return the List holding skills for the given scheme name and player, or null (if scheme or player isn't registered).
     */
    fun getScheme(playerId: Int, schemeName: String): List<Int> {
        return if (_schemesTable[playerId] == null || _schemesTable[playerId]?.get(schemeName) == null) emptyList() else _schemesTable[playerId]?.get(schemeName) ?: emptyList()
    }

    /**
     * @param playerId : The player objectId to check.
     * @param schemeName : The scheme name to check.
     * @param skillId : The skill id to check.
     * @return true if the skill is already registered on the scheme, or false otherwise.
     */
    fun getSchemeContainsSkill(playerId: Int, schemeName: String, skillId: Int): Boolean {
        val skills = getScheme(playerId, schemeName)
        if (skills.isEmpty())
            return false

        for (id in skills) {
            if (id == skillId)
                return true
        }
        return false
    }

    /**
     * @param groupType : The type of skills to return.
     * @return a list of skills ids based on the given groupType.
     */
    fun getSkillsIdsByType(groupType: String): List<Int> {
        val skills = ArrayList<Int>()
        for (skill in _availableBuffs.values) {
            if (skill.type.equals(groupType, ignoreCase = true))
                skills.add(skill.id)
        }
        return skills
    }

    fun getAvailableBuff(skillId: Int): BuffSkillHolder? {
        return _availableBuffs[skillId]
    }
}