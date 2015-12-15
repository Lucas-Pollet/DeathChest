package de.KaskadekingDE.DeathChest.Runnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;

public class Locker implements Runnable {

    private int lockTime;
    private DeathChest chest;

    private int taskId = -1;
    private boolean taskIdSet;

    public Locker(DeathChest deathChest) {
        lockTime = Main.LockTime;
        chest = deathChest;
    }

    @Override
    public void run() {
        if(Main.HolographicDisplays) {
            if(chest.HologramDisplay == null) {
                chest.Locked = false;
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        String holoText = "§cUnlocks in §6?";
        if(lockTime > 0) {
            lockTime--;
            holoText = "§cUnlocks in §6" + Integer.toString(lockTime);
        } else if(lockTime <= 0) {
            holoText = "§aUnlocked";
        }

        if(Main.HolographicDisplays) {
            if(chest.HologramDisplay == null || chest.HologramDisplay.HologramDisplay == null) {
                Bukkit.getScheduler().cancelTask(taskId);
                return;
            }
            Hologram holo = chest.HologramDisplay.HologramDisplay;
            holo.removeLine(1);
            holo.appendTextLine(holoText);
        }

        if(lockTime == 0) {
            chest.Locked = false;
        }
    }

    public void setTaskId(int id) {
        if(taskIdSet) return;
        taskId = id;
        taskIdSet = true;
    }
}
