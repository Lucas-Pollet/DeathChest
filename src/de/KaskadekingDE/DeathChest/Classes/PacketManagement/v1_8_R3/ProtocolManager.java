package de.KaskadekingDE.DeathChest.Classes.PacketManagement.v1_8_R3;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.PacketManagement.IProtocolManager;
import de.KaskadekingDE.DeathChest.Classes.PermissionManager;
import de.KaskadekingDE.DeathChest.Main;
import de.inventivegames.packetlistener.PacketListenerAPI;
import de.inventivegames.packetlistener.handler.PacketHandler;
import de.inventivegames.packetlistener.handler.ReceivedPacket;
import de.inventivegames.packetlistener.handler.SentPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockAction;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtocolManager implements IProtocolManager {

    public PacketHandler Handler;

    @Override
    public void RegisterEvents() {
        Handler = new PacketHandler(Main.plugin) {
            @Override
            public void onSend(SentPacket sentPacket) {
                if(sentPacket.getPacketName().equals("PacketPlayOutBlockAction")) {
                    if(sentPacket.getPacketName().equals("PacketPlayOutBlockAction")) {
                        BlockPosition bp = (BlockPosition)sentPacket.getPacketValue("a");
                        int x = bp.getX();
                        int y = bp.getY();
                        int z = bp.getZ();
                        if(sentPacket.hasPlayer()) {
                            Player p = sentPacket.getPlayer();
                            World w = p.getLocation().getWorld();
                            Location loc = new Location(w, x, y, z);
                            Helper.ChestState state = Helper.GetChestType(loc);
                            if(state == Helper.ChestState.DeathChest) {
                                DeathChest dc = DeathChest.DeathChestByLocation(loc);
                                if(dc != null) {
                                    if(!dc.Owner.equals(p) && !PermissionManager.PlayerHasPermission(p, PermissionManager.PROTECTION_BYPASS, false)) {
                                        sentPacket.setCancelled(true);
                                    }
                                }
                            } else if(state == Helper.ChestState.HomeChest) {
                                HomeChest hc = HomeChest.HomeChestByLocation(loc);
                                if(hc != null) {
                                    if(!hc.Owner.equals(p) && !PermissionManager.PlayerHasPermission(p, PermissionManager.PROTECTION_BYPASS, false)) {
                                        sentPacket.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onReceive(ReceivedPacket receivedPacket) { }
        };
        PacketListenerAPI.addPacketHandler(Handler);
    }

    @Override
    public void UnregisterEvents() {
        PacketListenerAPI.removePacketHandler(Handler);
    }

    @Override
    public void SendChestAnimation(Location loc, Player p, int state) {
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        org.bukkit.block.Block block = loc.getBlock();
        Block b = CraftMagicNumbers.getBlock(block);
        PacketPlayOutBlockAction blockActionPacket = new PacketPlayOutBlockAction(bp, b, 1, state);
        SentPacket(blockActionPacket, p);
    }

    @Override
    public void SendChestOpenPacket(Location loc, Player p) {
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        org.bukkit.block.Block block = loc.getBlock();
        Block b = CraftMagicNumbers.getBlock(block);
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect("random.chestopen", loc.getX(), loc.getY(), loc.getZ(), 0.5F, 1.0F);
        PacketPlayOutBlockAction blockActionPacket = new PacketPlayOutBlockAction(bp, b, 1, 1);
        SentPacket(packet, p);
        SentPacket(blockActionPacket, p);
    }

    @Override
    public void SendChestClosePacket(Location loc, Player p) {
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        org.bukkit.block.Block block = loc.getBlock();
        Block b = CraftMagicNumbers.getBlock(block);
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect("random.chestclosed", loc.getX(), loc.getY(), loc.getZ(), 0.5F, 1.0F);
        PacketPlayOutBlockAction blockActionPacket = new PacketPlayOutBlockAction(bp, b, 1, 0);
        SentPacket(packet, p);
        SentPacket(blockActionPacket, p);
    }

    @Override
    public void SentPacket(Object packet, Player p) {
        Handler.sendPacket(p, packet);
    }
}
