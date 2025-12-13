# KawaiiAPI v1.0

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Paper API](https://img.shields.io/badge/Paper-1.21.4+-green.svg)](https://papermc.io/)

**KawaiiAPI** is a modern, high-performance framework for Paper Minecraft plugins. Built with Java 21 and leveraging contemporary practices like Adventure API, reflection-based systems, and industrial-grade code quality, KawaiiAPI eliminates boilerplate and provides powerful utilities for plugin dev
elopment.

## ✨ Features

### 🎨 **Color & Text Management**
- **Full Adventure API Support**: Modern Component-based text handling
- **Legacy Color Codes**: Support for `&` color codes and hex colors (`&#RRGGBB`)
- **MiniMessage Integration**: Advanced formatting with gradients, hover events, and click actions
- **Color Builder**: Fluent API for constructing complex colored messages

### ⚡ **Command System**
- **Reflection-Based Registration**: Automatic command registration without plugin.yml
- **Subcommand Architecture**: Hierarchical command structure with built-in routing
- **Smart Tab Completion**: Permission-aware tab completion with caching
- **Custom Command Lists**: Override default help displays with custom implementations
- **Alias Support**: Multiple aliases per command and subcommand

### 📦 **Menu System**
- **Inventory Menus**: Abstract base class for creating custom inventory GUIs
- **Paginated Menus**: Built-in pagination with navigation buttons (28 items per page)
- **Menu History**: Automatic back navigation with menu stack management
- **Player-Specific Data**: PlayerMenuUtility for storing per-player menu context
- **Event Handling**: Automatic click and close event management

### ⚙️ **Configuration Management**
- **Annotation-Based**: `@Config` annotation for declarative configuration
- **Multi-Format Support**: JSON and YAML with Jackson serialization
- **Auto-Generation**: Creates default configs if missing
- **Backup System**: Optional timestamped backups before saving
- **Hot Reloading**: Reload configurations without restart
- **Type-Safe**: Full Java object serialization/deserialization

### 💬 **Chat Input System**
- **Modern AsyncChat API**: Replaces deprecated Conversation API
- **Type Validation**: Built-in validators for integers, doubles, booleans, ranges, and options
- **Timeout Management**: Automatic cleanup after 60 seconds
- **Cancellation Support**: Type "cancel" to abort any input prompt
- **Disconnect Handling**: Automatic cleanup when players quit

### 🖼️ **Custom Player Skulls**
- **Profile API**: Uses Paper's modern PlayerProfile system
- **Multiple Sources**: Create skulls from UUIDs, Mojang URLs, or base64 textures
- **Block Support**: Set placed blocks to custom skull textures
- **No NMS**: Fully compatible with all Paper versions using stable APIs

### 🎯 **Region Management**
- **Cuboid Regions**: Define 3D areas with two corner points
- **Entity Queries**: Efficient chunk-based entity retrieval
- **Overlap Detection**: Check region intersections and containment
- **Visual Selectors**: Render region borders with glowing entities
- **Expansion/Contraction**: Modify region bounds programmatically

### 🛠️ **Item Utilities**
- **Builder Pattern**: Fluent ItemStack creation with method chaining
- **Component Support**: Full Adventure Component integration
- **Glow Effects**: Add enchantment glow without actual enchantments
- **Custom Model Data**: Resource pack support built-in
- **Flag Management**: Hide enchants, attributes, and other item flags

---

## 📥 Installation

### Maven Integration

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.KawaiiDevelopmentMC</groupId>
        <artifactId>KawaiiAPI</artifactId>
        <version>1.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Gradle Integration

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.KawaiiDevelopmentMC:KawaiiAPI:1.0'
}
```

### Manual Installation

1. Download `KawaiiAPI-1.0.jar` from [Releases](https://github.com/KawaiiDevelopmentMC/KawaiiAPI/releases)
2. Place the JAR in your server's `plugins/` folder
3. Restart your server

---

## 🚀 Quick Start

### Initialize MenuManager

In your main plugin class:

```java
package dev.oumaimaa.yourplugin;

