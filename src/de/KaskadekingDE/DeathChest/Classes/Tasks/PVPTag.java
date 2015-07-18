package de.KaskadekingDE.DeathChest.Classes.Tasks;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;

public class PVPTag implements Listener {

    public static HashMap<Player, TagItem> TaggedPlayers = new HashMap<Player, TagItem>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if(Main.PvpTagTime == -1) return;
        Entity entity = e.getEntity();
        Entity damager = e.getDamager();
        if(entity instanceof Player && damager instanceof Player) {
            final Player p = (Player) entity;
            if(TaggedPlayers.containsKey(p)) {
                TagItem oldItem = TaggedPlayers.get(p);
                Bukkit.getScheduler().cancelTask(oldItem.TaskId);
                TaggedPlayers.remove(p);
            }
            Runnable tagTime = new Runnable() {
                @Override
                public void run() {
                    if(TaggedPlayers.containsKey(p)) {
                        TaggedPlayers.remove(p);
                    }
                }
            };
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, tagTime, Main.PvpTagTime * 20L);
            TagItem item = new TagItem((Player) damager, taskId);
            TaggedPlayers.put(p, item);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(TaggedPlayers.containsKey(e.getEntity())) {
            int tagTime = TaggedPlayers.get(e.getEntity()).TaskId;
            Bukkit.getScheduler().cancelTask(tagTime);
            TaggedPlayers.remove(e.getEntity());
        }
    }
}
