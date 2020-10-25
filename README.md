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
chatprefix <prefix>
cmdcooldown <duration in milliseconds, 1 sec = 1000>
npformat <format>

enablechatlog <yn>
enablematchcmd <yn>
enablemisccmd <yn>
enablenpcmd <yn>
enabletimecmd <yn>
enablelinkdetect <yn>
enablepredict <yn>
```

You can get **oauth** here: https://twitchapps.com/tmi/

for **osu! api**: https://osu.ppy.sh/p/api/

Every `<yn>` entries are either `yes` or anything else and not having them set just enables them (Basically `oauth`, `twichname`, `defch` is the only necessary. osu! api key is not needed if you are not using features like match tracking).
This bot doesn't distinguish big or small letters for keys so you can use `ChatPrefix` or so on to improve readability.

`chatprefix` and `cmdcooldown` has their default value ("" (no prefix) and 3000 respectively).

Default of `npformat` is `{artist} - {song} [{difficulty}] +{mods} (by {mapper} | {sr}* | {bpm}) osu.ppy.sh/s/{setid}` and you can also use `{beatmapid}`

### predict.txt (example)
```
skin !skin @%s
key !keys @%s
```

If **skin** is included in someone's message, the bot will send `!skin @<his name>` if you type ~(without shift, looks similar to ') in the console.

---

## Command Guide

There are some basic commands you can use right away. You can try to add some new commands if you can do some java!

### Match Commands

`!start mp:<mp id> team:<blue/red>` / `!start mp:<mp id> player:<me>,<opponent>` :
**[MODS]** These two commands are used to track the match automatically. You can use ' ' (blank) in player name by replacing blanks with * (asterisk). You can also use `set:<number of games>` to automatically reset score and update set score.

`!start` :
**[MODS]** Mods can add score with `!win` and `!lose` manually. These two commands also can be used when it's automatically tracked. `set:<score>` can be used and `!win 2` to add 2 score is also possible.

`!over` :
**[MODS]** Tracking (and just basic use of `!start`) will be over if you use this command.

`!reset` :
**[MODS]** Score reset. You can also reset set scores by adding `all` at the end.

`!mp` :
Displays mp link if available.

`!setinfo <message>` :
**[MODS]** Set info. 

`!score` :
Displays score with the info (if set).

---

_Use scenario because I know my english is bad_

```
!start mp:123456 player:big*black,freedom*dive set:3 - big black is 'me' and freedom dive is 'opponent'
Bot> Now I track the match automatically.

*in case the score is already 1-2*

!win
Bot> big black | 1 - 0 | freedom dive

!lose 2
Bot> big black | 1 - 2 | freedom dive

!setinfo hello
Bot> Info is now set.

!score
Bot> big black | 1 - 2 | freedom dive / hello

*freedom dive wins*
Bot> Auto: big black (0) | 0 - 0 | (1) freedom dive / hello

*big black wins*
Bot> Auto: big black (0) | 1 - 0 | (1) freedom dive / hello

!over
Bot> Match is over / big black | 1 - 2 | freedom dive / hello
```

### Time commands

Note that this command follows the running system's timezone.

`!setcountdown <hour>:<minute>` :
**[MODS]** Sets countdown.

`!countdown` / `!cd` :
Displays countdown.

### Miscellaneous commands

`!roll` :
**[VIP]** You can add number at the end to roll a number between 1 and that number. Default is 100.

### Gosumemory commands

[You can download gosumemory here](https://github.com/l3lackshark/gosumemory)

`!np` / `!nowplaying` :
Displays the current song if gosumemory is available.

---

This bot has an interactive shell.



It displays chat and if there is a message which needs a specific action, the message will be shown in **bright yellow color** with the reason after |

You can let the bot take an appropriate action by typing `
