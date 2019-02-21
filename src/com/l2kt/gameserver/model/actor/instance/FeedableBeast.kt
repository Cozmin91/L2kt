package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * This class is here to avoid hardcoded IDs.<br></br>
 * It refers to mobs that can be attacked but can also be fed.<br></br>
 * This class is only used by handlers in order to check the correctness of the target.<br></br>
 * However, no additional tasks are needed, since they are all handled by scripted AI.
 */
open class FeedableBeast(objectId: Int, template: NpcTemplate) : Monster(objectId, template)