package com.williambl.numen.gods

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class GodArgumentType private constructor() : ArgumentType<God> {
    @Throws(CommandSyntaxException::class)
    override fun parse(stringReader: StringReader): God {
        val id = Identifier.fromCommandInput(stringReader)
        return Gods.REGISTRY.get(id) ?: throw INVALID_ABILITY_EXCEPTION.create(id)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(Gods.REGISTRY.ids.map(Identifier::toString), builder)
    }

    override fun getExamples(): Collection<String> {
        return EXAMPLES
    }

    companion object {
        private val EXAMPLES: Collection<String> = listOf("numen:agricultural", "numen:oceanic")
        val INVALID_ABILITY_EXCEPTION = DynamicCommandExceptionType { LiteralText("Invalid god: $it") }

        fun ability(): GodArgumentType {
            return GodArgumentType()
        }

        fun getAbility(context: CommandContext<ServerCommandSource?>, name: String?): God {
            return context.getArgument(
                name,
                God::class.java
            ) as God
        }
    }

    object Serialiser: ArgumentSerializer<GodArgumentType> {
        override fun toPacket(argumentType: GodArgumentType, packetByteBuf: PacketByteBuf) = Unit

        override fun fromPacket(packetByteBuf: PacketByteBuf): GodArgumentType = GodArgumentType()

        override fun toJson(argumentType: GodArgumentType, jsonObject: JsonObject) = Unit
    }
}