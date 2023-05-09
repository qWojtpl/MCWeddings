<p align="center">
  <img src="https://media.discordapp.net/attachments/816647374239694849/1099095466542645340/c852d34d65bda10cbbdbd7a9a8eadbaa492d95cbda39a3ee5e6b4b0d3255bfef95601890afd80709da39a3ee5e6b4b0d3255bfef95601890afd8070958b3d40f5be58a9073d6ba848ddb9f72.png">
</p>

<br>

# MCWeddings

<p>Add weddings to your Minecraft server</p>
<p>Tested minecraft versions: </p> 

`1.19.3`

# Installation

<p>Put MCWeddings.jar to your plugins folder and restart the server.</p>
<p>You can install LuckPerms for new features</p>

# Configuration

<details><summary>config.yml</summary>
<br>

## Config

`managePermission` - Manage plugin permission (reloads, force weddings/divorces)<br>
`marryPermission` - Permission to marry someone (use /marry command)<br>
`divorcePermission` - Permission to divorce (use /divorce command)<br>

### ==== LuckPerms features section ====

`marryStatusPermission` - If player is married then they have this permission<br>
`suffixSchema` - Suffix schema. This suffix is applied when player is married. {0} is a custom marriage color (/marry color)<br>
`suffixColors` - Available suffix colors<br>
`suffixColorPermission` - Permission to change suffix (use /marry color command)<br>
`suffixCooldown` - Cooldown between changing suffixes. Applies to both married players<br>

Both players from one marriage will have the same suffix color.

### ==== LuckPerms features section ====

`requestCooldown` - Cooldown between sending request to the same person<br>
`maxDistance` - Max distance between players who wants to marry. 0 to disable<br>
`cost` - Cost for marry/divorce<br>

In this example player need 3 gold ingots to get married, and 12 wither roses named "Divorce rose" to get divorced:

```yml
  cost:
    marry:
      0:
        item: GOLD_INGOT
        count: 3
    divorce:
      0:
        item: WITHER_ROSE
        count: 12
        name: "Divorce rose"
```

**Only /marry or /divorce executor needs to have these items, not both players!** <br>

`rewards` - Rewards for marriage length<br>

In this example reward is named first_reward (it doesn't matter how it's called), you need at least 1 day of marriage to take it.<br>
If you take it you will got 2 diamonds named "Super diamonds!" without lore, which are unbreakable and has DIG_SPEED 3 enchantment.<br>
When player will receive this reward, then console will execute this /say command.

```yml
  rewards:
    "first_reward":
      requiredDays: 1
      item: DIAMOND
      count: 2
      name: "Super diamonds!"
      lore: []
      enchantments:
      - DIG_SPEED:3
      unbreakable: true
      execute: "say {0} just got the 1st reward!"
```

**Both players can receive this reward. After a divorce, player can receive the same reward again if marriage length requirement is met again**

## Args-aliases

`requirements` - Argument name to check requirements (needed items), default it is /marry requirements<br>
`status` - Argument name to check marriage status, default is /marry status [player]<br>
`rewards` - Argument name to see rewards, default is /marry rewards<br>
`color` - Argument name to change suffix. LuckPerms is required to change suffix.<br>

## Messages

**All message fields are descripted in config.yml**

<details><summary>Default messages fields (click to reveal)</summary>

