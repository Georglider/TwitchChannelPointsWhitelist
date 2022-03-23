package georglider.twitch.data;

import georglider.twitch.IntegrationApplication;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WhitelistManager {

    private final IntegrationApplication plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public WhitelistManager(IntegrationApplication plugin) {
        this.plugin = plugin;
        // Init config
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), "whitelist.yml");

        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = this.plugin.getResource("whitelist.yml");

        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "whitelist.yml");
        }

        if (!this.configFile.exists()) {
            this.plugin.saveResource("whitelist.yml", false);
        }

        if (this.configFile.length() == 0) {
            if (Bukkit.getOnlineMode()) {
                this.getConfig().set("12022aa0-1c49-4aef-8683-1eb74305a75e", 43200);
            } else {
                this.getConfig().set("Georglider", 43200);
            }
            this.saveConfig();
        }
    }

}
