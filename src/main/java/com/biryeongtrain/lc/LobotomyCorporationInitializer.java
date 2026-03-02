package com.biryeongtrain.lc;

import com.biryeongtrain.lc.command.LobotomyCommands;
import com.biryeongtrain.lc.entity.ContainmentAbnormalityEntity;
import com.biryeongtrain.lc.entity.LCEntities;
import com.biryeongtrain.lc.game.containment.ContainmentChamberManager;
import com.biryeongtrain.lc.game.manager.GameManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobotomyCorporationInitializer implements ModInitializer {
	public static final String MOD_ID = "lobotomy_corporation";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Lobotomy Corporation gameplay systems");
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		LCEntities.register();
		ContainmentAbnormalityEntity.preloadCatalogModels();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				LobotomyCommands.register(dispatcher)
		);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				GameManager.getInstance().onPlayerJoin(handler.player)
		);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity player) {
				GameManager.getInstance().onPlayerDeath(player.getServer(), player);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GameManager.getInstance().reset();
			ContainmentChamberManager.clear();
		});
	}
}
