# my-twitch-chat-helper
A Twitch chat helper that has some useful features including osu! multiplayer tracker

---

You need to make `config.txt` and `predict.txt` in a directory where the program is located to make full use of this bot.

### config.txt
```
oauth oauth:<twitch oauth>
twitchname <your twitch name>
defch #<channel to join>
osuapi <your osu api key>
```

You can get **oauth** here: https://twitchapps.com/tmi/

for **osu! api**: https://osu.ppy.sh/p/api/

### predict.txt (example)
```
skin !skin @%s
key !keys @%s
```

If **skin** is included in someone's message, the bot will send `!skin @<his name>`

---

This bot has an interactive shell.



It displays chat and if there is a message which the bot deemed to need a specific action, the message will be shown in **bright yellow color** with the reason after |

You can let the bot take an appropriate action by typing `
