package de.KaskadekingDE.DeathChest.Classes.Tasks.Animation;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Animation {

    public Player player;
    public Location loc;
    public AnimationScheduler animationScheduler;
    public int TaskId;

    public Animation(Player p, Location loc, int taskId) {
        player = p;
        this.loc = loc;
        TaskId = taskId;
    }
}
