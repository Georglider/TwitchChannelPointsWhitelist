# Twitch Channel Points with Minecraft Whitelist

## Requirements
- Bukkit Minecraft Server 1.15+
- Twitch Affiliate Account

## Start
1. Go to [Twitch Dev Console](https://dev.twitch.tv/console/apps) and create an app (As a category you can choose Game Integration)
2. Download VirtualTwitchServer and open it. (You can also open it via CLI if you pass any argument to startup script (java -jar VirtualTwitchServer.jar -cli))
   1. Go to the Twitch Dev Console of your app and set OAuth Redirect URL like this: http://localhost:{PORT}/
   2. Over here you need to put port which you have selected in the previous step
   3. Click on Login with Twitch button and then paste your generated token into your config
3. Set your broadcaster_name in config as you have it on twitch
4. Copy Client ID and Client Secret to plugin's config.yml
5. Set how much time player can spend on your server after buying reward
   1. 10 = 10 seconds
   2. 5M = 5 minutes
   3. 10H = 10 hours
   4. 30D = 30 days
6. Now you can change reward settings created by bot in [Twitch Dashboard](https://dashboard.twitch.tv/)

### Please **do not** change app_access_token, user_access_token, reward_id and broadcaster_id if you don't know what you're doing

## TODO
- [x] Release plugin
- [ ] Add Integration with Twitch Subscribers and Bits
- [ ] Fix plugin size