package com.l2kt.gameserver.model.multisell

/**
 * A datatype which is part of multisell system. A multisell list can hold multiple Products.<br></br>
 * Each Product owns a List of "required part(s)" and "result(s)" known both as [Ingredient]s.
 */
open class Entry {
    var ingredients: MutableList<Ingredient> = mutableListOf()
        protected set
    var products: MutableList<Ingredient> = mutableListOf()
        protected set
    var isStackable = true
        protected set

    open var taxAmount: Int = 0
        get() = 0
        protected set

    constructor(ingredients: MutableList<Ingredient>, products: MutableList<Ingredient>) {
        this.ingredients = ingredients
        this.products = products
        isStackable = products.stream().allMatch { it.isStackable }
    }

    /**
     * This constructor used in PreparedEntry only, ArrayLists not created.
     */
    protected constructor() {}
}