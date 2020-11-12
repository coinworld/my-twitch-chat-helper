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
scoreformat <format>
setscoreformat <format>

nooverlay <yn, this feature is meant to be disabled by default. so write anything that isn't 'yes'>
font <font name>
fontsize <font size>
backcolor <color>
labelcolor <color>
overlayscoreformat <format>
overlaysetscoreformat <format>

enablechatlog <yn>
enablematchcmd <yn>
enablemisccmd <yn>
enablenpcmd <yn>
enabletimecmd <yn>
enablelinkdetect <yn>
enablepredict <yn>
```

You can get **oauth** here: https://twitchapps.com/tmi/ (for chat)

for **osu! api**: https://osu.ppy.sh/p/api/ (for match tracking / Tracking calls API every three seconds)

Every `<yn>` entries are either `yes` or anything else and not having them set just enables them (Basically `oauth`, `twichname`, `defch` are the only necessary entries. osu! api key is not needed if you are not using features like match tracking).
Keys (oauth, defch, ...) of the config are not case sensitive so you can use `ChatPrefix` or so on to improve readability.

Default values (If you don't set these values, values below are used so you don't have to set every value manually):
- `chatprefix`: (no prefix)
- `cmdcooldown`: 3000
- `npformat`: `{artist} - {song} [{difficulty}] +{mods} (by {mapper} | {sr}* | {bpm}) {url}` and you can also use `{beatmapid}` and `{setid}`
- `scoreformat`: `{ourname} | {ourscore} - {oppscore} | {oppname}`
- `setscoreformat`: `{ourname} ({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore}) {oppname}`
- `overlayscoreformat`: `{ourscore} - {oppscore}` (you can use name too)
- `overlaysetscoreformat`: `({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore})` (same as above)
- `font`: `Serif`
- `fontsize`: `30`
- `backcolor`: `white`
- `labelcolor`: `black`

You can use `black, blue, cyan, darkgray, gray, green, yellow, lightgray, magenta, orange, pink, red and white` for color.

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
**[MODS]** These two commands are used to track the match automatically. You can use ' ' (blank) in player name by replacing blanks with * (asterisk). mp id is numbers after https://osu.ppy.sh/community/matches/

The bot will open the overlay window automatically if `nooverlay` is set as anything that is not `yes`.

You can add the overlay window to OBS and if you have set `backcolor` as `black`, you should add a **Color Key** filter, set **Key Color Type** as **Custom Color** and set it as black.

Additional Options:
- `set:<number of games>` to automatically reset score and update set score.
- `nowarmup`: By default, this bot ignores every game held before `!start`. You can use this option to disable that and the bot will get every game before.
- `autoname`: Can only be used with team option. If the match title is like `some name: (TeamRed) VS (TeamBlue)` and you use this option, the bot will automatically apply team names.

`!start` :
**[MODS]** Mods can add score with `!win` and `!lose` manually. These two commands also can be used when it's automatically tracked. `set:<score>` can be used and `!win 2` to add 2 score is also possible.

`!over` :
**[MODS]** Tracking (and just basic use of `!start`) will be over if you use this command.

`!reset` :
**[MODS]** Score reset. You can also reset set scores by adding `all` at the end.

`!mp` :
Displays mp link if available.

`!setinfo <message>` :
**[MODS]** Sets info. 

`!score` :
Displays score with the info (if set).

`!setname <me>,<opponent>` :
**[MODS]** Sets name. You can also use '*' (to be replaced with ' ') here too.

---

_Use scenario because I know my english is bad_

```
!start mp:12345 player:big*black,freedom*dive set:3 - big black is 'me' and freedom dive is 'opponent'
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
Bot> Auto: big black (0) | 0 - 0 | (1) freedom dive

*big black wins*
Bot> Auto: big black (0) | 1 - 0 | (1) freedom dive

!reset
Bot> big black (0) | 0 - 0 | (1) freedom dive

!reset all
Bot> big black | 0 - 0 | freedom dive

!over
Bot> Now that every information is gone, you can start again.
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

`!np` / `!nowplaying` / `!map` / `!song`:
Displays the current song if gosumemory is available.

---

This bot has an interactive shell.



It displays chat and you just can chat with it.

If there is a message which needs a specific action, the message will be shown in **bright yellow color** with the reason after |

You can let the bot take an appropriate action by typing `
