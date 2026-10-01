# JumpScare

A Fabric mod for Minecraft 1.21.1. Every second you're in the game, two dice get rolled:

| Every second | Chance | On average |
|---|---|---|
| A face fills your screen and screams | 1 in 10,000 | about once every 2.8 hours |
| You just drop dead | 1 in 100,000 | about once every 28 hours |

The death ignores armor, Totems of Undying, and creative mode. The death message says `<player> rolled a 1 in 100,000 and dropped dead`.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Download `jumpscare-x.x.x.jar` from the latest [Actions build](../../actions) (open the newest run, then grab the **JumpScare** artifact) and put it in `mods`.

On a server, the server needs the mod for the death roll, and each player needs it on their client for the jumpscare.

## Commands

- `/jumpscare`: trigger the jumpscare right now (client side, anyone can use it)
- `/unlucky`: die the unlucky death right now (needs op / cheats on)

## Building

```
./gradlew build
```

The jar ends up in `build/libs/`.
