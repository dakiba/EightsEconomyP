package com.epherical.eights;

import com.epherical.eights.commands.BalanceCommand;
import com.epherical.octoecon.api.event.EconomyEvents;
import com.epherical.octoecon.api.user.UniqueUser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.UUID;

public class EightsEconMod implements ModInitializer {

    private EightsEconomyProvider provider;

    public static final Style CONSTANTS_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#999999"));
    public static final Style VARIABLE_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#ffd500"));
    public static final Style APPROVAL_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#6ba4ff"));
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#b31717"));

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            BalanceCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            provider = new EightsEconomyProvider(this, server);
            EconomyEvents.ECONOMY_CHANGE_EVENT.invoker().onEconomyChanged(provider);
            registerListeners();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            provider.close();
        });
    }

    private void registerListeners() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID uuid = handler.getPlayer().getUUID();
            if (!provider.hasAccount(uuid)) {
                UniqueUser user = provider.getOrCreatePlayerAccount(handler.getPlayer().getUUID());
                if (user != null) {
                    provider.cachePlayer(user);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            provider.removePlayer(handler.getPlayer().getUUID());
        });
    }
}
