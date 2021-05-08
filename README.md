# my-twitch-chat-helper
A Twitch chat helper that has some useful features including osu! multiplayer tracker

---

You need to make `config.txt` and `startup.txt` in a directory where the program is located to make full use of this bot.

### config.txt

Recommended entries:

```
oauth oauth:<twitch oauth>
twitchname <your twitch name>
defch #<channel to join>
osuapi <your osu api key>
cmdcooldown <duration in milliseconds, 1 sec = 1000>
enablechatlog <yn>
```

Additional entries you can add:

```
chatprefix <prefix>

npformat <format>
scoreformat <format>
setscoreformat <format>
autoscorechat <yn>

textscore <yn>
textscoreformat <format>
textsetscoreformat <format>
```

You can get **oauth** here: https://twitchapps.com/tmi/ (for chat)

for **osu! api**: https://osu.ppy.sh/p/api/ (for match tracking / Tracking calls API every three seconds)

Every `<yn>` entries are either `yes` or anything else and not having them set just enables them (Basically `oauth`, `twichname`, `defch` are the only necessary entries. osu! api key is not needed if you are not using features like match tracking).
Keys (oauth, defch, ...) of the config are not case sensitive so you can use `ChatPrefix` or so on to improve readability.

Font~Height Options are only used when `overlaytype` is set as `window`.

Default values (If you don't set these values, values below are used so you don't have to set every value manually):
- `chatprefix`: (no prefix)
- `cmdcooldown`: 3000
- `npformat`: `{artist} - {song} [{difficulty}] +{mods} (by {mapper} | {sr}* | {bpm}) {url}` and you can also use `{beatmapid}` and `{setid}`
- `scoreformat`: `{ourname} | {ourscore} - {oppscore} | {oppname}`
- `setscoreformat`: `{ourname} ({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore}) {oppname}`
- `textscoreformat`: `{ourscore} - {oppscore}` (you can use name too)
- `textsetscoreformat`: `({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore})` (same as above)

### startup.txt

This file defines command to be launched at startup. You will have no commands (except for manage ones) or suggestions if you don't have this file set up.

```
!managecmd enable np
!managecmd enable time
!managecmd enable match

!managesugg enable linkdetect
```

---

### Some config.txt examples

only !np bot:

```
> config.txt
oauth oauth:1234567abcdefg
twitchname npbot
defch #channel
osuapi notused

enablechatlog no

> startup.txt
!managecmd enable np
```

Score and time commands with text overlay:

```
> config.txt
oauth oauth:1234567abcdefgh
twitchname matchbot
defch #channel
osuapi 1234567890qwertyuiop

textscore yes

> startup.txt
!managecmd enable match
!managecmd enable time
```

---

## Command Guide

There are some basic commands you can use right away. You can try to add some new commands if you can do some java!

### Match Commands / `match`

`!start mp:<mp id> team:<blue/red>` / `!start mp:<mp id> player:<me>,<opponent>` :
**[MODS]** These two commands are used to track the match automatically. You can use ' ' (blank) in player name by replacing blanks with * (asterisk). mp id is numbers after https://osu.ppy.sh/community/matches/

You can also use `textscore` option, which is enabled by default. This option will create `score.txt` in a directory where the program is in.

To use this in OBS, add **Text** in OBS, check **Read from file** in its properties and choose the file in the Text File 'Browse' dialog.

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
**[MODS]** Sets info. You can use `{info}` in any score format.

`!score` :
Displays score with the info (if set).

`!setname <me>,<opponent>` :
**[MODS]** Sets name. You can also use '*' (to be replaced with ' ') here too.

---

_Use scenario because I know my english is bad_

```
!start mp:12345 player:a,b set:3 - a is 'me' and b is 'opponent'
Bot> (some message)

*in case the score is already 1-2*

!win
Bot> a | 1 - 0 | b

!lose 2
Bot> a | 1 - 2 | b

!score
Bot> a | 1 - 2 | b

*b wins*
Bot> Auto: a (0) | 0 - 0 | (1) b  <-- you can disable this Auto msg with 'autoscorechat' in config

*a wins*
Bot> Auto: a (0) | 1 - 0 | (1) b

!reset
Bot> a (0) | 0 - 0 | (1) b

!reset all
Bot> a | 0 - 0 | b

!over
Bot> Now that every information is gone, you can start again.
```

### Time commands / `time`

Note that this command follows the running system's timezone.

`!setcountdown <hour>:<minute>` :
**[MODS]** Sets countdown.

`!countdown` / `!cd` :
Displays countdown.

### Miscellaneous commands / `misc`

`!roll` :
**[VIP]** You can add number at the end to roll a number between 1 and that number. Default is 100.

### Now playing commands / `np`

[You can download gosumemory here](https://github.com/l3lackshark/gosumemory)

`!np` / `!nowplaying` / `!map` / `!song`:
Displays the current song if gosumemory is available.

`!chimu (!bloodcat) / !nerina / !sayobot`:
Displays a corresponding mirror link of previous `!np` result.

### Debug commands / `debug`

`!debug`:
**[MODS]** Not really used currently. It is used when something wrong happened and I need to check in the chat.

### Manage commands / `manage`

`!managecmd <enable/disable> <command group name>`:
**[MODS]** Enable or disable command without restarting bot.

`!managesugg <enable/disable/toggleinstant> <suggest name>`:
**[MODS]** Enable or disable suggestions or toggle instant action without restarting bot.

`!chconf <key> <value>`:
**[MODS]** Changes a config.txt entry temporarily. Entries changed with this command won't survive re-launch. Most commands don't support changing config through this command so you will need to re-**enable** the command to see the effect.

---

This bot has an interactive shell.



It displays chat and you just can chat with it.
