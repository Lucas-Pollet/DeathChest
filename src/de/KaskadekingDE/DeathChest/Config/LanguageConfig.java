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
    private FileConfiguration langConfig = null;
    private File langConfigFile = null;
    private JavaPlugin plugin;

    public LanguageConfig(JavaPlugin plugin) {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        this.plugin = plugin;
        langConfigFile = new File(plugin.getDataFolder(), "language.yml");

    }

    public void reloadLangConfig() {
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        InputStream configStream = Main.plugin.getResource("language.yml");
        if(configStream != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            langConfig.setDefaults(config);
        }
    }

    public FileConfiguration getLangConfig() {
        if(langConfig == null) {
            reloadLangConfig();
        }
        return langConfig;
    }

    public void saveLangConfig() {
        if(langConfig == null || langConfigFile == null) {
            return;
        }
        try {
            getLangConfig().save(langConfigFile);
        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to save language config!");
            ex.printStackTrace();
        }
    }

    public void saveDefaultLangConfig() {
        if(langConfigFile == null) {
            langConfigFile = new File(Main.plugin.getDataFolder(), "language.yml");
        }
        if(!langConfigFile.exists()) {
            Main.plugin.saveResource("language.yml", false);
        }
    }
}
