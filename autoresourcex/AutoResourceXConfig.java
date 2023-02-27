package net.runelite.client.plugins.autoresourcex;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("autoresourcex")
public interface AutoResourceXConfig extends Config
{
    // scan distance
    @ConfigItem(
            keyName = "scanDistance",
            name = "Scan Distance",
            description = "Configures the distance to scan for objects and loot",
            position = 1
    )
    default int scanDistance()
    {
        return 1;
    }

    // toggle for mining
    @ConfigItem(
            keyName = "mine",
            name = "Mining",
            description = "Configures whether or not to mine",
            position = 2
    )
    default boolean mine()
    {
        return false;
    }

    // toggle for pickaxe swapping
    @ConfigItem(
            keyName = "tickManipulation",
            name = "Tick Manipulation",
            description = "Configures whether or not to use tick manipulation with log and knife (use mahogany logs or teak logs and add them to bank blacklist to prevent dropping)",
            position = 3
    )
    default boolean tickManipulation()
    {
        return false;
    }

    // setting for mining
    @ConfigItem(
            keyName = "mineableList",
            name = "Mineable List",
            description = "Configures a list of mineable objects separated by commas (e.g. Copper ore, Tin ore) or (e.g. All)",
            position = 6
    )
    default String mineableList()
    {
        return "";
    }

    // toggle for woodcutting
    @ConfigItem(
            keyName = "woodcutting",
            name = "Woodcutting",
            description = "Configures whether or not to woodcut",
            position = 7
    )
    default boolean woodcutting()
    {
        return false;
    }

    // setting for woodcutting
    @ConfigItem(
            keyName = "woodcuttingList",
            name = "Woodcutting List",
            description = "Configures a list of woodcutting objects separated by commas (e.g. Oak, Willow) or (e.g. All)",
            position = 8
    )
    default String woodcuttingList()
    {
        return "";
    }

    // make fire toggle
    @ConfigItem(
            keyName = "makeFire",
            name = "Make Fire",
            description = "Configures whether or not to make a fire after collecting logs",
            position = 9
    )
    default boolean makeFire()
    {
        return false;
    }
    
    // toggle for looting
    @ConfigItem(
            keyName = "dropLoot",
            name = "Drop Loot",
            description = "Configures whether or not to drop loot",
            position = 10
    )
    default boolean dropLoot()
    {
        return false;
    }

    // toggle for banking when inventory is full
    @ConfigItem(
            keyName = "banking",
            name = "Banking",
            description = "Configures whether or not to bank when inventory is full",
            position = 11
    )
    default boolean banking()
    {
        return false;
    }

    // banking deposit blacklist
    @ConfigItem(
            keyName = "bankingDepositBlacklist",
            name = "Banking Deposit Blacklist",
            description = "Configures a list of items to not deposit separated by commas (e.g. Copper ore, Tin ore) or (e.g. All)",
            position = 12
    )
    default String bankingDepositBlacklist()
    {
        return "";
    }
}
