package de.KaskadekingDE.DeathChest.Config;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KillerChestsConfig {
    private FileConfiguration langConfig = null;
    private File langConfigFile = null;
    private JavaPlugin plugin;

    public KillerChestsConfig(JavaPlugin plugin) {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        this.plugin = plugin;
        langConfigFile = new File(plugin.getDataFolder() + File.separator + "store", "killerchest.yml");

    }

    public void reloadKillerConfig() {
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        InputStream configStream = Main.plugin.getResource("killerchest.yml");
        if(configStream != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            langConfig.setDefaults(config);
        }
    }

    public FileConfiguration getKillerConfig() {
        if(langConfig == null) {
            reloadKillerConfig();
        }
        return langConfig;
    }

    public void saveKillerConfig() {
        if(langConfig == null || langConfigFile == null) {
            return;
        }
        try {
            getKillerConfig().save(langConfigFile);
        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to save language config!");
            ex.printStackTrace();
        }
    }

    public void saveDefaultKillerConfig() {
        if(langConfigFile == null) {
            langConfigFile = new File(plugin.getDataFolder() + File.separator + "store", "killerchest.yml");
        }
        if(!langConfigFile.exists()) {
            Main.plugin.saveResource("killerchest.yml", false);
        }
    }
}
