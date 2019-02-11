package com.l2kt.gameserver.xmlfactory

import java.io.File

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document

object XMLDocumentFactory {

    private val builder: DocumentBuilder

    init {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        factory.isIgnoringComments = true
        builder = factory.newDocumentBuilder()
    }

    fun loadDocument(filePath: String): Document {
        return loadDocument(File(filePath))
    }

    fun loadDocument(file: File): Document {
        if (!file.exists() || !file.isFile)
            throw IllegalArgumentException("File: ${file.absolutePath} doesn't exist and/or is not a file.")

        return builder.parse(file)
    }

    fun newDocument(): Document {
        return builder.newDocument()
    }
}