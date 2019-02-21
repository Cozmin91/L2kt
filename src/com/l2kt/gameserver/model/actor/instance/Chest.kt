package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * This class manages all chest.
 * @author Julian
 */
class Chest(objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    @Volatile
    var isInteracted: Boolean = false
        private set
    @Volatile
    var isSpecialDrop: Boolean = false
        private set

    init {
        setIsNoRndWalk(true)

        isInteracted = false
        isSpecialDrop = false
    }

    override fun onSpawn() {
        super.onSpawn()

        isInteracted = false
        isSpecialDrop = false
    }

    fun setInteracted() {
        isInteracted = true
    }

    fun setSpecialDrop() {
        isSpecialDrop = true
    }

    override fun doItemDrop(npcTemplate: NpcTemplate, lastAttacker: Creature) {
        var id = template.npcId

        if (!isSpecialDrop) {
            if (id in 18265..18286)
                id += 3536
            else if (id == 18287 || id == 18288)
                id = 21671
            else if (id == 18289 || id == 18290)
                id = 21694
            else if (id == 18291 || id == 18292)
                id = 21717
            else if (id == 18293 || id == 18294)
                id = 21740
            else if (id == 18295 || id == 18296)
                id = 21763
            else if (id == 18297 || id == 18298)
                id = 21786
        }

        super.doItemDrop(NpcData.getTemplate(id), lastAttacker)
    }

    override fun isMovementDisabled(): Boolean {
        if (super.isMovementDisabled())
            return true

        return if (isInteracted) false else true

    }

    override fun hasRandomAnimation(): Boolean {
        return false
    }
}