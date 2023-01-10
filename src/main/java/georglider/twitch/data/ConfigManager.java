package georglider.twitch.data;

import georglider.twitch.IntegrationApplication;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private final IntegrationApplication plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public ConfigManager(IntegrationApplication plugin) {
        this.plugin = plugin;
        // Init config
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), "config.yml");

        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = this.plugin.getResource("config.yml");

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
            this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        }

        if (!this.configFile.exists()) {
            this.plugin.saveResource("config.yml", false);
        }

        if (this.configFile.length() == 0) {
            this.getConfig().set("client_secret", 8);
            this.getConfig().set("client_id", 8);
            this.getConfig().set("broadcaster_name", 8);
            this.getConfig().set("whitelist_time", "5M");
            this.getConfig().set("reward_name", "SMP Whitelist");
            this.getConfig().set("reward_price", 5000);
            this.getConfig().set("kick_message", "You need to buy whitelist for 5000 points on Twitch!");
            this.getConfig().set("chat_message", "$(PLAYER) bought whitelist just now!");

            this.getConfig().set("app_access_token", 8);
            this.getConfig().set("user_access_token", 8);
            this.getConfig().set("reward_id", 8);
            this.getConfig().set("broadcaster_id", 8);
            this.saveConfig();
        }
    }

}