import dev.oumaimaa.kawaiiapi.menu.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize MenuManager (required for menu system)
        try {
            MenuManager.setup(getServer(), this);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize MenuManager", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getSLF4JLogger().info("Plugin enabled successfully!");
    }
}
```

---

## 📚 Usage Examples

### 1️⃣ **Creating Commands**

#### Define a Subcommand

```java
package dev.oumaimaa.yourplugin.commands;

import dev.oumaimaa.kawaiiapi.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ReloadSubCommand extends SubCommand {

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @Nullable List<String> getAliases() {
        return Arrays.asList("rl", "r");
    }

    @Override
    public @NotNull String getDescription() {
        return "Reload the plugin configuration";
    }

    @Override
    public @NotNull String getSyntax() {
        return "/myplugin reload";
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String[] args) {
        // Your reload logic here
        Component success = Component.text("Configuration reloaded!", NamedTextColor.GREEN);
        sender.sendMessage(success);
    }

    @Override
    public @Nullable List<String> getSubcommandArguments(@NotNull CommandSender sender, @NotNull String[] args) {
        return null; // No tab completion needed
    }

    @Override
    public @Nullable String getPermission() {
        return "yourplugin.admin.reload";
    }

    @Override
    public boolean isPlayerOnly() {
        return false; // Console can use this
    }
}
```

#### Register the Command

```java
package dev.oumaimaa.yourplugin;

import dev.oumaimaa.kawaiiapi.command.CommandManager;
import dev.oumaimaa.yourplugin.commands.ReloadSubCommand;
import dev.oumaimaa.yourplugin.commands.HelpSubCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register command with subcommands
        CommandManager.createCoreCommand(
            this,
            "myplugin",
            "Main plugin command",
            "/myplugin <subcommand>",
            null, // Use default command list
            Arrays.asList("mp", "mplug"), // Aliases
            ReloadSubCommand.class,
            HelpSubCommand.class
        );
    }
}
```

#### Using CommandBuilder (Alternative)

```java
new CommandManager.CommandBuilder(this)
    .name("myplugin")
    .description("Main plugin command")
    .usage("/myplugin <subcommand>")
    .aliases("mp", "mplug")
    .addSubcommand(ReloadSubCommand.class)
    .addSubcommand(HelpSubCommand.class)
    .register();
```

---

### 2️⃣ **Creating Menus**

#### Basic Menu

```java
package dev.oumaimaa.yourplugin.menus;

