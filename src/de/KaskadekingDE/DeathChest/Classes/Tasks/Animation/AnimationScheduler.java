package de.KaskadekingDE.DeathChest.Classes.Tasks.Animation;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AnimationScheduler implements Runnable {

    public Player p;
    public Location loc;
    public int id;
    public boolean Cancel;

    public void setTaskId(int id) {
        this.id = id;
    }

    public void cancel() {
        Cancel = true;
        Bukkit.getScheduler().cancelTask(id);
        Main.ProtocolManager.SendChestAnimation(loc, p, 0);
    }

    @Override
    public void run() {
        if(Cancel) return;
        Main.ProtocolManager.SendChestAnimation(loc, p, 1);
    }
}
