package com.l2kt.gameserver.model

import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used to retain access level informations, such as isGM() and multiple allowed actions (experience gain, allow transactions/peace attack/damage dealing/...).
 */
class AccessLevel(set: StatsSet) {
    val level: Int = set.getInteger("level")
    val name: String = set.getString("name")
    private val _childLevel: Int = set.getInteger("childLevel", 0)
    private var _childAccess: AccessLevel? = null
    val nameColor: Int = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"))!!
    val titleColor: Int = Integer.decode("0x" + set.getString("titleColor", "FFFF77"))!!
    @get:JvmName("canTakeAggro")
    val canTakeAggro: Boolean = set.getBool("takeAggro", true)
    val isGm: Boolean = set.getBool("isGM", false)

    @get:JvmName("allowPeaceAttack")
    val allowPeaceAttack: Boolean = set.getBool("allowPeaceAttack", false)
    val allowFixedRes: Boolean = set.getBool("allowFixedRes", false)
    val allowTransaction: Boolean = set.getBool("allowTransaction", true)
    val allowAltG: Boolean = set.getBool("allowAltg", false)
    @get:JvmName("canGiveDamage")
    val canGiveDamage: Boolean = set.getBool("giveDamage", true)
    @get:JvmName("canGainExp")
    val canGainExp: Boolean = set.getBool("gainExp", true)

    /**
     * Set the child of a [AccessLevel] if not existing, then verify if this access level is associated to AccessLevel set as parameter.
     * @param access : The AccessLevel to check.
     * @return true if a child access level is equals to access, otherwise false.
     */
    fun hasChildAccess(access: AccessLevel): Boolean {
        // No child access has been found ; we check if a child level has been set. If yes, then we dig into AdminData to find back the AccessLevel and we set it for future usage.
        if (_childAccess == null && _childLevel > 0)
            _childAccess = AdminData.getInstance().getAccessLevel(_childLevel)

        return _childAccess != null && (_childAccess!!.level == access.level || _childAccess!!.hasChildAccess(access))
    }
}