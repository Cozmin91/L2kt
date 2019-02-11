package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.holder.skillnode.ClanSkillNode
import com.l2kt.gameserver.model.holder.skillnode.FishingSkillNode
import com.l2kt.gameserver.model.holder.skillnode.GeneralSkillNode
import com.l2kt.gameserver.model.holder.skillnode.SkillNode
import java.util.*

class AcquireSkillList(private val _skillType: AcquireSkillType, skills: List<SkillNode>) : L2GameServerPacket() {

    private val _skills: List<SkillNode>

    enum class AcquireSkillType {
        USUAL,
        FISHING,
        CLAN
    }

    init {
        _skills = ArrayList(skills)
    }

    override fun writeImpl() {
        writeC(0x8a)
        writeD(_skillType.ordinal)
        writeD(_skills.size)

        when (_skillType) {
            AcquireSkillList.AcquireSkillType.USUAL -> _skills.map { it as GeneralSkillNode }.forEach { gsn ->
                writeD(gsn.id)
                writeD(gsn.value)
                writeD(gsn.value)
                writeD(gsn.correctedCost)
                writeD(0)
            }

            AcquireSkillList.AcquireSkillType.FISHING -> _skills.map { it as FishingSkillNode }.forEach { gsn ->
                writeD(gsn.id)
                writeD(gsn.value)
                writeD(gsn.value)
                writeD(0)
                writeD(1)
            }

            AcquireSkillList.AcquireSkillType.CLAN -> _skills.map { it as ClanSkillNode }.forEach { gsn ->
                writeD(gsn.id)
                writeD(gsn.value)
                writeD(gsn.value)
                writeD(gsn.cost)
                writeD(0)
            }
        }
    }
}