package com.github.quiltservertools.ledger.listeners

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.TntEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import com.github.quiltservertools.ledger.actionutils.ActionFactory
import com.github.quiltservertools.ledger.callbacks.BlockBurnCallback
import com.github.quiltservertools.ledger.callbacks.BlockExplodeCallback
import com.github.quiltservertools.ledger.callbacks.BlockFallCallback
import com.github.quiltservertools.ledger.callbacks.BlockLandCallback
import com.github.quiltservertools.ledger.database.DatabaseManager


fun registerBlockListeners() {
    BlockExplodeCallback.EVENT.register(::onExplode)
    BlockBurnCallback.EVENT.register(::onBurn)
    BlockFallCallback.EVENT.register(::onFall)
    BlockLandCallback.EVENT.register(::onLand)
}

private fun onLand(world: World, pos: BlockPos, state: BlockState) {
    DatabaseManager.logAction(
        ActionFactory.blockPlaceAction(world, pos, state, "gravity")
    )
}

private fun onFall(world: World, pos: BlockPos, state: BlockState) {
    DatabaseManager.logAction(
        ActionFactory.blockBreakAction(
            world,
            pos,
            state,
            "gravity"
        )
    )
}

private fun onBurn(world: World, pos: BlockPos, state: BlockState, entity: BlockEntity?) {
    DatabaseManager.logAction(
        ActionFactory.blockBreakAction(
            world,
            pos,
            state,
            "fire",
            entity
        )
    )
}

private fun onExplode(
    world: World,
    source: Entity?,
    blockPos: BlockPos,
    blockState: BlockState,
    entity: BlockEntity?
) {
    val sourceName = source?.let { Registry.ENTITY_TYPE.getId(it.type).path } ?: "explosion"

    val action = ActionFactory.blockBreakAction(
        world,
        blockPos,
        blockState,
        sourceName,
        entity
    )

    if (source is TntEntity && source.causingEntity is ServerPlayerEntity) {
        action.sourceProfile = (source.causingEntity as ServerPlayerEntity).gameProfile
    }

    DatabaseManager.logAction(action)
}
