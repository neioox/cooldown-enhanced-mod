package dev.neiox;

import dev.neiox.utils.VersionChecker;
import net.fabricmc.api.ClientModInitializer;

public class CooldownEnhancedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.


		System.out.println("Current mod version: "+ VersionChecker.getCurrentVersion());
	}
}