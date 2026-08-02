package net.robotic.seeleaderzombie.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Holder for the client-only config screen hook.
 *
 * <p>This class must only ever be touched on {@code Dist.CLIENT}; loading it on a dedicated
 * server would fail to resolve the GUI classes it references.
 */
public final class ClientConfigScreen {

    private ClientConfigScreen() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
