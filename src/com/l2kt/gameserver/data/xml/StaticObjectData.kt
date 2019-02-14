package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.StaticObjectData.forEach
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.instance.StaticObject
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads, stores and spawns [StaticObject]s.
 */
object StaticObjectData : IXmlReader {
    private val _objects = HashMap<Int, StaticObject>()

    val staticObjects: Collection<StaticObject>
        get() = _objects.values

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/staticObjects.xml")
        IXmlReader.LOGGER.info("Loaded {} static objects.", _objects.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "object") { objectNode ->
                val set = parseAttributes(objectNode)
                val obj = StaticObject(IdFactory.getInstance().nextId)
                obj.staticObjectId = set.getInteger("id")
                obj.type = set.getInteger("type")
                obj.setMap(set.getString("texture"), set.getInteger("mapX"), set.getInteger("mapY"))
                obj.spawnMe(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"))
                _objects[obj.objectId] = obj
            }
        }
    }
}