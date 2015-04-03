package de.KaskadekingDE.DeathChest.Classes.Tasks;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TaskScheduler {

    public static List<Integer> Tasks = new ArrayList<Integer>();

    public static void AddTask(int id) {
        if(!Tasks.contains(id)) {
            Tasks.add(id);
        }
    }

    public static void RemoveTask(int id) {
        if(Tasks.contains(id)) {
            int index = Tasks.indexOf(id);
            Tasks.remove(index);
        }
    }

    public static void RemoveAll() {
        for(Integer id: Tasks) {
            Bukkit.getScheduler().cancelTask(id);
        }
        Tasks.clear();
    }
}
