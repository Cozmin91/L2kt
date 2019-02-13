package com.l2kt.gameserver.communitybbs.BB

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.communitybbs.Manager.TopicBBSManager

class Topic(
    constructorType: ConstructorType,
    val id: Int,
    val forumID: Int,
    val name: String,
    val date: Long,
    val ownerName: String,
    private val _ownerId: Int,
    private val _type: Int,
    private val _cReply: Int
) {
    enum class ConstructorType {
        RESTORE,
        CREATE
    }

    init {

        TopicBBSManager.addTopic(this)

        if (constructorType == ConstructorType.CREATE)
            insertIntoDb()
    }

    private fun insertIntoDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(INSERT_TOPIC).use { ps ->
                    ps.setInt(1, id)
                    ps.setInt(2, forumID)
                    ps.setString(3, name)
                    ps.setLong(4, date)
                    ps.setString(5, ownerName)
                    ps.setInt(6, _ownerId)
                    ps.setInt(7, _type)
                    ps.setInt(8, _cReply)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save new topic.", e)
        }

    }

    fun deleteMe(forum: Forum) {
        TopicBBSManager.deleteTopic(this)
        forum.removeTopic(id)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_TOPIC).use { ps ->
                    ps.setInt(1, id)
                    ps.setInt(2, forum.id)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete topic.", e)
        }

    }

    companion object {

        private val LOGGER = CLogger(Topic::class.java.name)

        private const val INSERT_TOPIC =
            "INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)"
        private const val DELETE_TOPIC = "DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?"

        const val MORMAL = 0
        const val MEMO = 1
    }
}