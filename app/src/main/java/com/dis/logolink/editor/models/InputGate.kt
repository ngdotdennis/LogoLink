package com.dis.logolink.editor.models

open class InputGate(inputList: MutableList<Component>) : Component(inputList)  {

    override fun setResult(): Boolean {
        return result
    }

    override fun not(): Component {
        result = !result
        return this
    }
}