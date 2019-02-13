package com.l2kt.gameserver.communitybbs.BB

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.communitybbs.Manager.PostBBSManager
import java.util.*

class Post {

    private val _posts = ArrayList<CPost>()

    constructor(topic: Topic) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_POSTS).use { ps ->
                    ps.setInt(1, topic.forumID)
                    ps.setInt(2, topic.id)

                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            _posts.add(
                                CPost(
                                    rs.getInt("post_id"),
                                    rs.getString("post_owner_name"),
                                    rs.getInt("post_ownerid"),
                                    rs.getLong("post_date"),
                                    rs.getInt("post_topic_id"),
                                    rs.getInt("post_forum_id"),
                                    rs.getString("post_txt")
                                )
                            )
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load posts for {} / {}.", e, topic.forumID, topic.id)
        }

    }

    constructor(owner: String, ownerId: Int, date: Long, topicId: Int, forumId: Int, text: String) {
        // Create a new CPost object.
        val post = CPost(0, owner, ownerId, date, topicId, forumId, text)

        // Add it to the container.
        _posts.add(post)

        // Insert it to database.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_POST).use { ps ->
                    ps.setInt(1, 0)
                    ps.setString(2, owner)
                    ps.setInt(3, ownerId)
                    ps.setLong(4, date)
                    ps.setInt(5, topicId)
                    ps.setInt(6, forumId)
                    ps.setString(7, text)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save new Post.", e)
        }

    }

    fun getCPost(id: Int): CPost? {
        for ((i, cp) in _posts.withIndex()) {
            if (i == id)
                return cp
        }
        return null
    }

    fun deleteMe(topic: Topic) {
        PostBBSManager.deletePostByTopic(topic)
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_POST).use { ps ->
                    ps.setInt(1, topic.forumID)
                    ps.setInt(2, topic.id)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete Post.", e)
        }

    }

    fun updateText(index: Int) {
        val post = getCPost(index) ?: return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_TEXT).use { ps ->
                    ps.setString(1, post.text)
                    ps.setInt(2, post.id)
                    ps.setInt(3, post.topicId)
                    ps.setInt(4, post.forumId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update Post text.", e)
        }

    }

    inner class CPost(
        val id: Int,
        val owner: String,
        val ownerId: Int,
        val date: Long,
        val topicId: Int,
        val forumId: Int,
        var text: String?
    )

    companion object {
        private val LOGGER = CLogger(Post::class.java.name)

        private const val RESTORE_POSTS = "SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC"
        private const val ADD_POST =
            "INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)"
        private const val DELETE_POST = "DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?"
        private const val UPDATE_TEXT = "UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?"
    }
}