```yml
messages:

  # Prefix for commands
  prefix: "&d[&cMCWeddings&d] "

  # Message if player don't have permission and tried to execute a command
  noPermission: "&cYou don't have permission!"

  # Broadcast marry message. {0} is first player, {1} is second player
  marryMessage: "&a{0} is now married with {1}!"

  # Divorce message. {0} is first player, {1} is second player
  divorceMessage: "&c{0} is no longer married with {1}!"

  # Marriage inquiry message. {0} is first player.
  marriageInquiryMessage: "&a{0} want to marry with you. Type /marry {0} to accept!"

  # Message will be sent if player is not online. {0} is a player.
  cannotFoundPlayer: "&cCannot found player: {0}"

  # Message to console if console decide to marry someone
  mustBePlayer: "&cYou must be a player!"

  # Message if someone decide to marry himself
  marryHimself: "&cYou can't marry yourself."

  # Message if someone will send requests two times or more
  requestAlreadySent: "&cYou've already sent request to this player!"

  # Message when someone send request to another player. {0} is another player.
  requestSent: "&aSent marry request to {0}!"

  # Message when someone divorce with another player. {0} is another player
  successfullyDivorced: "&aYou successfully divorced with {0}"

  # Message when someone tries to marry another married player.
  playerAlreadyMarried: "&cThis player is already married!"
  
  # Message when someone tries to marry another player while being married
  youAreMarried: "&cYou're already married!"

  # Message when someone is not married
  notMarried: "&cYou're not married!"

  # Message when another player is not married
  playerNotMarried: "&cThis player is not married!"

  # Message when someone want to marry and doesn't have required items
  marryNoRequiredItems: "&cYou don't have required items to marry someone! Check requirements at /marry requirements"

  # Message when someone want to divorce and doesn't have required items
  divorceNoRequiredItems: "&cYou don't have required items to get divorced! Check requirements at /divorce requirements"

  # Message when second player doesn't have enough items to marry
  partnerWithoutItems: "&cYour partner doesn't have required items. You can't marry right now."

  # Message when LuckPerms is not available and someone want to change suffix color
  cantDoIt: "&cSorry, you can't do it right now."

  # Message when someone want to use color which is not listed in config
  cantUseColor: "&cYou can't use this color! Available colors: "

  # Message when someone want to change suffix and second player in marriage is not online
  partnerMustBeOnline: "&cYour partner must be online to change suffix color!"

  # Message when cooldown is active and someone want to change suffix
  suffixHasCooldown: "&cPlease wait a while before changing suffix color again!"

  # Message when someone want to change color suffix to color which is already active
  suffixActive: "&cYou already have suffix in this color!"

  # Message when suffix will be changed
  changedSuffix: "&aChanged suffix color."

  # Message when system cannot find requested reward
  cannotFindReward: "&cCannot find this reward!"

  # Message when player can't receive reward (doesn't have required days)
  cantReceiveReward: "&cYou can't receive this reward yet!"

  # Message when reward is already received
  rewardReceived: "&cYou've already received this reward!"

  # Message when player is too far away from another player
  tooFarAway: "&cYou're too far away from the second player!"

  # Simple words section
  marriageDate: "&dMarriage date: &c{0}"
  marriageLength: "&dDays of marriage: &c{0}"
  suffix: "&dSuffix: {0}"
  marriageRequirements: "&dMarriage requirements:"
  divorceRequirements: "&dDivorce requirements:"
  rewards: "&dRewards for being married:"
  remaining: "&cRemaining {0} days"
  ready: "&aREADY"
  clickToReceive: "&a&lClick to receive reward!"
  requiresDaysOfMarriage: "&c&lRequires {0} days of marriage!"
  days: "days"
  correctUsage: "&cCorrect usage: {0}"

  # Command help section
  help-marry: "&d/marry <nick> &4- &cMarry a player (or accept request)"
  help-mRequirements: "&d/marry requirements &4- &cRequirements for getting married"
  help-rewards: "&d/marry rewards &4- &cRewards for being married"
  help-status: "&d/marry status [nick] &4- &cStatus of your marriage"
  help-color: "&d/marry color <color> &4- &cSet suffix color"
  help-divorce: "&d/divorce &4- &cDivorce with player"
  help-dRequirements: "&d/divorce requirements &4- &cRequirements for getting divorced"
  help-reload: "&d/marry reload &4- &cReload configuration and data"
  help-mForce: "&d/marry force <player> <player> &4- &cForce marriage"
  help-dForce: "&d/divorce force <player> &4- &cForce divorce"
```

</details>

</details>

# Commands & Permissions

Remember, that all non-admin argument names can be changed. These arguments are default ones.<br>

`/marry` - Plugin commands. You need permission from marryPermission field<br>
`/marry <player>` - Marry another player.<br>
`/marry requirements` - Requirements to marry someone (required items from cost field)<br>
`/marry status [player]` - Status of your or (if given) - marriage of another player<br>
`/marry color <color>` - This requires LuckPerms. This changes your suffix color.<br>
`/divorce` - Get divorced. You must be married.<br>
`/divorce requirements` - Requirements to get divorced (required items from cost field)<br>
`/marry reload` - Reload plugin. You need manage permission.<br>
`/marry force <player> <player>` - Force marriage between two players. You can't execute it from console, you need manage permission<br>
`/divorce force <player>` - Force divorce for player. You can't execute it from console, you need manage permission<br>

You can use /slub as an alias for /marry and /rozwod for /divorce
