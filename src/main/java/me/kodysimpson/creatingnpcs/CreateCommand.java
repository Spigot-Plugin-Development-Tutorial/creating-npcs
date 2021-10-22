package me.kodysimpson.creatingnpcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CreateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (sender instanceof Player p){

            CraftPlayer craftPlayer = (CraftPlayer) p;

            //NMS representation of the MC server
            MinecraftServer server = craftPlayer.getHandle().getServer();
            //NMS representation of the MC world
            ServerLevel level = craftPlayer.getHandle().getLevel();

            //Create a new NPC named Billy Bob, with a new GameProfile to uniquely identify them
            ServerPlayer npc = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), "Billy Bob"));
            //Set their position. They will be here when we call the packets below to spawn them
            npc.setPos(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());

            //set the npc skin
            /* Retrieving skins:
             *  - First thing to understand is that skins have to be "signed" by Mojang. Each skin has two parts: texture and signature
             *  - You can retrieve this data from:
             *    https://sessionserver.mojang.com/session/minecraft/profile/UUID?unsigned=false
             *  - UUID = the uuid of a player without the dashes
             *  - UUID data can only be retrieved every 60 seconds (if same uuid)
             */
            String signature = "ArwoD4sGhthC32Qaq1oSwNOWPciJN54mLj+Tq0tZBUMCaw7Gnpj6W9HJhLrax6gVs8X3O5cWUrgLbAIF8uelb5jLdUpm9ZFsAFUo/MtE3oqCXBjoXw8+Wn8y8WR1UAXwv0ts+C6OSyOfLGk0tR7Jmkac6G7bUKYOAMFtCGcppdmoxvhALHPkcsPmdlE8SsHhOVDBp+SE9SBA0V5Z2YDTua34bLdCh4jHibb9x6D8yLxos5ksqcUzsLW9HZ6gqt29GqRD3+M2q1VyXyOjQCR1MD/5A0WfFAFBtExWPRn4V8Fl8a6+814a84H6apaoIN0e6rZHC9ArLEbfSStS54YbjFZ5jfUHx4jkyg0n16B14Z7KLVRmWJjUPtICWaW7zlOOzzq+ZkV1fckVmXEA0Ri349DnWMSGU44nkgPsjD5PL9PLdDqhWqXQGL9f3C+XmUC+5WWdE1cA2W+ZrTN0mZajlkmcwYL0priAZZfzubhVV6PqWAaM9phgaoK7s5oQc6ruaXObauGZvxZ2p+LDx8A+AKnpxSPvjE+fVoOZUAvzVIhwXkFo8Y7+lJi29GjNS8f+fZctPivnABnK2oHXVapvdWlOfpTg/Y8cgc+GHhsvY82f9p7tyFAjV59Ps2G3TDjNbxm7iRaNs4MBUf2e8+mQFt/MbbblCfDBMUOprV0vjks=";
            String texture = "ewogICJ0aW1lc3RhbXAiIDogMTYzMzI2Mzg5NjIyNSwKICAicHJvZmlsZUlkIiA6ICIwNjlhNzlmNDQ0ZTk0NzI2YTViZWZjYTkwZTM4YWFmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RjaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOTIwMDlhNDkyNWI1OGYwMmM3N2RhZGMzZWNlZjA3ZWE0Yzc0NzJmNjRlMGZkYzMyY2U1NTIyNDg5MzYyNjgwIgogICAgfQogIH0KfQ==";
            npc.getGameProfile().getProperties().put("textures", new Property("textures", texture, signature));

            //Send the packets to artificially spawn this entity, only the client we are sending the packet to will know of it's existence
            ServerGamePacketListenerImpl ps = craftPlayer.getHandle().connection;
            //Player Info Packet
            //Sent by the server to update the user list (<tab> in the client).
            ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
            //Spawn Player packet
            ps.send(new ClientboundAddPlayerPacket(npc));

            //Give the player items and armor
            ItemStack item = new ItemStack(Material.DIAMOND_AXE);
            ps.send(new ClientboundSetEquipmentPacket(npc.getBukkitEntity().getEntityId(), List.of(Pair.of(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(item)))));
            ps.send(new ClientboundSetEquipmentPacket(npc.getBukkitEntity().getEntityId(), List.of(Pair.of(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item)))));
            ps.send(new ClientboundSetEquipmentPacket(npc.getBukkitEntity().getEntityId(), List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.GOLDEN_HELMET, 1))))));

            //add it to the list of NPCs so we can access it in our movement listener
            CreatingNpcs.getPlugin().getNpcs().add(npc);

            p.sendMessage("Spawning a Billy Bob NPC");
        }

        return true;
    }
}
