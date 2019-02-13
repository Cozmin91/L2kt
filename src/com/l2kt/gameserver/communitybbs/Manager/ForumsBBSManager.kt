package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.communitybbs.BB.Forum
import java.util.concurrent.ConcurrentHashMap

object ForumsBBSManager : BaseBBSManager() {

    private val _forums = ConcurrentHashMap.newKeySet<Forum>()

    private var _lastId = 1

    val aNewID: Int
        get() = ++_lastId

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_FORUMS).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            addForum(Forum(rs.getInt("forum_id"), null))
                    }
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't load forums root.", e)
        }

    }

    fun initRoot() {
        for (forum in _forums)
            forum.vload()

        BaseBBSManager.LOGGER.info("Loaded {} forums.", _forums.size)
    }

    fun addForum(forum: Forum?) {
        if (forum == null)
            return

        _forums.add(forum)

        if (forum.id > _lastId)
            _lastId = forum.id
    }

    fun createNewForum(name: String, parent: Forum, type: Int, perm: Int, oid: Int): Forum {
        val forum = Forum(name, parent, type, perm, oid)
        forum.insertIntoDb()

        return forum
    }

    fun getForumByName(name: String): Forum? {
        return _forums.stream().filter { f -> f.name == name }.findFirst().orElse(null)
    }

    fun getForumByID(id: Int): Forum? {
        return _forums.stream().filter { f -> f.id == id }.findFirst().orElse(null)
    }

    private const val LOAD_FORUMS = "SELECT forum_id FROM forums WHERE forum_type=0"
}