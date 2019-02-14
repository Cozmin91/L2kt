package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.RecipeData.forEach
import com.l2kt.gameserver.model.item.Recipe
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [Recipe]s. Recipes are part of craft system, which uses a Recipe associated to items (materials) to craft another item (product).
 */
object RecipeData : IXmlReader {
    private val _recipes = HashMap<Int, Recipe>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/recipes.xml")
        IXmlReader.LOGGER.info("Loaded {} recipes.", _recipes.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "recipe") { recipeNode ->
                val set = parseAttributes(recipeNode)
                _recipes[set.getInteger("id")] = Recipe(set)
            }
        }
    }

    fun getRecipeList(listId: Int): Recipe? {
        return _recipes[listId]
    }

    fun getRecipeByItemId(itemId: Int): Recipe? {
        return _recipes.values.firstOrNull { r -> r.recipeId == itemId }
    }
}