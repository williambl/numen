package com.williambl.numen

import com.williambl.numen.gods.sacrifice.ChthonicEnvironmentEvaluator
import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvalutator
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

fun init() {
    CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
        dispatcher.register(CommandManager.literal("evaluate_naturalness").executes { ctx ->
            val pos = BlockPos(ctx.source.position)
            val world = ctx.source.world
            ctx.source.sendFeedback(LiteralText(NatureEnvironmentEvalutator.evaluate(world, pos, 25, 3).toString()), false)
            return@executes 0;
        })
        dispatcher.register(CommandManager.literal("evaluate_chthonicness").executes { ctx ->
            val pos = BlockPos(ctx.source.position)
            val world = ctx.source.world
            ctx.source.sendFeedback(LiteralText(ChthonicEnvironmentEvaluator.evaluate(world, pos, 25, 3).toString()), false)
            return@executes 0;
        })
    }
}

fun id(path: String) = Identifier("numen", path)