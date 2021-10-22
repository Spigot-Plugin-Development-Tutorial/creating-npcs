package me.kodysimpson.creatingnpcs;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CreatingNpcs extends JavaPlugin {

    //Used to keep our NPCs to be accessed in other classes
    private List<ServerPlayer> npcs = new ArrayList<>();
    private static CreatingNpcs plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic

        plugin = this;

        getCommand("create").setExecutor(new CreateCommand());

        getServer().getPluginManager().registerEvents(new MovementListener(), this);

    }

    public List<ServerPlayer> getNpcs() {
        return npcs;
    }

    public static CreatingNpcs getPlugin() {
        return plugin;
    }
}
