package com.hybridash.jumpscare.client;

import com.hybridash.jumpscare.JumpScare;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

/**
 * Client side: once per second there's a 1 in 10,000 chance a face fills the screen and screams.
 */
public class JumpScareClient implements ClientModInitializer {
	private static final Identifier FACE = Identifier.of(JumpScare.MOD_ID, "textures/gui/face.png");
	private static final SoundEvent SCREAM = SoundEvent.of(Identifier.of(JumpScare.MOD_ID, "scream"));

	/** How long the scare stays on screen, in milliseconds. */
	private static final long DURATION_MS = 1600;

	private static final Random RANDOM = Random.create();
	private static long scareStart = -1;
	private int ticks = 0;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		HudRenderCallback.EVENT.register(JumpScareClient::render);

		// /jumpscare - trigger it on demand (for testing, or for scaring a friend over your shoulder)
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("jumpscare").executes(context -> {
					trigger();
					return 1;
				})));
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player == null || client.isPaused()) return;
		if (++ticks < 20) return; // 20 ticks = 1 second
		ticks = 0;

		if (RANDOM.nextInt(JumpScare.JUMPSCARE_ODDS) == 0) {
			trigger();
		}
	}

	public static void trigger() {
		MinecraftClient client = MinecraftClient.getInstance();
		scareStart = System.currentTimeMillis();
		client.getSoundManager().play(PositionedSoundInstance.master(SCREAM, 1.0f, 1.0f));
	}

	private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		if (scareStart < 0) return;
		long elapsed = System.currentTimeMillis() - scareStart;
		if (elapsed > DURATION_MS) {
			scareStart = -1;
			return;
		}

		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();

		// Black out the game
		ctx.fill(0, 0, w, h, 0xFF000000);

		// The face lunges at you over the first 150ms, then keeps creeping closer
		float lunge = Math.min(1f, elapsed / 150f);
		float zoom = 0.4f + 0.9f * lunge + 0.15f * (elapsed / (float) DURATION_MS);
		int size = (int) (Math.max(w, h) * zoom);

		// Violent shake
		int shake = Math.max(2, size / 60);
		int x = (w - size) / 2 + RANDOM.nextBetween(-shake, shake);
		int y = (h - size) / 2 + RANDOM.nextBetween(-shake, shake);

		RenderSystem.enableBlend();
		ctx.drawTexture(FACE, x, y, size, size, 0, 0, 256, 256, 256, 256);

		// Strobing red flash
		if ((elapsed / 70) % 2 == 0) {
			ctx.fill(0, 0, w, h, 0x66FF0000);
		}

		// Fade to black right before it vanishes
		long fadeStart = DURATION_MS - 300;
		if (elapsed > fadeStart) {
			int alpha = (int) (255 * (elapsed - fadeStart) / 300f);
			ctx.fill(0, 0, w, h, (Math.min(alpha, 255) << 24));
		}
		RenderSystem.disableBlend();
	}
}
