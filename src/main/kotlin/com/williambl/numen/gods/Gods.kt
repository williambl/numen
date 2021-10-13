package com.williambl.numen.gods

import com.williambl.numen.id
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder

object Gods {
    val REGISTRY = FabricRegistryBuilder.createSimple(God::class.java, id("gods")).buildAndRegister()
}