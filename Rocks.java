package net.runelite.client.plugins.autoresourcex;
import static net.runelite.api.ObjectID.*;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public enum Rocks
{
	TIN(ROCKS_11360, ROCKS_11361),
	COPPER(ROCKS_10943, ROCKS_11161),
	IRON(ROCKS_11364, ROCKS_11365, ROCKS_36203),
	COAL(ROCKS_11366, ROCKS_11367, ROCKS_36204),
	SILVER(ROCKS_11368, ROCKS_11369, ROCKS_36205),
	SANDSTONE(ROCKS_11386),
	GOLD(ROCKS_11370, ROCKS_11371, ROCKS_36206),
	GRANITE(ROCKS_11387),
	MITHRIL(ROCKS_11372, ROCKS_11373, ROCKS_36207),
	LOVAKITE(ROCKS_28596, ROCKS_28597),
	ADAMANTITE(ROCKS_11374, ROCKS_11375, ROCKS_36208),
	RUNITE(ROCKS_11376, ROCKS_11377, ROCKS_36209),
	//ORE_VEIN(),
	//AMETHYST(),
	ASH_VEIN(ASH_PILE),
	GEM_ROCK(ROCKS_11380, ROCKS_11381),
	URT_SALT(ROCKS_33254),
	EFH_SALT(ROCKS_33255),
	TE_SALT(ROCKS_33256),
	BASALT(ROCKS_33257),
	DAEYALT_ESSENCE(DAEYALT_ESSENCE_39095);
	//BARRONITE(),
	//MINERAL_VEIN();

	private final int[] ids;
    // create a java map of the ids to respective names
    public static final Map<Integer, Rocks> RockMap;

    static
    {
        ImmutableMap.Builder<Integer, Rocks> builder = new ImmutableMap.Builder<>();
        for (Rocks rock : values())
        {
            for (int rockId : rock.getIds())
            {
                builder.put(rockId, rock);
            }
        }
        RockMap = builder.build();
    }

	Rocks(int... ids)
	{
		this.ids = ids;
	}

	public int[] getIds()
	{
		return ids;
	}

	public static Rocks getRock(int id)
	{
		for (Rocks rock : values())
		{
			for (int rockId : rock.getIds())
			{
				if (rockId == id)
				{
					return rock;
				}
			}
		}
		return null;
	}

	public static int getId(String name)
	{
		for (Rocks rock : values())
		{
			if (rock.name().equalsIgnoreCase(name))
			{
				return rock.getIds()[0];
			}
		}
		return -1;
	}

    public static String getName(int id)
    {
        for (Rocks rock : values())
        {
            for (int rockId : rock.getIds())
            {
                if (rockId == id)
                {
                    return rock.name();
                }
            }
        }
        return null;
    }
}