package com.l2kt.gameserver.data.xml

import com.l2kt.Config
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.SpellbookData.forEach
import com.l2kt.gameserver.model.L2Skill
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores spellbook / skillId relation.<br></br>
 * TODO Could be possibly moved back on skillTrees.
 */
object SpellbookData : IXmlReader {
    private val _books = HashMap<Int, Int>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/spellbooks.xml")
        IXmlReader.LOGGER.info("Loaded {} spellbooks.", _books.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "book") { bookNode ->
                val attrs = bookNode.attributes
                _books[parseInteger(attrs, "skillId")] = parseInteger(attrs, "itemId")
            }
        }
    }

    fun getBookForSkill(skillId: Int, level: Int): Int {
        if (skillId == L2Skill.SKILL_DIVINE_INSPIRATION) {
            if (!Config.DIVINE_SP_BOOK_NEEDED)
                return 0

            return when (level) {
                1 -> 8618 // Ancient Book - Divine Inspiration (Modern Language Version)
                2 -> 8619 // Ancient Book - Divine Inspiration (Original Language Version)
                3 -> 8620 // Ancient Book - Divine Inspiration (Manuscript)
                4 -> 8621 // Ancient Book - Divine Inspiration (Original Version)
                else -> 0
            }
        }

        if (level != 1)
            return 0

        if (!Config.SP_BOOK_NEEDED)
            return 0

        return if (!_books.containsKey(skillId)) 0 else _books[skillId]!!

    }
}