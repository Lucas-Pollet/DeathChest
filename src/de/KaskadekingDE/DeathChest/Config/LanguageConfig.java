package de.KaskadekingDE.DeathChest.Config;

import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LanguageConfig {
    private FileConfiguration LanguageConfig = null;
    private File LanguageConfigFile = null;
    private JavaPlugin plugin;

    public LanguageConfig(JavaPlugin plugin) {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        this.plugin = plugin;
        LanguageConfigFile = new File(plugin.getDataFolder(), "language.yml");

    }

    public void reloadLanguageConfig() {
        LanguageConfig = YamlConfiguration.loadConfiguration(LanguageConfigFile);
        InputStream configStream = Main.plugin.getResource("language.yml");
        if(configStream != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            LanguageConfig.setDefaults(config);
        }
    }

    public FileConfiguration getLanguageConfig() {
        if(LanguageConfig == null) {
            reloadLanguageConfig();
        }
        return LanguageConfig;
    }

    public void saveLanguageConfig() {
        if(LanguageConfig == null || LanguageConfigFile == null) {
            return;
        }
        try {
            getLanguageConfig().save(LanguageConfigFile);
        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to save language config!");
            ex.printStackTrace();
        }
    }

    public void saveDefaultLanguageConfig() {
        if(LanguageConfigFile == null) {
            LanguageConfigFile = new File(plugin.getDataFolder(), "language.yml");
        }
        if(!LanguageConfigFile.exists()) {
            Main.plugin.saveResource("language.yml", false);
        }
    }
}
