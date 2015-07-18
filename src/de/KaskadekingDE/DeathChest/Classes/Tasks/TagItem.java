package de.KaskadekingDE.DeathChest.Classes.Tasks;

import org.bukkit.entity.Player;

public class TagItem {
    public Player Damager;
    public int TaskId;

    public TagItem(Player damager, int taskId) {
        Damager = damager;
        TaskId = taskId;
    }

}
