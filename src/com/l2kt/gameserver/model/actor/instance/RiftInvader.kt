package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate

// Not longer needed since rift monster targeting control now is handled by the room zones for any mob
class RiftInvader(objectId: Int, template: NpcTemplate) : Monster(objectId, template)
