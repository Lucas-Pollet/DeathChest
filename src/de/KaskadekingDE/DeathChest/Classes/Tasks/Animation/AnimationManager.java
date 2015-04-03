package de.KaskadekingDE.DeathChest.Classes.Tasks.Animation;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AnimationManager {


    private static List<Animation> animation = new ArrayList<Animation>();

    public static void Create(Player p, Location loc) {
        AnimationScheduler animationScheduler = new AnimationScheduler();
        animationScheduler.p = p;
        animationScheduler.loc = loc;
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, animationScheduler, 1L, 1L);
        animationScheduler.setTaskId(taskId);
        Animation ani = new Animation(p, loc, taskId);
        ani.animationScheduler = animationScheduler;
        animation.add(ani);
    }

    public static void Remove(Player p) {
        Iterator<Animation> iter = animation.iterator();
        while(iter.hasNext()) {
            Animation ani = iter.next();
            if(ani.player.equals(p)) {
                ani.animationScheduler.cancel();
                iter.remove();
            }
        }
    }

    public static int GetTaskId(Player p) {
        for(Animation ani: animation) {
            if(ani.player.equals(p)) {
                return ani.TaskId;
            }
        }
        return -1;
    }
}
