package com.williambl.numen.spells.component

import com.williambl.numen.gods.God
import com.williambl.numen.gods.component.GodFavourComponent
import com.williambl.numen.id
import com.williambl.numen.spells.Spell
import com.williambl.numen.spells.SpellData
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent

interface AttachedSpellsComponent: CommonTickingComponent, AutoSyncedComponent, MutableList<SpellData> {
    companion object {
        val KEY = ComponentRegistry.getOrCreate(id("attached_spells"), AttachedSpellsComponent::class.java)
    }
}