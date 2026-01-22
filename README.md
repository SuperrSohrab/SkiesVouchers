# SkiesVouchers

A Minecraft plugin that allows players to use vouchers that run configurable commands when redeemed.

## Features

- **Configurable Vouchers**: Set up vouchers with custom names, IDs, and commands
- **Cooldown System**: Optional cooldowns per voucher (in seconds)
- **Usage Limits**: Optional maximum uses per player per voucher
- **Command Placeholders**: Use variables like `[player]`, `[uuid]`, `[world]`, `[x]`, `[y]`, `[z]` in commands
- **Player Data Tracking**: Automatically tracks player usage and cooldowns
- **Tab Completion**: Full tab completion support for all commands

## Commands

- `/voucher redeem <voucherId>` - Redeem a voucher
- `/voucher give <player> <voucherId>` - Give a voucher to a player (requires permission)
- `/voucher list` - List all available vouchers
- `/voucher reload` - Reload the configuration (requires permission)

**Aliases**: `/v`, `/vouchers`

## Permissions

- `skiesvouchers.redeem` - Allows players to redeem vouchers (default: op)
- `skiesvouchers.give` - Allows giving vouchers to other players (default: op)
- `skiesvouchers.reload` - Allows reloading the plugin configuration (default: op)
- `skiesvouchers.list` - Allows listing all available vouchers (default: true)

## Configuration

Vouchers are configured in `config.yml`:

```yaml
vouchers:
  voucher_key:
    name: "Display Name"          # The display name of the voucher
    id: "unique-id"                # Unique identifier for the voucher
    commands:                      # List of commands to execute
      - "give [player] diamond 10"
      - "tellraw [player] {\"text\":\"Message\"}"
    cooldown: 3600                 # Cooldown in seconds (0 = no cooldown)
    max-uses: 5                    # Max uses per player (0 = unlimited)
```

### Available Placeholders

You can use these placeholders in commands:

- `[player]` - Player's name
- `[uuid]` - Player's UUID
- `[world]` - Player's current world name
- `[x]`, `[y]`, `[z]` - Player's coordinates

### Example Vouchers

The plugin comes with several example vouchers:

1. **Diamond Reward** - Gives diamonds with no restrictions
2. **Daily Money** - Gives money with a 24-hour cooldown
3. **XP Boost** - Gives XP levels with a max of 3 uses per player
4. **Starter Kit** - Gives starter items with 1-hour cooldown and single use
5. **Spawn Teleport** - Teleports to spawn with 10-minute cooldown

## Installation

1. Download `skiesvouchers-1.0.jar` from the `target` folder
2. Place the jar file in your server's `plugins` folder
3. Restart your server
4. Edit the `config.yml` in `plugins/SkiesVouchers/` to configure your vouchers
5. Run `/voucher reload` to reload the configuration

## Building from Source

```bash
mvn clean package
```

The compiled jar will be in the `target` folder.

## Requirements

- Spigot/Paper 1.15.1 or higher
- Java 8 or higher

## Data Storage

Player data (usage counts and cooldowns) is stored in `plugins/SkiesVouchers/playerdata.yml` and is automatically saved when the plugin is disabled or reloaded.

## Support

For issues or feature requests, please contact the authors: ItzaCat, SuperrSohrab
