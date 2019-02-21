package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * This class handles skills trainers.
 */
class Trainer(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        filename = if (`val` == 0)
            "" + npcId
        else
            "$npcId-$`val`"

        return "data/html/trainer/$filename.htm"
    }
}