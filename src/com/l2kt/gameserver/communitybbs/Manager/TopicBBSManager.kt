package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.communitybbs.BB.Forum
import com.l2kt.gameserver.communitybbs.BB.Post
import com.l2kt.gameserver.communitybbs.BB.Topic
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.actor.instance.Player

import java.text.DateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TopicBBSManager : BaseBBSManager() {
    private val _topics = ArrayList<Topic>()
    private val _maxId = ConcurrentHashMap<Forum, Int>()

    override fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        when (ar1) {
            "crea" -> {
                val forum = ForumsBBSManager.getForumByID(Integer.parseInt(ar2))
                if (forum == null) {
                    BaseBBSManager.separateAndSend(
                        "<html><body><br><br><center>The forum named '$ar2' doesn't exist.</center></body></html>",
                        player
                    )
                    return
                }

                forum.vload()
                val topic = Topic(
                    Topic.ConstructorType.CREATE,
                    TopicBBSManager.getMaxID(forum) + 1,
                    Integer.parseInt(ar2),
                    ar5,
                    Calendar.getInstance().timeInMillis,
                    player.name,
                    player.objectId,
                    Topic.MEMO,
                    0
                )
                forum.addTopic(topic)
                TopicBBSManager.setMaxID(topic.id, forum)

                val post = Post(player.name, player.objectId, Calendar.getInstance().timeInMillis, topic.id, forum.id, ar4)
                PostBBSManager.addPostByTopic(post, topic)

                parseCmd("_bbsmemo", player)
            }
            "del" -> {
                val forum = ForumsBBSManager.getForumByID(Integer.parseInt(ar2))
                if (forum == null) {
                    BaseBBSManager.separateAndSend(
                        "<html><body><br><br><center>The forum named '$ar2' doesn't exist.</center></body></html>",
                        player
                    )
                    return
                }

                val topic = forum.getTopic(Integer.parseInt(ar3))
                if (topic == null) {
                    BaseBBSManager.separateAndSend(
                        "<html><body><br><br><center>The topic named '$ar3' doesn't exist.</center></body></html>",
                        player
                    )
                    return
                }

                val post = PostBBSManager.getPostByTopic(topic)
                post?.deleteMe(topic)

                topic.deleteMe(forum)
                parseCmd("_bbsmemo", player)
            }
            else -> super.parseWrite(ar1, ar2, ar3, ar4, ar5, player)
        }
    }

    override fun parseCmd(command: String, player: Player) {
        if (command == "_bbsmemo") {
            val forum = player.memo
            if (forum != null)
                showTopics(forum, player, 1, forum.id)
        } else if (command.startsWith("_bbstopics;read")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()
            st.nextToken()

            val forumId = Integer.parseInt(st.nextToken())
            val index = if (st.hasMoreTokens()) st.nextToken() else null
            val ind = if (index == null) 1 else Integer.parseInt(index)

            showTopics(ForumsBBSManager.getForumByID(forumId), player, ind, forumId)
        } else if (command.startsWith("_bbstopics;crea")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()
            st.nextToken()

            val forumId = Integer.parseInt(st.nextToken())

            showNewTopic(ForumsBBSManager.getForumByID(forumId), player, forumId)
        } else if (command.startsWith("_bbstopics;del")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()
            st.nextToken()

            val forumId = Integer.parseInt(st.nextToken())
            val topicId = Integer.parseInt(st.nextToken())

            val forum = ForumsBBSManager.getForumByID(forumId)
            if (forum == null) {
                BaseBBSManager.separateAndSend(
                    "<html><body><br><br><center>The forum named '$forumId' doesn't exist.</center></body></html>",
                    player
                )
                return
            }

            val topic = forum.getTopic(topicId)
            if (topic == null) {
                BaseBBSManager.separateAndSend(
                    "<html><body><br><br><center>The topic named '$topicId' doesn't exist.</center></body></html>",
                    player
                )
                return
            }

            val post = PostBBSManager.getPostByTopic(topic)
            post?.deleteMe(topic)

            topic.deleteMe(forum)
            parseCmd("_bbsmemo", player)
        } else
            super.parseCmd(command, player)
    }

    fun addTopic(topic: Topic) {
        _topics.add(topic)
    }

    fun deleteTopic(topic: Topic) {
        _topics.remove(topic)
    }

    fun setMaxID(id: Int, forum: Forum) {
        _maxId[forum] = id
    }

    fun getMaxID(forum: Forum): Int {
        return (_maxId).getOrDefault(forum, 0)
    }

    fun getTopicById(forumId: Int): Topic? {
        return _topics.firstOrNull { t -> t.id == forumId }
    }

    private fun showTopics(forum: Forum?, player: Player, index: Int, forumId: Int) {
        if (forum == null) {
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The forum named '$forumId' doesn't exist.</center></body></html>",
                player
            )
            return
        }

        if (forum.type == Forum.MEMO)
            showMemoTopics(forum, player, index)
        else
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The forum named '" + forum.name + "' doesn't exist.</center></body></html>",
                player
            )
    }

    private fun showMemoTopics(forum: Forum, player: Player, index: Int) {
        forum.vload()
        val sb =
            StringBuilder("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415 align=center>&$413;</td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>&$418;</td></tr></table>")

        val dateFormat = DateFormat.getInstance()
        run {
            var i = 0
            var j = getMaxID(forum) + 1
            while (i < 12 * index) {
                if (j < 0)
                    break

                val topic = forum.getTopic(j)
                if (topic != null) {
                    if (i++ >= 12 * (index - 1))
                        StringUtil.append(
                            sb,
                            "<table border=0 cellspacing=0 cellpadding=5 WIDTH=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415><a action=\"bypass _bbsposts;read;",
                            forum.id,
                            ";",
                            topic.id,
                            "\">",
                            topic.name,
                            "</a></td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>",
                            dateFormat.format(Date(topic.date)),
                            "</td></tr></table><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">"
                        )
                }
                j--
            }
        }

        sb.append("<br><table width=610 cellspace=0 cellpadding=0><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=510 align=center><table border=0><tr>")

        if (index == 1)
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>")
        else
            StringUtil.append(
                sb,
                "<td><button action=\"bypass _bbstopics;read;",
                forum.id,
                ";",
                index - 1,
                "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>"
            )

        var pageNumber: Int = forum.topicSize / 8
        if (pageNumber * 8 != ClanTable.getInstance().clans.size)
            pageNumber++

        for (i in 1..pageNumber) {
            if (i == index)
                StringUtil.append(sb, "<td> ", i, " </td>")
            else
                StringUtil.append(
                    sb,
                    "<td><a action=\"bypass _bbstopics;read;",
                    forum.id,
                    ";",
                    i,
                    "\"> ",
                    i,
                    " </a></td>"
                )
        }

        if (index == pageNumber)
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>")
        else
            StringUtil.append(
                sb,
                "<td><button action=\"bypass _bbstopics;read;",
                forum.id,
                ";",
                index + 1,
                "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>"
            )

        StringUtil.append(
            sb,
            "</tr></table></td><td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;",
            forum.id,
            "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td></td><td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td><td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table></td></tr></table><br><br><br></center></body></html>"
        )
        BaseBBSManager.separateAndSend(sb.toString(), player)
    }

    private fun showNewTopic(forum: Forum?, player: Player, forumId: Int) {
        if (forum == null) {
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The forum named '$forumId' doesn't exist.</center></body></html>",
                player
            )
            return
        }

        if (forum.type == Forum.MEMO)
            showMemoNewTopics(forum, player)
        else
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The forum named '" + forum.name + "' doesn't exist.</center></body></html>",
                player
            )
    }

    private fun showMemoNewTopics(forum: Forum, player: Player) {
        val html =
            "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.id + " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>"
        BaseBBSManager.send1001(html, player)
        BaseBBSManager.send1002(player)
    }
}