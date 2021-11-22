package com.dis.logolink.models

open class NotGate(position: Position,
                   inputList: MutableList<Component>,
                   name: String,
) : Component(position, inputList, name) {
    override fun setResult(): Boolean {
        return inputList[0].setResult().not()
    }
}