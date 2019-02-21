package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.base.ClassType

class VillageMasterPriest(objectId: Int, template: NpcTemplate) : VillageMaster(objectId, template) {

    override fun checkVillageMasterRace(pclass: ClassId?): Boolean {
        return if (pclass == null) false else pclass.race === ClassRace.HUMAN || pclass.race === ClassRace.ELF
    }

    override fun checkVillageMasterTeachType(pclass: ClassId?): Boolean {
        return if (pclass == null) false else pclass.type === ClassType.PRIEST
    }
}