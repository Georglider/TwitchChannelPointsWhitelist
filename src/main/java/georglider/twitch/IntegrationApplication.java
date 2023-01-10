package georglider.twitch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import georglider.twitch.data.ConfigManager;
import georglider.twitch.data.WhitelistManager;
import georglider.twitch.listeners.JoinLeaveListener;
import georglider.twitch.listeners.TwitchListenerAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public final class IntegrationApplication extends JavaPlugin {

    public ConfigManager config;
    public WhitelistManager whitelist;
    public TwitchListenerAPI twitch;

    @Override
    public void onEnable() {
        this.config = new ConfigManager(this);
        this.whitelist = new WhitelistManager(this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();

        console.sendMessage(String.valueOf(this.config.getConfig().getInt("app_access_token")));
        console.sendMessage(Objects.requireNonNull(this.config.getConfig().getString("app_access_token")));

        if (this.config.getConfig().getInt("app_access_token") == 8) {
            if (this.config.getConfig().getInt("client_id") != 8) {
                if (this.config.getConfig().getInt("client_secret") != 8) {
                    try {
                        URL url = new URL("https://id.twitch.tv/oauth2/token?client_id="+ this.config.getConfig().getString("client_id")+"&client_secret="+this.config.getConfig().getString("client_secret")+"&grant_type=client_credentials&scope=channel:manage:redemptions%20channel:read:redemptions");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        InputStream inputStream = connection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(inputStream);
                        JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

                        this.config.getConfig().set("app_access_token", json.get("access_token").getAsString());
                        this.config.saveConfig();
                    } catch (IOException e) {
                        console.sendMessage(ChatColor.DARK_RED + "Either client_id or client_secret is wrong. Please check your config file");
                        e.printStackTrace();
                    }
                    this.config.reloadConfig();
                } else {
                    console.sendMessage(ChatColor.DARK_RED + "Please provide your client_secret into config file!");
                }
            } else {
                console.sendMessage(ChatColor.DARK_RED + "Please provide your client_id into config file!");
            }
        }

        console.sendMessage(String.valueOf(this.config.getConfig().getInt("user_access_token")));
        if (this.config.getConfig().getInt("user_access_token") != 8) {
            this.twitch = new TwitchListenerAPI();
            getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
        } else {
            console.sendMessage(ChatColor.DARK_RED + "Please provide your user_access_token into config file! You can get it via special jar");
        }

        //twitchClient.getEventManager().onEvent(UpdateRedemptionProgressEvent.class, System.out::println);
    }

    @Override
    public void onDisable() {
        this.twitch.stop();
        //twitchClient.getPubSub().unsubscribeFromTopic(ChannelPointsListener);
    }

}
