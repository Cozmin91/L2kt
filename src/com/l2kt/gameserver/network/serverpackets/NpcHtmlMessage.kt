package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.cache.HtmCache

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes VOLUMN UNKNOWN UL U TT TR TITLE TEXTCODE TEXTAREA TD TABLE SUP SUB STRIKE SPIN SELECT RIGHT PRE P OPTION OL MULTIEDIT LI LEFT INPUT IMG I HTML H7 H6 H5 H4 H3 H2 H1 FONT EXTEND EDIT COMMENT COMBOBOX CENTER
 * BUTTON BR BODY BAR ADDRESS A SEL LIST VAR FORE READONL ROWS VALIGN FIXWIDTH BORDERCOLORLI BORDERCOLORDA BORDERCOLOR BORDER BGCOLOR BACKGROUND ALIGN VALU READONLY MULTIPLE SELECTED TYP TYPE MAXLENGTH CHECKED SRC Y X QUERYDELAY NOSCROLLBAR IMGSRC B FG SIZE FACE COLOR DEFFON DEFFIXEDFONT WIDTH VALUE
 * TOOLTIP NAME MIN MAX HEIGHT DISABLED ALIGN MSG LINK HREF ACTION
 */
class NpcHtmlMessage(private val _npcObjId: Int) : L2GameServerPacket() {
    private var _html: String? = null
    private var _itemId = 0
    private var _validate = true

    override fun runImpl() {
        if (!_validate)
            return

        val activeChar = client.activeChar ?: return

        activeChar.clearBypass()
        var i = 0
        while (i < _html!!.length) {
            var start = _html!!.indexOf("\"bypass ", i)
            val finish = _html!!.indexOf("\"", start + 1)
            if (start < 0 || finish < 0)
                break

            if (_html!!.substring(start + 8, start + 10) == "-h")
                start += 11
            else
                start += 8

            i = finish
            val finish2 = _html!!.indexOf("$", start)
            if (finish2 < finish && finish2 > 0)
                activeChar.addBypass2(_html!!.substring(start, finish2).trim { it <= ' ' })
            else
                activeChar.addBypass(_html!!.substring(start, finish).trim { it <= ' ' })
            i++
        }
    }

    override fun writeImpl() {
        writeC(0x0f)

        writeD(_npcObjId)
        writeS(_html)
        writeD(_itemId)
    }

    fun disableValidation() {
        _validate = false
    }

    fun setItemId(itemId: Int) {
        _itemId = itemId
    }

    fun setHtml(text: String) {
        if (text.length > 8192) {
            _html = "<html><body>Html was too long.</body></html>"
            return
        }
        _html = text
    }

    fun setFile(filename: String) {
        setHtml(HtmCache.getInstance().getHtmForce(filename))
    }

    fun basicReplace(pattern: String, value: String) {
        _html = _html!!.replace(pattern.toRegex(), value)
    }

    fun replace(pattern: String, value: String) {
        _html = _html!!.replace(pattern.toRegex(), value.replace("\\$".toRegex(), "\\\\\\$"))
    }

    fun replace(pattern: String, value: Int) {
        _html = _html!!.replace(pattern.toRegex(), Integer.toString(value))
    }

    fun replace(pattern: String, value: Long) {
        _html = _html!!.replace(pattern.toRegex(), java.lang.Long.toString(value))
    }

    fun replace(pattern: String, value: Double) {
        _html = _html!!.replace(pattern.toRegex(), java.lang.Double.toString(value))
    }
}