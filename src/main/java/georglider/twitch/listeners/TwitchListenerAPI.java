package georglider.twitch.listeners;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.helix.domain.CustomReward;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import georglider.twitch.IntegrationApplication;
import georglider.twitch.data.ConfigManager;
import georglider.twitch.data.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.bukkit.Bukkit.getServer;

public class TwitchListenerAPI {

    private final TwitchClient twitchClient;
    private final CustomReward reward;
    private final WhitelistManager whitelist;
    private final ConfigManager config;
    private final Long whitelistTime;
    private PubSubSubscription channelPointsRedemptionEvents;
    private final boolean onlineMode = Bukkit.getOnlineMode();

    public TwitchListenerAPI() {
        this.config = IntegrationApplication.getPlugin(IntegrationApplication.class).config;
        this.whitelist = IntegrationApplication.getPlugin(IntegrationApplication.class).whitelist;

        this.twitchClient = TwitchClientBuilder.builder()
//                .withClientId(config.getConfig().getString("client_id"))
//                .withClientSecret(config.getConfig().getString("client_id"))
                .withDefaultAuthToken(new OAuth2Credential("twitch", config.getConfig().getString("app_access_token")))
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .build();

        if (this.config.getConfig().getInt("broadcaster_id") == 8) {
            User broadcaster = twitchClient.getHelix().getUsers(config.getConfig().getString("app_access_token"), null, Collections.singletonList(config.getConfig().getString("broadcaster_name"))).execute().getUsers().get(0);
            config.getConfig().set("broadcaster_id", broadcaster.getId());
            config.saveConfig();
            config.reloadConfig();
        }
        if (config.getConfig().getInt("reward_id") == 8) {
            config.getConfig().set("reward_id", twitchClient.getHelix().createCustomReward(config.getConfig().getString("user_access_token"), config.getConfig().getString("broadcaster_id"),
                    CustomReward.builder()
                            .title(config.getConfig().getString("reward_name"))
                            .cost(config.getConfig().getInt("reward_price"))
                            .isEnabled(false)
                            .isUserInputRequired(true)
                            .build()
            ).execute().getRewards().get(0).getId());
            config.saveConfig();
            config.reloadConfig();
        }

        this.reward = twitchClient.getHelix().getCustomRewards(config.getConfig().getString("user_access_token"),
                config.getConfig().getString("broadcaster_id"),
                Collections.singleton(config.getConfig().getString("reward_id")),
                true).execute().getRewards().get(0).withIsPaused(false);

        twitchClient.getHelix().updateCustomReward(config.getConfig().getString("user_access_token"), config.getConfig().getString("broadcaster_id"), reward.getId(), reward).execute();

        this.whitelistTime = getTime(config.getConfig().getString("whitelist_time"));
        start();
    }

    private void start() {
        this.channelPointsRedemptionEvents = twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(new OAuth2Credential("twitch", config.getConfig().getString("user_access_token")), config.getConfig().getString("broadcaster_id"));
        twitchClient.getEventManager().onEvent(RewardRedeemedEvent.class, this::rewardRedeemed);
    }

    public void stop() {
        if (twitchClient != null) {
            twitchClient.getHelix().updateCustomReward(config.getConfig().getString("user_access_token"),
                    config.getConfig().getString("broadcaster_id"),
                    config.getConfig().getString("reward_id"),
                    reward.withIsPaused(true)).execute();

            twitchClient.getPubSub().unsubscribeFromTopic(channelPointsRedemptionEvents);
        }
    }

    public void addToWhiteList(String Nickname) throws CommandException, ExecutionException, InterruptedException {
        Bukkit.getScheduler().callSyncMethod(IntegrationApplication.getPlugin(IntegrationApplication.class), () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),"whitelist add "+Nickname)).get();
        Bukkit.reloadWhitelist();
    }

    private static long getTime(String t) {
        char c = t.charAt(t.length() - 1);
        if (c == 'D') {
            return Long.parseLong(new StringBuffer(t).deleteCharAt(t.length() - 1).toString()) * 86400;
        }
        if (c == 'H') {
            return Long.parseLong(new StringBuffer(t).deleteCharAt(t.length() - 1).toString()) * 3600;
        }
        if (c == 'M') {
            return Long.parseLong(new StringBuffer(t).deleteCharAt(t.length() - 1).toString()) * 60;
        }
        return Long.parseLong(t);
    }

    private void rewardRedeemed(RewardRedeemedEvent x) {
        ChannelPointsRedemption redemption = x.getRedemption();
        if (redemption.getReward().getId().equals(config.getConfig().getString("reward_id"))) {
            String username = redemption.getUserInput();

            if (onlineMode) {
                try {
                    URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (connection.getResponseCode() == 200) {
                        InputStream inputStream = connection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(inputStream);

                        JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                        String idFormatted = json.get("id").getAsString();
                        if (idFormatted.length() < 32) {
                            twitchClient.getHelix().updateRedemptionStatus(config.getConfig().getString("user_access_token"),
                                    config.getConfig().getString("broadcaster_id"),
                                    config.getConfig().getString("reward_id"),
                                    Collections.singleton(redemption.getId()),
                                    RedemptionStatus.CANCELED).execute();
                        }
                        UUID id = UUID.fromString(String.format("%s-%s-%s-%s-%s", idFormatted.substring(0, 8), idFormatted.substring(8, 12), idFormatted.substring(12, 16), idFormatted.substring(16, 20), idFormatted.substring(20, 32)));
                        idFormatted = id.toString();
                        this.whitelist.getConfig().set(idFormatted, this.whitelist.getConfig().getLong(idFormatted) + whitelistTime);
                        addToWhiteList(json.get("name").getAsString());
                        getServer().broadcastMessage(ChatColor.GREEN + Objects.requireNonNull(config.getConfig().getString("chat_message")).replace("$(PLAYER)", username));
                    } else {
                        twitchClient.getHelix().updateRedemptionStatus(config.getConfig().getString("user_access_token"),
                                config.getConfig().getString("broadcaster_id"),
                                config.getConfig().getString("reward_id"),
                                Collections.singleton(redemption.getId()),
                                RedemptionStatus.CANCELED).execute();
                    }
                } catch (ExecutionException | InterruptedException | IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    twitchClient.getHelix().updateRedemptionStatus(config.getConfig().getString("user_access_token"),
                            config.getConfig().getString("broadcaster_id"),
                            config.getConfig().getString("reward_id"),
                            Collections.singleton(redemption.getId()),
                            RedemptionStatus.CANCELED).execute();
                }
            } else {
                this.whitelist.getConfig().set(username, this.whitelist.getConfig().getLong(username) + whitelistTime);
                try {
                    addToWhiteList(username);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    twitchClient.getHelix().updateRedemptionStatus(config.getConfig().getString("user_access_token"),
                            config.getConfig().getString("broadcaster_id"),
                            config.getConfig().getString("reward_id"),
                            Collections.singleton(redemption.getId()),
                            RedemptionStatus.CANCELED).execute();
                }
                twitchClient.getHelix().updateRedemptionStatus(config.getConfig().getString("user_access_token"),
                        config.getConfig().getString("broadcaster_id"),
                        config.getConfig().getString("reward_id"),
                        Collections.singleton(redemption.getId()),
                        RedemptionStatus.FULFILLED).execute();
                getServer().broadcastMessage(ChatColor.GREEN + Objects.requireNonNull(config.getConfig().getString("chat_message")).replace("$(PLAYER)", username));
            }
            this.whitelist.saveConfig();
        }
    }
}
