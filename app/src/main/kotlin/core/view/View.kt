package com.foam.app.core.view

import com.foam.app.core.node.Node

abstract class View<T : View<T>> : Node() {

    @Suppress("UNCHECKED_CAST")
    protected fun self(): T =
        this as T


    fun className(
        value: String
    ): T {

        classes += value

        return self()
    }


    fun frame(
        width: Float? = null,
        height: Float? = null
    ): T {

        inlineStyle.width = width
        inlineStyle.height = height

        return self()
    }


    fun padding(
        all: Float
    ): T {

        inlineStyle.paddingTop = all
        inlineStyle.paddingRight = all
        inlineStyle.paddingBottom = all
        inlineStyle.paddingLeft = all

        return self()
    }


    fun margin(
        all: Float
    ): T {

        inlineStyle.marginTop = all
        inlineStyle.marginRight = all
        inlineStyle.marginBottom = all
        inlineStyle.marginLeft = all

        return self()
    }
}