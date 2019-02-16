package com.l2kt.gameserver.model.olympiad

/**
 * @author DS
 */
enum class CompetitionType constructor(private val _name: String) {
    CLASSED("classed"),
    NON_CLASSED("non-classed");

    override fun toString(): String {
        return _name
    }
}