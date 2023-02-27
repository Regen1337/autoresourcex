package net.runelite.client.plugins.autoresourcex;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.ObjectID;
import net.runelite.api.Tile;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.CollisionMap;
import net.unethicalite.api.movement.pathfinder.GlobalCollisionMap;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.client.Static;
import net.runelite.api.GameObject;


import static net.runelite.api.AnimationID.MINING_MOTHERLODE_3A;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_ADAMANT;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_BLACK;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_BRONZE;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_CRYSTAL;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_DRAGON;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_DRAGON_OR;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_DRAGON_OR_TRAILBLAZER;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_GILDED;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_INFERNAL;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_IRON;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_MITHRIL;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_RUNE;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_STEEL;
import static net.runelite.api.AnimationID.MINING_MOTHERLODE_TRAILBLAZER;
import static net.runelite.api.AnimationID.WOODCUTTING_3A_AXE;
import static net.runelite.api.AnimationID.WOODCUTTING_ADAMANT;
import static net.runelite.api.AnimationID.WOODCUTTING_BLACK;
import static net.runelite.api.AnimationID.WOODCUTTING_BRONZE;
import static net.runelite.api.AnimationID.WOODCUTTING_CRYSTAL;
import static net.runelite.api.AnimationID.WOODCUTTING_DRAGON;
import static net.runelite.api.AnimationID.WOODCUTTING_DRAGON_OR;
import static net.runelite.api.AnimationID.WOODCUTTING_GILDED;
import static net.runelite.api.AnimationID.WOODCUTTING_INFERNAL;
import static net.runelite.api.AnimationID.WOODCUTTING_IRON;
import static net.runelite.api.AnimationID.WOODCUTTING_MITHRIL;
import static net.runelite.api.AnimationID.WOODCUTTING_RUNE;
import static net.runelite.api.AnimationID.WOODCUTTING_STEEL;
import static net.runelite.api.AnimationID.WOODCUTTING_TRAILBLAZER;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.runelite.client.plugins.autoresourcex.Rocks;

@PluginDescriptor(
        name = "Auto Resource X",
        description = "Automatically mines, chops, and makes fires and banks",
        tags = {"@nukeafrica", "auto", "resource", "mining", "chopping", "firemaking", "woodcutting", "mines", "chops", "fires", "woodcuts", "mine", "chop"}
)

