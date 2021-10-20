package com.williambl.numen

import com.williambl.numen.gods.Gods
import com.williambl.numen.spells.Spells
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder

val numenGroup = FabricItemGroupBuilder.build(id("numen")) { Spells.WRITABLE_TABLET.defaultStack }

fun init() {
    Gods.init()
    Spells.init()
}
