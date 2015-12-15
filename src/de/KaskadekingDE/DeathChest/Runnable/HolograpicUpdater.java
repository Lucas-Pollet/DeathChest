package de.KaskadekingDE.DeathChest.Runnable;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;

public class HolograpicUpdater implements Runnable {

    private DeathChest chest;
    private int secondsToRemove = Main.SecondsToRemove;
    private int taskId;
    private boolean taskIdSet;

    public HolograpicUpdater(DeathChest chest){
        this.chest = chest;
    }

    @Override
    public void run() {
        if(secondsToRemove == 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void setTaskId(int id) {
        if(taskIdSet) return;
        taskId = id;
        taskIdSet = true;
    }
}
