package com.williambl.numen.spells

import com.williambl.numen.gods.God
import com.williambl.numen.id
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.util.registry.Registry

object Spells {
    val REGISTRY = FabricRegistryBuilder.createSimple(Spell::class.java, id("spells")).buildAndRegister()

    fun init() {}
}