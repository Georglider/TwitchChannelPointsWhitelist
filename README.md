# Twitch Channel Points with Minecraft Whitelist

## Requirements
- Bukkit Minecraft Server 1.15+
- Twitch Affiliate Account

## Start
1. Go to [Twitch Dev Console](https://dev.twitch.tv/console/apps) and create an app (As a category you can choose Game Integration)
2. Set OAuth redirect to your server ip with port like this: http://localhost:3000, don't forget to set this port to plugin's config.yml
   1. If your server hosting does not allow you to open other ports, you can install this server to your computer and as address set http://localhost:{port} with your preferred port
3. Set your broadcaster_name in config as you have it on twitch
4. Copy Client ID and Client Secret to plugin's config.yml
5. Set how much time player can spend on your server after buying reward
   1. 10 = 10 seconds
   2. 5M = 5 minutes
   3. 10H = 10 hours
   4. 30D = 30 days
6. Start server and go to link from 2nd point
7. Here you need to click login and authorize through your Twitch account
8. Now you can change reward settings created by bot in [Twitch Dashboard](https://dashboard.twitch.tv/)

### Please **do not** change app_access_token, user_access_token, reward_id and broadcaster_id if you don't know what you're doing

## TODO
- [x] Release plugin
- [] Add Integration with Twitch Subscribers and Bits
- [] Fix plugin size