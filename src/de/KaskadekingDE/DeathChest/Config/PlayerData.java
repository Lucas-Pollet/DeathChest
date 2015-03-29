package de.KaskadekingDE.DeathChest.Config;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlayerData {
    private FileConfiguration playerConfig = null;
    private File playerConfigFile = null;
    private JavaPlugin plugin;

    public PlayerData(JavaPlugin plugin) {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        this.plugin = plugin;
        playerConfigFile = new File(plugin.getDataFolder(), "players.yml");

    }

    public void reloadConfig() {
        reloadPlayerConfig();
    }

    public void reloadPlayerConfig() {
        playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
        InputStream configStream = Main.plugin.getResource("players.yml");
        if(configStream != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            playerConfig.setDefaults(config);
        }
    }

    public FileConfiguration getConfig() {
        return getPlayerConfig();
    }

    public FileConfiguration getPlayerConfig() {
        if(playerConfig == null) {
            reloadPlayerConfig();
        }
        return playerConfig;
    }

    public void saveConfig() {
        savePlayerConfig();
    }

    public void savePlayerConfig() {
        if(playerConfig == null || playerConfigFile == null) {
            return;
        }
        try {
            getPlayerConfig().save(playerConfigFile);
        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to save player config!");
            ex.printStackTrace();
        }
    }

    public void saveDefaultPlayerConfig() {
        if(playerConfigFile == null) {
            playerConfigFile = new File(plugin.getDataFolder(), "players.yml");
        }
        if(!playerConfigFile.exists()) {
            Main.plugin.saveResource("players.yml", false);
        }
    }
}
