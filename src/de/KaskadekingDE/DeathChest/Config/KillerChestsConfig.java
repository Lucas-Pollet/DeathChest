package de.KaskadekingDE.DeathChest.Config;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class KillerChestsConfig {
    private FileConfiguration langConfig = null;
    private File langConfigFile = null;
    private JavaPlugin plugin;

    public KillerChestsConfig(JavaPlugin plugin) {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        this.plugin = plugin;
        langConfigFile = new File(plugin.getDataFolder() + File.separator + "store" + File.separator + "killerchest.yml");

    }

    public void reloadKillerConfig() {
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        InputStream configStream = Main.plugin.getResource("killerchest.yml");
        if(configStream != null) {
            FileConfiguration config;
            if(langConfigFile.exists()) {
                config = YamlConfiguration.loadConfiguration(langConfigFile);
            } else {
                try {
                    langConfigFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            }
            try {
                config.save(langConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            Main.plugin.getLogger().severe("Failed to save killer config!");
            ex.printStackTrace();
        }
    }

    public void saveDefaultKillerConfig() {
        if(langConfigFile == null) {
            langConfigFile = new File(plugin.getDataFolder() + File.separator + "store" + File.separator + "killerchest.yml");
        }
        if(!langConfigFile.exists()) {
            InputStream stream = getClass().getResourceAsStream("/killerchest.yml");
            try {
                Files.copy(stream, langConfigFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
