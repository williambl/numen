package com.williambl.numen.gods.component

import com.williambl.numen.gods.God
import com.williambl.numen.id
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent

interface GodFavourComponent: ServerTickingComponent, MutableMap<God, Double> {
    fun addFavour(god: God, amount: Double) = this.compute(god) { _, v -> (v ?: 0.0)+amount }
    fun modifyFavour(god: God, modifier: (Double) -> Double) = this.compute(god) { _, v -> modifier(v ?: 0.0) }

    companion object {
        val KEY = ComponentRegistry.getOrCreate(id("god_favour"), GodFavourComponent::class.java)
    }
}