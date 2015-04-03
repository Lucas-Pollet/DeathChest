package de.KaskadekingDE.DeathChest.Classes.PacketManagement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IProtocolManager {

    public void RegisterEvents();

    public void UnregisterEvents();

    public void SendChestAnimation(Location loc, Player p, int state);
    
    public void SendChestOpenPacket(Location loc, Player p);

    public void SendChestClosePacket(Location loc, Player p);

    public void SentPacket(Object packet, Player p);
}
