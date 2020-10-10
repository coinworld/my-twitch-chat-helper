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

## Command Guide

There are some basic commands you can use right away. You can try to add some new commands if you can do some java!

### Match Commands

`!start mp:<mp id> team:<blue/red>* / *!start mp:<mp id> player:<me>,<opponent>` :
**[MODS]** These two commands are used to track the match automatically. You can use ' ' (blank) in player name by replacing blanks with * (asterisk). You can also use `set:<number of games>` to automatically reset score and update set score.

`!start` :
**[MODS]** Mods can add score with `!win` and `!lose` manually. These two commands also can be used when it's automaticaally tracked. `set:<score>` can be used and `!win 2` to add 2 score is also possible.

`!over` :
**[MODS]** Tracking (and just basic use of `!start`) will be over if you use this command.

`!reset` :
**[MODS]** Score reset. You can also reset set scores by adding `all` at the end.

`!mp` :
Displays mp link if available.

`!setinfo <message>` **[MODS]** / `!info` :
Just basic info. 

`!score` :
Displays score.

### Countdown commands

Note that this command follows the running system's timezone.

`!setcountdown <hour>:<minute>` :
**[MODS]** Sets countdown.

`!countdown` / `!cd` :
Displays countdown.

### Fun command

`!roll` :
**[VIP]** You can add number at the end to roll a number between 1 and that number. Default is 100.

---

This bot has an interactive shell.



It displays chat and if there is a message which needs a specific action, the message will be shown in **bright yellow color** with the reason after |

You can let the bot take an appropriate action by typing `