@Slf4j
public class AutoResourceXPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private GlobalCollisionMap CollisionMap;

    @Inject
    private AutoResourceXConfig config;

    static final String CONFIG_GROUP = "autoresourcex";

    private State currentState;

    private WorldPoint savedLocation;
    private WorldPoint randLocation;

    public enum State
    {
        IDLE(false),
        SCANNING_FOR_MINEABLE(false),
        SCANNING_FOR_CHOPABLE(false),
        SCANNING_FOR_FIRE(false),
        CHOPPING_WOOD(false),
        MINING(false),
        MAKING_FIRE(false),
        DROPPING(false),
        WALKING_TO_BANK(false),
        OPENING_BANK(false),
        BANKING(false),
        WALKING_BACK_FROM_BANK(false);
        

        private boolean hasEntered;
        private boolean isComplete;

        State(boolean hasEntered) {
            this.hasEntered = hasEntered;
            this.isComplete = false;
        }

        public boolean hasEntered() {
            return hasEntered;
        }

        public boolean isComplete() {
            return isComplete;
        }

        public void setIsComplete(boolean isComplete) {
            this.isComplete = isComplete;
        }

        public void setHasEntered(boolean hasEntered) {
            this.hasEntered = hasEntered;
        }
    }

    // function gets if id is valid in Rock Map
    public boolean isValidRock(int id) {
        return Rocks.RockMap.containsKey(id);
    }

    // function gets name of rock based on id
    public String getRockName(int id) {
        return Rocks.RockMap.get(id).toString().toLowerCase();
    }

    @Provides
    AutoResourceXConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AutoResourceXConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        log.info("AutoResourceX started!");
        currentState = State.IDLE;
        savedLocation = null;
        randLocation = null;
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("AutoResourceX stopped!");
        currentState = State.IDLE;
        savedLocation = null;
        randLocation = null;
    }

    private static final Set<Integer> MINING_ANIMATION_IDS = ImmutableSet.of(
		MINING_MOTHERLODE_BRONZE, MINING_MOTHERLODE_IRON, MINING_MOTHERLODE_STEEL,
		MINING_MOTHERLODE_BLACK, MINING_MOTHERLODE_MITHRIL, MINING_MOTHERLODE_ADAMANT,
		MINING_MOTHERLODE_RUNE, MINING_MOTHERLODE_GILDED, MINING_MOTHERLODE_DRAGON,
		MINING_MOTHERLODE_DRAGON_UPGRADED, MINING_MOTHERLODE_DRAGON_OR, MINING_MOTHERLODE_DRAGON_OR_TRAILBLAZER,
		MINING_MOTHERLODE_INFERNAL, MINING_MOTHERLODE_3A, MINING_MOTHERLODE_CRYSTAL,
		MINING_MOTHERLODE_TRAILBLAZER
	);

    // set of all woodcutting animations
    private static final Set<Integer> WOODCUTTING_ANIMATION_IDS = ImmutableSet.of(
        WOODCUTTING_BRONZE, WOODCUTTING_IRON, WOODCUTTING_STEEL, WOODCUTTING_BLACK,
        WOODCUTTING_MITHRIL, WOODCUTTING_ADAMANT, WOODCUTTING_RUNE, WOODCUTTING_GILDED,
        WOODCUTTING_DRAGON, WOODCUTTING_DRAGON_OR, WOODCUTTING_INFERNAL, WOODCUTTING_CRYSTAL,
        WOODCUTTING_TRAILBLAZER, WOODCUTTING_3A_AXE
    );

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!currentState.hasEntered()) { // initializations
            currentState.setHasEntered(true);
            switch (currentState) {
                case IDLE:
                    log.info("Idle");
                    break;
                case SCANNING_FOR_MINEABLE:
                    log.info("Scanning for mineable");
                    startMining(); 
                    break;  
                case SCANNING_FOR_CHOPABLE:
                    log.info("Scanning for chopable");
                    startChopping();
                    break;
                case SCANNING_FOR_FIRE:
                    log.info("Scanning for fire");
                    setRandLocation();
                    moveToRandLocation();
                    break;
                case CHOPPING_WOOD:
                    log.info("Chopping Wood");
                    break;
                case MINING:
                    log.info("Mining");
                    break;
                case MAKING_FIRE:
                    log.info("Making Fire");
                    makeFire();
                    break;
                case DROPPING:
                    log.info("Dropping");
                    dropLoot();
                    break;
                case WALKING_TO_BANK:
                    log.info("Walking To Bank");
                    saveLocation();
                    break;
                case OPENING_BANK:
                    log.info("Opening Bank");
                    interactWithBank();
                    break;
                case BANKING:
                    log.info("Banking");
                    depositLoot();
                    Bank.close();
                    break;
                case WALKING_BACK_FROM_BANK:
                    log.info("Walking Back From Bank");
                    break;
            }
        }
        else if (currentState.isComplete()) { // transitions
            currentState.setHasEntered(false);
            currentState.setIsComplete(false);
            switch (currentState) {
                case IDLE:
                    if (config.banking() && isInventoryFull())
                    {
                        currentState = State.WALKING_TO_BANK;
                    }
                    else if (config.dropLoot() && isInventoryFull() && shouldDropLoot())
                    {
                        currentState = State.DROPPING;
                    }
                    else if (config.mine() && getMineable() != null)
                    {
                        currentState = State.SCANNING_FOR_MINEABLE;
                    }
                    else if (config.woodcutting() && getChopable() != null)
                    {
                        currentState = State.SCANNING_FOR_CHOPABLE;
                    }
                    else
                    {
                        currentState = State.IDLE;
                    }
                    break;
                case SCANNING_FOR_MINEABLE:
                    currentState = State.MINING;
                    break;
                case SCANNING_FOR_CHOPABLE:
                    currentState = State.CHOPPING_WOOD;
                    break;
                case SCANNING_FOR_FIRE:
                    currentState = State.MAKING_FIRE;
                    break;
                case CHOPPING_WOOD:
                    if (config.banking() && isInventoryFull())
                    {
                        currentState = State.WALKING_TO_BANK;
                    }
                    else if (config.makeFire() && hasLoot())
                    {
                        currentState = State.SCANNING_FOR_FIRE;
                    }
                    else if (config.dropLoot() && isInventoryFull() && shouldDropLoot())
                    {
                        currentState = State.DROPPING;
                    }
                    else
                    {
                        currentState = State.IDLE;
                    }
                    break;
                case MINING:
                    if (config.banking() && isInventoryFull())
                    {
                        currentState = State.WALKING_TO_BANK;
                    }
                    else if (config.dropLoot() && isInventoryFull() && shouldDropLoot())
                    {
                        currentState = State.DROPPING;
                    }
                    else if (config.mine() && getMineable() != null)
                    {
                        currentState = State.SCANNING_FOR_MINEABLE;
                    }
                    else
                    {
                        currentState = State.IDLE;
                    }
                    break;
                case MAKING_FIRE:
                    if (config.makeFire() && hasLoot())
                    {
                        currentState = State.SCANNING_FOR_FIRE;
                    }
                    else
                    {
                        currentState = State.IDLE;
                    }
                    break;
                case DROPPING:
                    if (config.dropLoot() && shouldDropLoot())
                    {
                        currentState = State.DROPPING;
                    }
                    else
                    {
                        currentState = State.IDLE;
                    }
                    break;
                case WALKING_TO_BANK:
                    currentState = State.OPENING_BANK;
                    break;
                case OPENING_BANK:
                    currentState = State.BANKING;
                    break;
                case BANKING:
                    if (config.woodcutting() || config.mine()) {
                        currentState = State.WALKING_BACK_FROM_BANK;
                    }
                    else {
                        currentState = State.IDLE;
                    }
                    break;
                case WALKING_BACK_FROM_BANK:
                    currentState = State.IDLE;
                    break;
            }
        }
        else // ran every tick inbetween top 2
        {
            switch (currentState) {
                case IDLE:
                    currentState.setIsComplete(true);
                    break;
                case SCANNING_FOR_MINEABLE:
                    if (isMining() || (client.getLocalPlayer().getInteracting() instanceof TileObject && !isValidRock(client.getLocalPlayer().getInteracting().getId()) || getMineable() == null)) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case SCANNING_FOR_CHOPABLE:
                    if (isChopping()) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case SCANNING_FOR_FIRE:
                    if (client.getLocalPlayer().isIdle() || isMakingFire()) 
                    {
                        currentState.setIsComplete(true);
                    }
                    break;
                case CHOPPING_WOOD:
                    if (!isChopping()) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case MINING:
                    if (!isMining() || !(client.getLocalPlayer().getInteracting() instanceof TileObject) || !isValidRock(client.getLocalPlayer().getInteracting().getId()) || getMineable() == null) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case MAKING_FIRE:
                    if (!isMakingFire()) {
                        randLocation = null;
                        currentState.setIsComplete(true);
                    }
                    break;
                case DROPPING:
                    break;
                case WALKING_TO_BANK:
                    if (isInBank()) {
                        currentState.setIsComplete(true);
                    }
                    else {
                        walkToBank();
                    }
                    break;
                case OPENING_BANK:
                    if (Bank.isOpen()) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case BANKING:
                    if (!Bank.isOpen()) {
                        currentState.setIsComplete(true);
                    }
                    break;
                case WALKING_BACK_FROM_BANK:
                    if (isAtSavedLocation()) {
                        savedLocation = null;
                        currentState.setIsComplete(true);
                    }
                    else {
                        walkToSavedLocation();
                    }
                    break;
            }
        }
    }

    /* BANKING HELPER FUNCTIONS */

    public boolean interactWithBank()
    {
        NPC bank = findNearestBank();
        TileObject bankBooth = findNearestBankBooth();
        if (bankBooth != null)
        {
            bankBooth.interact("Bank");
            return true;
        }
        else if (bank != null)
        {
            bank.interact("Bank");
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isInventoryFull()
    {
        return Inventory.isFull();
    }

    // function that checks if the inventory has either a log or an ore
    public boolean hasLoot()
    {
        return Inventory.contains(x -> x.getName().toLowerCase().contains("log") || x.getName().toLowerCase().contains("ore") || x.getName().toLowerCase().contains("uncut"));
    }

    public boolean depositLoot()
    {
        if (Bank.isOpen())
        {
            List<Item> loot = Inventory.getAll(x -> (x.getName().toLowerCase().contains("log") || x.getName().toLowerCase().contains("ore") || x.getName().toLowerCase().contains("uncut") ) && !getBankDepositBlackList().contains(x.getName().toLowerCase()));
            if (loot != null)
            {
                for (Item item : loot)
                {
                    Bank.depositAll(item.getName());
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public TileObject findNearestBankBooth()
    {
        TileObject bankBooth = TileObjects.getNearest(x -> x.getName().toLowerCase().contains("booth") && x.hasAction("Bank"));

        return bankBooth;
    }

    public NPC findNearestBank()
    {
        NPC bank = NPCs.getNearest(x -> x.hasAction("Bank"));
        return bank;
    }

    public BankLocation findNearestBankLocation()
    {
        BankLocation bankLocation = BankLocation.getNearest();
        return bankLocation;
    }

    public boolean isInBank()
    {
        return BankLocation.getNearest().getArea().contains(client.getLocalPlayer());
    }

    public void saveLocation()
    {
        savedLocation = client.getLocalPlayer().getWorldLocation();
    }

    public boolean isAtSavedLocation()
    {
        if (savedLocation != null)
        {
            return savedLocation.distanceTo(client.getLocalPlayer().getWorldLocation()) < 5;
        }
        else
        {
            return false;
        }
    }

    public boolean walkToBank()
    {
        if (config.banking() && (getUnderAttack() != null || client.getLocalPlayer().isIdle()))
        {
            Movement.walkTo(BankLocation.getNearest().getArea().getCenter());
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean walkToSavedLocation()
    {
        if (config.banking() && ( getUnderAttack() != null || client.getLocalPlayer().isIdle()))
        {
            Movement.walkTo(savedLocation);
            return true;
        }
        else
        {
            return false;
        }
    }

    /* END */

    /* WOODCUTTING HELPER FUNCTIONS */

    private boolean isChopping() {
        return WOODCUTTING_ANIMATION_IDS.contains(client.getLocalPlayer().getAnimation());
    }

    private TileObject getChopable() {
        TileObject chopableTileObject = TileObjects.getNearest(x -> getChopableList().contains(x.getName().toLowerCase()) && x.hasAction("Chop down") && x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < config.scanDistance());
        return chopableTileObject;
    }

    private boolean startChopping() {
        TileObject chopableTileObject = getChopable();
        if (chopableTileObject != null) {
            chopableTileObject.interact("Chop down");
            return true;
        }
        else {
            return false;
        }
    }


    /* END */

    /* MINING HELPER FUNCTIONS */

    private boolean isMining() {
        return !client.getLocalPlayer().isIdle();
    }

    private TileObject getMineable() {
        TileObject mineableTileObject = TileObjects.getNearest(x -> isValidRock(x.getId()) && getMineableList().contains(getRockName(x.getId())) && x.hasAction("Mine") && x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < config.scanDistance());
        return mineableTileObject;
    }

    private boolean startMining() {
        TileObject mineableTileObject = getMineable();
        if (mineableTileObject != null) {
            Item log = Inventory.getFirst(x -> x.getName().toLowerCase().contains("log"));
            Item knife = Inventory.getFirst(x -> x.getName().toLowerCase().contains("knife"));
            if (config.tickManipulation() && log != null && knife != null && client.getLocalPlayer().getWorldLocation().distanceTo(mineableTileObject.getWorldLocation()) < 2)
            {
                knife.useOn(log);
            }
            mineableTileObject.interact("Mine");
            return true;
        }
        else {
            return false;
        }
    }

    /* END */

    /* DROPPING HELPER FUNCTIONS */

    // check if dropping is enabled in config, and if so search inventory for loot name not case sensitive that has "log" or "ore" and is not in the getBankDepositBlackList
    private boolean shouldDropLoot()
    {
        return config.dropLoot() && Inventory.contains(x -> (x.getName().toLowerCase().contains("log") || x.getName().toLowerCase().contains("ore") || x.getName().toLowerCase().contains("uncut")) && !getBankDepositBlackList().contains(x.getName().toLowerCase()));
    }

    // drop first loot matching should drop loot
    private boolean dropLoot()
    {
        Item loot = Inventory.getFirst(x -> (x.getName().toLowerCase().contains("log") || x.getName().toLowerCase().contains("ore") || x.getName().toLowerCase().contains("uncut")) && !getBankDepositBlackList().contains(x.getName().toLowerCase()));
        if (loot != null)
        {
            Static.getClient().invokeMenuAction("Drop", "<col=ff9040>" + loot.getName(), 7, 1007, loot.getSlot(), 9764864);
            currentState.setIsComplete(true);
            return true;
        }
        else
        {
            currentState.setIsComplete(true);
            return false;
        }
    }


    /* END */

    /* GENERAL HELPER FUNCTIONS */


    private WorldPoint findEmptyTile()
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        Tile playerTile = Tiles.getAt(playerLocation);
        if (playerTile != null && isTileEmpty(playerTile))
        {
            return playerLocation;
        }
        for (int x = -2; x < 4; x++)
        {
            for (int y = -2; y < 4; y++)
            {
                WorldPoint tilePoint = new WorldPoint(playerLocation.getX() + x, playerLocation.getY() + y, playerLocation.getPlane());
                Tile tile = Tiles.getAt(tilePoint);
                if (tile != null && isTileEmpty(tile))
                {
                    return tile.getWorldLocation();
                }
            }
        }
        return null;
    }

    private boolean isTileEmpty(Tile tile)
    {
        return tile != null && !CollisionMap.fullBlock(tile.getWorldLocation()) && 
        TileObjects.getFirstAt(tile, x -> x instanceof GameObject) == null;
    }

    private void setRandLocation() {
        randLocation = findEmptyTile();
    }
    
    private boolean moveToRandLocation()
    {
        Item box = Inventory.getFirst("Tinderbox");
        Item log = Inventory.getFirst(x -> x.getName().toLowerCase().contains("log"));
        if (randLocation != null && box != null && log != null)
        {
            Movement.walkTo(randLocation);
            return true;
        }
        return false;
    }

    private boolean makeFire()
    {
        Item box = Inventory.getFirst("Tinderbox");
        Item log = Inventory.getFirst(x -> x.getName().toLowerCase().contains("log"));
        if (box != null && log != null)
        {
            box.useOn(log);
            return true;
        }
        return false;
    }

    // isMakingFire check animation, animation is 733
    private boolean isMakingFire()
    {
        return client.getLocalPlayer().getAnimation() == 733;
    }

    

    public NPC getUnderAttack()
    {
        NPC npc = NPCs.getNearest(x -> x.getInteracting() == client.getLocalPlayer());
        return npc;
    }

    private ArrayList<String> getMineableList() {
        return new ArrayList<>(Arrays.asList(config.mineableList().toLowerCase().split(",")));
    }

    private ArrayList<String> getChopableList() {
        return new ArrayList<>(Arrays.asList(config.woodcuttingList().toLowerCase().split(",")));
    }

    private ArrayList<String> getBankDepositBlackList() {
        return new ArrayList<>(Arrays.asList(config.bankingDepositBlacklist().toLowerCase().split(",")));
    }

    /* END */
}
