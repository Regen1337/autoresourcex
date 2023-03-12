package net.runelite.client.plugins.autoresourcex;

import net.unethicalite.api.magic.SpellBook;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public enum Teleports {
    VARROCK(SpellBook.Standard.VARROCK_TELEPORT, 25),
    LUMBRIDGE(SpellBook.Standard.LUMBRIDGE_TELEPORT, 31),
    FALADOR(SpellBook.Standard.FALADOR_TELEPORT, 37),
    CAMELOT(SpellBook.Standard.CAMELOT_TELEPORT, 45),
    ARDOUGNE(SpellBook.Standard.ARDOUGNE_TELEPORT, 51),
    WATCH_TOWER(SpellBook.Standard.WATCHTOWER_TELEPORT, 58);

    private final SpellBook.Standard teleport;
    private final int requiredLevel;

    Teleports(SpellBook.Standard teleport, int requiredLevel) {
        this.teleport = teleport;
        this.requiredLevel = requiredLevel;
    }

    // create 2 maps, one with name and respective teleport enum, and one with name and respective level
    public static final Map<String, SpellBook.Standard> TeleportMap;
    public static final Map<String, Integer> LevelMap;

    static {
        ImmutableMap.Builder<String, SpellBook.Standard> builder = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<String, Integer> builder2 = new ImmutableMap.Builder<>();
        for (Teleports teleport : values()) {
            builder.put(teleport.name(), teleport.teleport);
            builder2.put(teleport.name(), teleport.requiredLevel);
        }
        TeleportMap = builder.build();
        LevelMap = builder2.build();
    }

}