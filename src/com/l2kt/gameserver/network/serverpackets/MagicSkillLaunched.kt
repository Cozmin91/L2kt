package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature

/**
 * format ddddd d
 */
class MagicSkillLaunched : L2GameServerPacket {
    private val _charObjId: Int
    private val _skillId: Int
    private val _skillLevel: Int
    private val _numberOfTargets: Int
    private var _targets = listOf<WorldObject>()
    private val _singleTargetId: Int

    constructor(cha: Creature, skillId: Int, skillLevel: Int, targets: List<WorldObject>) {
        _charObjId = cha.objectId
        _skillId = skillId
        _skillLevel = skillLevel
        _numberOfTargets = targets.size
        _targets = targets
        _singleTargetId = 0
    }

    constructor(cha: Creature, skillId: Int, skillLevel: Int) {
        _charObjId = cha.objectId
        _skillId = skillId
        _skillLevel = skillLevel
        _numberOfTargets = 1
        _singleTargetId = cha.targetId
    }

    override fun writeImpl() {
        writeC(0x76)
        writeD(_charObjId)
        writeD(_skillId)
        writeD(_skillLevel)
        writeD(_numberOfTargets) // also failed or not?
        if (_singleTargetId != 0 || _numberOfTargets == 0)
            writeD(_singleTargetId)
        else
            for (target in _targets) {
                try {
                    writeD(target.objectId)
                } catch (e: NullPointerException) {
                    writeD(0) // untested
                }

            }
    }
}