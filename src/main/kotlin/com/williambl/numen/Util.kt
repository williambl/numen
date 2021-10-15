package com.williambl.numen

import net.minecraft.SharedConstants
import net.minecraft.util.Identifier

fun id(path: String) = Identifier("numen", path)

fun decay(value: Double, divideByInDay: Double) = value + ((value/divideByInDay)-value)/(SharedConstants.TICKS_PER_IN_GAME_DAY.toDouble())
