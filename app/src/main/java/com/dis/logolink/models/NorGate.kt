package com.dis.logolink.models

class NorGate (position: Position,
               inputList: MutableList<Component>,
               name: String
) : Component(position, inputList, name) {


    override fun setResult(): Boolean {
        val list: MutableList<Component> = inputList
        var result = false
        val itr = list.iterator()
        while (itr.hasNext()) {
            result = result.or(itr.next().setResult())
        }
        return !result
    }
}