import dev.oumaimaa.kawaiiapi.menu.Menu;
import dev.oumaimaa.kawaiiapi.menu.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class ShopMenu extends Menu {

    public ShopMenu(@NotNull PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public @NotNull String getMenuName() {
        return "&6&l✦ Shop Menu";
    }

    @Override
    public int getSlots() {
        return 27; // 3 rows
    }

    @Override
    public boolean cancelAllClicks() {
        return true; // Prevent item taking
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) {
        int slot = e.getSlot();
        
        switch (slot) {
            case 11 -> {
                // Buy sword
                sendMessage("&aYou purchased a sword!");
                playSound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.closeInventory();
            }
            case 15 -> {
                // Buy armor
                sendMessage("&aYou purchased armor!");
                player.closeInventory();
            }
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        
        inventory.setItem(11, makeItem(
            Material.DIAMOND_SWORD,
            "&e&lDiamond Sword",
            "&7Price: &6100 coins",
            "",
            "&aClick to purchase!"
        ));
        
        inventory.setItem(15, makeItem(
            Material.DIAMOND_CHESTPLATE,
            "&e&lDiamond Armor",
            "&7Price: &6500 coins",
            "",
            "&aClick to purchase!"
        ));
    }
}
```

#### Opening the Menu

```java
import dev.oumaimaa.kawaiiapi.menu.MenuManager;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerException;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerNotSetupException;

// In your command or event handler:
try {
    MenuManager.openMenu(ShopMenu.class, player);
} catch (MenuManagerException | MenuManagerNotSetupException e) {
    player.sendMessage(Component.text("Failed to open shop!", NamedTextColor.RED));
}
```

#### Paginated Menu

```java
package dev.oumaimaa.yourplugin.menus;

import dev.oumaimaa.kawaiiapi.menu.PaginatedMenu;
import dev.oumaimaa.kawaiiapi.menu.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerListMenu extends PaginatedMenu {

    public PlayerListMenu(@NotNull PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public @NotNull String getMenuName() {
        return "&6&lOnline Players";
    }

    @Override
    public int getSlots() {
        return 54; // 6 rows
    }

    @Override
    public boolean cancelAllClicks() {
        return true;
    }

    @Override
    public @NotNull List<ItemStack> dataToItems() {
        List<ItemStack> items = new ArrayList<>();
        
        for (Player onlinePlayer : getPlayer().getServer().getOnlinePlayers()) {
            ItemStack skull = makeItem(
                Material.PLAYER_HEAD,
                "&e" + onlinePlayer.getName(),
                "&7Health: &c" + onlinePlayer.getHealth(),
                "&7Location: &f" + onlinePlayer.getWorld().getName()
            );
            items.add(skull);
        }
        
        return items;
    }

    @Override
    public @Nullable HashMap<Integer, ItemStack> getCustomMenuBorderItems() {
        return null; // Use default border
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) {
        int slot = e.getSlot();
        
        // Navigation buttons
        if (slot == 47) firstPage();
        else if (slot == 48) prevPage();
        else if (slot == 49) player.closeInventory();
        else if (slot == 50) nextPage();
        else if (slot == 51) lastPage();
    }
}
```

---

### 3️⃣ **Configuration Management**

#### Define Configuration Class

```java
package dev.oumaimaa.yourplugin.config;

import dev.oumaimaa.kawaiiapi.config.Config;
import dev.oumaimaa.kawaiiapi.config.ConfigManager;

import java.util.Arrays;
import java.util.List;

@Config(
    fileName = "config",
    fileType = ConfigManager.FileType.YAML,
    createBackup = true
)
public class PluginConfig {
    
    public String serverName = "My Awesome Server";
    public int maxPlayers = 100;
    public boolean enableFeatureX = true;
    public double economyMultiplier = 1.5;
    
    public DatabaseSettings database = new DatabaseSettings();
    public List<String> enabledWorlds = Arrays.asList("world", "world_nether", "world_the_end");
    
    public static class DatabaseSettings {
        public String host = "localhost";
        public int port = 3306;
        public String database = "minecraft";
        public String username = "root";
        public String password = "password";
    }
    
    // No-args constructor required
    public PluginConfig() {}
}
```

#### Load and Save Configuration

```java
package dev.oumaimaa.yourplugin;

import dev.oumaimaa.kawaiiapi.config.ConfigManager;
import dev.oumaimaa.yourplugin.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    
    private PluginConfig config;
    
    @Override
    public void onEnable() {
        // Load config (creates default if doesn't exist)
        config = ConfigManager.loadConfig(this, PluginConfig.class);
        
        getSLF4JLogger().info("Server name: {}", config.serverName);
        getSLF4JLogger().info("Max players: {}", config.maxPlayers);
    }
    
    public void reloadConfiguration() {
        // Reload from disk
        config = ConfigManager.reloadConfig(this, PluginConfig.class);
        getSLF4JLogger().info("Configuration reloaded!");
    }
    
    public void saveConfiguration() {
        // Save changes to disk
        ConfigManager.saveConfig(this, config);
    }
}
```

---

### 4️⃣ **Chat Input System**

```java
import dev.oumaimaa.kawaiiapi.input.ChatInput;

// Simple text input
ChatInput.requestInput(plugin, player, "&eEnter your name:", input -> {
    player.sendMessage(Component.text("Hello, " + input + "!", NamedTextColor.GREEN));
});

// Integer input
ChatInput.requestIntegerInput(plugin, player, "&eHow old are you?", age -> {
    player.sendMessage(Component.text("You are " + age + " years old!", NamedTextColor.GREEN));
});

// Ranged integer input
ChatInput.requestRangedIntegerInput(plugin, player, "&eChoose difficulty (1-10)", 1, 10, difficulty -> {
    player.sendMessage(Component.text("Difficulty set to " + difficulty, NamedTextColor.GREEN));
});

// Boolean input
ChatInput.requestBooleanInput(plugin, player, "&eEnable PvP?", enable -> {
    if (enable) {
        player.sendMessage(Component.text("PvP enabled!", NamedTextColor.GREEN));
    } else {
        player.sendMessage(Component.text("PvP disabled!", NamedTextColor.RED));
    }
});

// Option selection
String[] options = {"easy", "medium", "hard"};
ChatInput.requestOptionInput(plugin, player, "&eSelect difficulty", options, choice -> {
    player.sendMessage(Component.text("Difficulty set to: " + choice, NamedTextColor.GREEN));
});

// Custom validation
ChatInput.requestInput(
    plugin,
    player,
    "&eEnter server IP (format: xxx.xxx.xxx.xxx)",
    input -> input.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
    input -> "&cInvalid IP format!",
    ip -> player.sendMessage(Component.text("IP set to: " + ip, NamedTextColor.GREEN)),
    () -> player.sendMessage(Component.text("Input cancelled.", NamedTextColor.GRAY))
);
```

---

### 5️⃣ **Color & Text Management**

```java
import dev.oumaimaa.kawaiiapi.colors.ColorTranslator;
import net.kyori.adventure.text.Component;

// Legacy color codes
Component colored = ColorTranslator.translateToComponent("&aGreen &c&lBold Red");
player.sendMessage(colored);

// Hex colors
Component hex = ColorTranslator.translateToComponent("&#FF5733This is orange!");
player.sendMessage(hex);

// MiniMessage format
Component mini = ColorTranslator.translateMiniMessage(
    "<gradient:red:blue>Rainbow Text</gradient> <hover:show_text:'Hello!'>Hover me</hover>"
);
player.sendMessage(mini);

// Color Builder
Component complex = new ColorTranslator.ColorBuilder()
    .append('a', "Success: ")
    .appendHex("FF0000", "Error occurred")
    .newLine()
    .append('e', "Please try again")
    .build();
player.sendMessage(complex);

// Strip colors
String plain = ColorTranslator.stripColors("&aColored &c&lText");
// Result: "Colored Text"
```

---

### 6️⃣ **Item Utilities**

```java
import dev.oumaimaa.kawaiiapi.items.ItemUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

// Basic item
ItemStack sword = ItemUtils.makeItem(
    Material.DIAMOND_SWORD,
    "&6&lLegendary Sword",
    "&7Damage: &c+10",
    "&7Durability: &a∞",
    "",
    "&eRight-click to use"
);

// Item with amount
ItemStack arrows = ItemUtils.makeItem(
    Material.ARROW,
    64,
    "&fArrows",
    "&7A bundle of arrows"
);

// Item Builder
ItemStack customItem = new ItemUtils.ItemBuilder(Material.DIAMOND_CHESTPLATE)
    .amount(1)
    .name("&6&lDivine Armor")
    .lore(
        "&7Protection: &a+100",
        "&7Health: &c+20",
        "",
        "&eLegendary Tier"
    )
    .enchant(Enchantment.PROTECTION, 10)
    .glow()
    .unbreakable()
    .customModelData(12345)
    .hideFlags()
    .build();

// Add glow effect
ItemStack glowing = ItemUtils.addGlow(sword);

// Make unbreakable
ItemStack unbreakable = ItemUtils.makeUnbreakable(sword);

// Hide all flags
ItemStack clean = ItemUtils.hideAllFlags(sword);
```

---

### 7️⃣ **Custom Player Skulls**

```java
import dev.oumaimaa.kawaiiapi.heads.SkullCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

// Create skull from player UUID
UUID playerUUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
ItemStack skull = SkullCreator.itemFromUuid(playerUUID);

// Create skull from Mojang texture URL
String textureUrl = "http://textures.minecraft.net/texture/...";
ItemStack customSkull = SkullCreator.itemFromUrl(textureUrl);

// Create skull from base64 texture
String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvIn19fQ==";
ItemStack texturedSkull = SkullCreator.itemFromBase64(base64);

// Modify existing skull item
ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
SkullCreator.itemWithUuid(playerHead, playerUUID);

// Set a block to a custom skull
Block block = location.getBlock();
SkullCreator.blockWithUrl(block, textureUrl);
```

---

### 8️⃣ **Region Management**

```java
import dev.oumaimaa.kawaiiapi.region.Region;
import dev.oumaimaa.kawaiiapi.region.RegionSelector;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

// Create a region
Location corner1 = new Location(world, 0, 64, 0);
Location corner2 = new Location(world, 10, 74, 10);
Region region = new Region(corner1, corner2);

// Check if location is in region
if (region.isIn(player.getLocation())) {
    player.sendMessage(Component.text("You are in the region!", NamedTextColor.GREEN));
}

// Get all players in region
List<Player> playersInRegion = region.getPlayers();
playersInRegion.forEach(p -> p.sendMessage("You're in the region!"));

// Get all entities
List<Entity> entities = region.getEntities();

// Get specific entity types
List<Zombie> zombies = region.getEntitiesByType(Zombie.class);

// Region calculations
int volume = region.getTotalBlockSize();
double height = region.getHeight();
Location center = region.getCenter();

// Expand/contract region
region.expand(5); // Expand by 5 blocks in all directions
region.contract(2); // Contract by 2 blocks

// Check region overlap
Region otherRegion = new Region(corner3, corner4);
if (region.overlaps(otherRegion)) {
    // Regions intersect
}

// Visualize region with glowing entities
RegionSelector.drawSelector(region, "spawn-area", NamedTextColor.GREEN);

// Update selector color
RegionSelector.updateSelectorColor("spawn-area", NamedTextColor.RED);

// Remove selector
RegionSelector.killSelectorsWithTag("spawn-area");

// Clean up all selectors
RegionSelector.killAllSelectors();
RegionSelector.removeTempTeams();
```

---

## 🏗️ Project Structure

```
KawaiiAPI/
├── src/main/java/dev/oumaimaa/kawaiiapi/
│   ├── KawaiiAPI.java              # Main plugin class
│   ├── colors/
│   │   └── ColorTranslator.java    # Color and text utilities
│   ├── command/
│   │   ├── CommandManager.java     # Command registration system
│   │   ├── CoreCommand.java        # Core command implementation
│   │   ├── SubCommand.java         # Subcommand abstract class
│   │   └── CommandList.java        # Custom command list interface
│   ├── config/
│   │   ├── ConfigManager.java      # Configuration management
│   │   └── Config.java             # Configuration annotation
│   ├── menu/
│   │   ├── MenuManager.java        # Menu management system
│   │   ├── Menu.java               # Base menu class
│   │   ├── PaginatedMenu.java      # Paginated menu implementation
│   │   ├── PlayerMenuUtility.java  # Player-specific menu data
│   │   └── MenuListener.java       # Menu event listener
│   ├── input/
│   │   └── ChatInput.java          # Chat input system
│   ├── items/
│   │   └── ItemUtils.java          # Item creation utilities
│   ├── heads/
│   │   └── SkullCreator.java       # Custom skull utilities
│   ├── region/
│   │   ├── Region.java             # Region management
│   │   └── RegionSelector.java     # Region visualization
│   └── exceptions/
│       ├── MenuManagerException.java
│       ├── MenuManagerNotSetupException.java
│       └── ...
├── pom.xml
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Standards

- Use Java 21 features where appropriate
- Follow existing code style and formatting
- Include Javadoc for all public methods
- Avoid deprecated Paper API methods
- Write industrial-grade, production-ready code
- Prioritize performance and memory efficiency

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Paper Team** - For the excellent Paper API
- **KyoriPowered** - For the Adventure API
- **Jackson** - For JSON/YAML serialization
- **Lombok** - For reducing boilerplate code

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/KawaiiDevelopmentMC/KawaiiAPI/issues)
- **Discussions**: [GitHub Discussions](https://github.com/KawaiiDevelopmentMC/KawaiiAPI/discussions)
- **Discord**: [Discord](https://discord.gg/wqKyGMbXSN) 

---

## 🎯 Roadmap

- [ ] Add database utilities (SQL/MongoDB)
- [ ] Implement hologram API
- [ ] Add scoreboard utilities
- [ ] Create particle effect system
- [ ] Add NPC management
- [ ] Implement event bus system
- [ ] Create GUI builder for easier menu creation

---

**Made with ❤️ by KawaiiDevelopment**

*Building the future of Minecraft plugin development, one commit at a time.*
