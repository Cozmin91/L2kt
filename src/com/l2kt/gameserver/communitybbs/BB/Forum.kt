package com.l2kt.gameserver.communitybbs.BB

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.communitybbs.Manager.ForumsBBSManager
import com.l2kt.gameserver.communitybbs.Manager.TopicBBSManager
import java.util.*

class Forum {

    private val _children = ArrayList<Forum>()
    private val _topics = HashMap<Int, Topic>()

    val id: Int
    private var _forumName: String? = null

    private var _forumType: Int = 0
    private var _forumPost: Int = 0
    private var _forumPerm: Int = 0

    private val _parent: Forum?

    private var _ownerId: Int = 0

    private var _loaded = false

    val topicSize: Int
        get() {
            vload()

            return _topics.size
        }

    val name: String?
        get() {
            vload()

            return _forumName
        }

    val type: Int
        get() {
            vload()

            return _forumType
        }

    constructor(forumId: Int, parent: Forum?) {
        id = forumId
        _parent = parent
    }

    constructor(name: String, parent: Forum, type: Int, perm: Int, ownerId: Int) {
        _forumName = name
        id = ForumsBBSManager.aNewID

        _forumType = type
        _forumPost = 0
        _forumPerm = perm
        _parent = parent
        _ownerId = ownerId

        parent._children.add(this)
        ForumsBBSManager.addForum(this)
        _loaded = true
    }

    private fun load() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_FORUMS).use { ps ->
                    con.prepareStatement(RESTORE_TOPICS).use { ps2 ->
                        ps.setInt(1, id)

                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                _forumName = rs.getString("forum_name")

                                _forumPost = rs.getInt("forum_post")
                                _forumType = rs.getInt("forum_type")
                                _forumPerm = rs.getInt("forum_perm")

                                _ownerId = rs.getInt("forum_owner_id")
                            }
                        }

                        ps2.setInt(1, id)

                        ps2.executeQuery().use { rs2 ->
                            while (rs2.next()) {
                                val topic = Topic(
                                    Topic.ConstructorType.RESTORE,
                                    rs2.getInt("topic_id"),
                                    rs2.getInt("topic_forum_id"),
                                    rs2.getString("topic_name"),
                                    rs2.getLong("topic_date"),
                                    rs2.getString("topic_ownername"),
                                    rs2.getInt("topic_ownerid"),
                                    rs2.getInt("topic_type"),
                                    rs2.getInt("topic_reply")
                                )

                                _topics[topic.id] = topic

                                if (topic.id > TopicBBSManager.getMaxID(this))
                                    TopicBBSManager.setMaxID(topic.id, this)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load forums with id {}.", e, id)
        }

    }

    private fun getChildren() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_CHILDREN).use { ps ->
                    ps.setInt(1, id)

                    ps.executeQuery().use { result ->
                        while (result.next()) {
                            val forum = Forum(result.getInt("forum_id"), this)

                            _children.add(forum)

                            ForumsBBSManager.addForum(forum)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load children forum for parentId {}.", e, id)
        }

    }

    fun getTopic(id: Int): Topic ?{
        vload()

        return _topics[id]
    }

    fun addTopic(topic: Topic) {
        vload()

        _topics[topic.id] = topic
    }

    fun getChildByName(name: String): Forum? {
        vload()

        return _children.stream().filter { f -> f.name == name }.findFirst().orElse(null)
    }

    fun removeTopic(id: Int) {
        _topics.remove(id)
    }

    fun insertIntoDb() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_FORUM).use { ps ->
                    ps.setInt(1, id)
                    ps.setString(2, _forumName)
                    ps.setInt(3, _parent?.id ?: 0)
                    ps.setInt(4, _forumPost)
                    ps.setInt(5, _forumType)
                    ps.setInt(6, _forumPerm)
                    ps.setInt(7, _ownerId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save new Forum.", e)
        }

    }

    fun vload() {
        if (!_loaded) {
            load()
            getChildren()

            _loaded = true
        }
    }

    companion object {
        private val LOGGER = CLogger(Forum::class.java.name)

        private const val RESTORE_FORUMS = "SELECT * FROM forums WHERE forum_id=?"
        private const val RESTORE_TOPICS = "SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC"
        private const val RESTORE_CHILDREN = "SELECT forum_id FROM forums WHERE forum_parent=?"
        private const val ADD_FORUM =
            "INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)"

        // Types
        const val ROOT = 0
        const val NORMAL = 1
        const val CLAN = 2
        const val MEMO = 3
        const val MAIL = 4

        // Permissions
        const val INVISIBLE = 0
        const val ALL = 1
        const val CLANMEMBERONLY = 2
        const val OWNERONLY = 3
    }
}