package georglider.twitch.listeners;

import georglider.twitch.IntegrationApplication;
import georglider.twitch.data.ConfigManager;
import georglider.twitch.data.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;

public class JoinLeaveListener implements Listener {

    HashMap<Player, Timestamp> timestamps = new HashMap<>();
    Boolean onlineMode = Bukkit.getOnlineMode();
    WhitelistManager whitelist = IntegrationApplication.getPlugin(IntegrationApplication.class).whitelist;
    ConfigManager config = IntegrationApplication.getPlugin(IntegrationApplication.class).config;

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJoin(PlayerLoginEvent event) {
        if (!event.getPlayer().isWhitelisted()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Objects.requireNonNull(config.getConfig().getString("kick_message")));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        timestamps.put(player, Timestamp.from(Instant.now()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        FileConfiguration whitelistData = whitelist.getConfig();

        Timestamp timestamp = timestamps.get(p);
        Timestamp now = Timestamp.from(Instant.now());
        long playTime = (now.getTime() - timestamp.getTime()) / 1000;
        String playerId = playerIdentity(p);
        long timeLeft = whitelistData.getInt(playerId) - playTime;

        if (timeLeft < 0) {
            whitelistData.set(playerId, null);
            p.setWhitelisted(false);
        } else {
            whitelistData.set(playerId, timeLeft);
        }

        timestamps.remove(p);
        whitelist.saveConfig();
    }

    private String playerIdentity(Player p) {
        if (onlineMode) {
            return String.valueOf(p.getUniqueId());
        }
        return p.getName();
    }

}
