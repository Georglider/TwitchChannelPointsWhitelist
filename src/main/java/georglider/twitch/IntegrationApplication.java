package georglider.twitch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import georglider.twitch.api.TwitchAPIServer;
import georglider.twitch.data.ConfigManager;
import georglider.twitch.data.WhitelistManager;
import georglider.twitch.listeners.JoinLeaveListener;
import georglider.twitch.listeners.TwitchListenerAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public final class IntegrationApplication extends JavaPlugin {

    public ConfigManager config;
    public WhitelistManager whitelist;
    public TwitchListenerAPI twitch;

    @Override
    public void onEnable() {
        this.config = new ConfigManager(this);
        this.whitelist = new WhitelistManager(this);

        if (this.config.getConfig().get("user_access_token") == null || this.config.getConfig().getInt("user_access_token") != 0) {
            System.out.println(this.config.getConfig().getInt("user_access_token"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        ConfigManager setupConfig = IntegrationApplication.getPlugin(IntegrationApplication.class).config;
                        setupConfig.getConfig().set("user_access_token", TwitchAPIServer.startServer(config.getConfig().getInt("port"), config.getConfig().getString("client_id")));
                        setupConfig.saveConfig();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.runTask(this);
            this.config.reloadConfig();
        }
        if (this.config.getConfig().get("app_access_token") == null || this.config.getConfig().getInt("app_access_token") != 0) {
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
                e.printStackTrace();
            }
            this.config.reloadConfig();
        }


        this.twitch = new TwitchListenerAPI();
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);



        //twitchClient.getEventManager().onEvent(UpdateRedemptionProgressEvent.class, System.out::println);
    }

    @Override
    public void onDisable() {
        this.twitch.stop();
        //twitchClient.getPubSub().unsubscribeFromTopic(ChannelPointsListener);
    }

}
