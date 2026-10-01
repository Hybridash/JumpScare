package com.hybridash.jumpscare;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side: once per second, every player has a 1 in 100,000 chance of dropping dead.
 * (The jumpscare roll happens on the client, see JumpScareClient.)
 */
public class JumpScare implements ModInitializer {
	public static final String MOD_ID = "jumpscare";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** 1 in this many chance, rolled once per second. */
	public static final int DEATH_ODDS = 100_000;
	/** 1 in this many chance, rolled once per second. */
	public static final int JUMPSCARE_ODDS = 10_000;

	/** Custom damage type so the death message says what happened. Defined in data/jumpscare/damage_type. */
	public static final RegistryKey<DamageType> UNLUCKY =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "unlucky"));

	private static final Random RANDOM = Random.create();
	private int ticks = 0;

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		// /unlucky - ops can test the death without waiting ~28 hours
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("unlucky")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
							killUnlucky(player);
							return 1;
						})));

		LOGGER.info("JumpScare loaded. Good luck.");
	}

	private void onServerTick(MinecraftServer server) {
		if (++ticks < 20) return; // 20 ticks = 1 second
		ticks = 0;

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.isSpectator() || !player.isAlive()) continue;
			if (RANDOM.nextInt(DEATH_ODDS) == 0) {
				killUnlucky(player);
			}
		}
	}

	public static void killUnlucky(ServerPlayerEntity player) {
		DamageSource source = new DamageSource(
				player.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(UNLUCKY));
		LOGGER.info("{} rolled the 1 in {} and died.", player.getName().getString(), DEATH_ODDS);
		player.damage(source, Float.MAX_VALUE);
	}
}
