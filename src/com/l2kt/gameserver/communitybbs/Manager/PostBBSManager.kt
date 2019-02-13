package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.gameserver.communitybbs.BB.Forum
import com.l2kt.gameserver.communitybbs.BB.Post
import com.l2kt.gameserver.communitybbs.BB.Topic
import com.l2kt.gameserver.model.actor.instance.Player

import java.text.DateFormat
import java.util.*

object PostBBSManager : BaseBBSManager() {
    private val _postByTopic = HashMap<Topic, Post>()

    override fun parseCmd(command: String, player: Player) {
        when {
            command.startsWith("_bbsposts;read;") -> {
                val st = StringTokenizer(command, ";")
                st.nextToken()
                st.nextToken()

                val forumId = Integer.parseInt(st.nextToken())
                val pageNumber = Integer.parseInt(st.nextToken())
                val index = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 1

                showPost(
                    TopicBBSManager.getTopicById(pageNumber),
                    ForumsBBSManager.getForumByID(forumId),
                    player,
                    index
                )
            }
            command.startsWith("_bbsposts;edit;") -> {
                val st = StringTokenizer(command, ";")
                st.nextToken()
                st.nextToken()

                val forumId = Integer.parseInt(st.nextToken())
                val topicId = Integer.parseInt(st.nextToken())
                val pageId = Integer.parseInt(st.nextToken())

                showEditPost(
                    TopicBBSManager.getTopicById(topicId),
                    ForumsBBSManager.getForumByID(forumId),
                    player,
                    pageId
                )
            }
            else -> super.parseCmd(command, player)
        }
    }

    override fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        val st = StringTokenizer(ar1, ";")

        val forumId = Integer.parseInt(st.nextToken())
        val topicId = Integer.parseInt(st.nextToken())
        val pageId = Integer.parseInt(st.nextToken())

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

        val post = getPostByTopic(topic)
        if (post.getCPost(pageId) == null) {
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The post named '$pageId' doesn't exist.</center></body></html>",
                player
            )
            return
        }

        post.getCPost(pageId)!!.text = ar4
        post.updateText(pageId)

        parseCmd("_bbsposts;read;" + forum.id + ";" + topic.id, player)
    }

    fun getPostByTopic(topic: Topic): Post {
        var post: Post? = _postByTopic[topic]
        if (post == null) {
            post = load(topic)
            _postByTopic[topic] = post
        }
        return post
    }

    fun deletePostByTopic(topic: Topic) {
        _postByTopic.remove(topic)
    }

    fun addPostByTopic(post: Post, topic: Topic) {
        if (_postByTopic[topic] == null)
            _postByTopic[topic] = post
    }

    private fun showEditPost(topic: Topic?, forum: Forum?, player: Player, pageNumber: Int) {
        if (forum == null || topic == null) {
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>This forum and/or topic don't exit.</center></body></html>",
                player
            )
            return
        }

        val post = getPostByTopic(topic)
        if (post == null) {
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>This post doesn't exist.</center></body></html>",
                player
            )
            return
        }

        showHtmlEditPost(topic, player, forum, post)
    }

    private fun showPost(topic: Topic?, forum: Forum?, player: Player, ind: Int) {
        if (forum == null || topic == null)
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>This forum and/or topic don't exist.</center></body></html>",
                player
            )
        else if (forum.type == Forum.MEMO)
            showMemoPost(topic, player, forum)
        else
            BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The forum named '" + forum.name + "' isn't implemented.</center></body></html>",
                player
            )
    }

    private fun showMemoPost(topic: Topic, player: Player, forum: Forum) {
        val post = getPostByTopic(topic)
        val locale = Locale.getDefault()
        val dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale)

        var mes = post.getCPost(0)!!.text!!.replace(">", "&gt;")
        mes = mes.replace("<", "&lt;")
        mes = mes.replace("\n", "<br1>")

        val html =
            "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0 bgcolor=333333><tr><td height=10></td></tr><tr><td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td><td fixWIDTH=380 valign=top>" + topic.name + "</td><td fixwidth=5></td><td fixwidth=50></td><td fixWIDTH=120></td></tr><tr><td height=10></td></tr><tr><td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td><td><font color=\"AAAAAA\">" + topic.ownerName + "</font></td><td></td><td><font color=\"AAAAAA\">&$418; :</font></td><td><font color=\"AAAAAA\">" + dateFormat.format(
                post.getCPost(0)!!.date
            ) + "</font></td></tr><tr><td height=10></td></tr></table><br><table border=0 cellspacing=0 cellpadding=0><tr><td fixwidth=5></td><td FIXWIDTH=600 align=left>" + mes + "</td><td fixqqwidth=5></td></tr></table><br><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><img src=\"L2UI.squaregray\" width=\"610\" height=\"1\"><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=560 align=right><table border=0 cellspacing=0><tr><td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;" + forum.id + ";" + topic.id + ";0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;" + forum.id + ";" + topic.id + "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;" + forum.id + "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;</tr></table></td></tr></table><br><br><br></center></body></html>"
        BaseBBSManager.separateAndSend(html, player)
    }

    private fun load(topic: Topic): Post {
        return Post(topic)
    }

    private fun showHtmlEditPost(topic: Topic, player: Player, forum: Forum, post: Post) {
        val html =
            "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">" + forum.name + " Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540>" + topic.name + "</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post " + forum.id + ";" + topic.id + ";0 _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>"
        BaseBBSManager.send1001(html, player)
        BaseBBSManager.send1002(
            player,
            post.getCPost(0)?.text ?: "",
            topic.name,
            DateFormat.getInstance().format(Date(topic.date))
        )
    }
}