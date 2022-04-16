package net.runelite.client.plugins.hasOneClickMLM;

import com.google.inject.Provides;
import io.reactivex.rxjava3.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.iutils.*;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION;

// Many functions borrowed from https://github.com/Magnusrn/Plugins
@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-OneClickMLM",
	enabledByDefault = false,
	description = "One click MLM",
	tags = {"has, one click, has-MLM, MLM"}

)
@Slf4j
public class hasOneClickMLMPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasOneClickMLMConfiguration config;

	@Inject
	private iUtils utils;

	@Inject
	private ActionQueue action;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ExecutorService executorService;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private InterfaceUtils interfaceUtils;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MenuUtils menu;

	@Inject
	private ObjectUtils object;

	@Inject
	private BankUtils bank;

	@Inject
	private NPCUtils npc;

	@Inject
	private KeyboardUtils key;

	@Inject
	private WalkUtils walk;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ExecutorService executor;

	@Inject
	private WorldService worldService;

	@Inject
	PluginManager pluginManager;

	Player player;
	Boolean plugStarted;
	Boolean startClicked = false;
	int timeout;
	int tripsDone;
	int loadsDeposited;

	private String state = "WEAR_PENDANT";

	WorldPoint afterShortCutLocation;
	WallObject wToMine;
	private static final int UPPER_FLOOR_HEIGHT = -490;


	@Provides
	hasOneClickMLMConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasOneClickMLMConfiguration.class);
	}

	private void resetVals() {
		state = "DETERMINE_STATE";
		timeout = 0;
		afterShortCutLocation = new WorldPoint(3765,5671,0);
		tripsDone = 0;
		loadsDeposited = 0;
	}


	@Override
	protected void startUp()
	{
		resetVals();
		player = client.getLocalPlayer();
	}



	@Override
	protected void shutDown() {
		// runs on plugin shutdown
		startClicked = false;
		plugStarted = false;
		utils.sendGameMessage("Trips done is " + tripsDone);
		utils.sendGameMessage(state + " is State! Is Loads Deposited: " + loadsDeposited);
		log.info("Plugin stopped");

	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event) {

		if (!event.getGroup().equalsIgnoreCase("hasOneClickMLM")) {
			return;
		}

		if (event.getKey().equals("startButton")) {
			startClicked = !startClicked;

		}
	}












	@Subscribe
	public void onGameTick(GameTick tick)
	{


		if (timeout > 0)
		{
			timeout--;
		}



	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		String text;

		if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
			return;
		else if (client.getLocalPlayer().getAnimation() == 791)
		{
			text = "<col=00ff00>MLM-ing";
		}
		else
		{
			text = "<col=00ff00>One Click MLM";
		}
		this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
				.getId(), 0, 0, 0, true);
		//Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
		client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
	{
		if (event.getMenuOption().equals("<col=00ff00>One Click MLM"))
			System.out.println(state + " is State! Is Loads Deposited: " + loadsDeposited);
			handleClick(event);
	}


	private void handleClick(MenuOptionClicked event)
	{

		if (timeout != 0)
		{
			log.debug("Consuming event because timeout is not 0");
			event.consume();
			return;

		}
		if ((client.getLocalPlayer().isMoving()
				|| client.getLocalPlayer().getPoseAnimation()
				!= client.getLocalPlayer().getIdlePoseAnimation()
				|| client.getLocalPlayer().getAnimation() == 791
				|| client.getLocalPlayer().getAnimation() == 6758
				|| client.getLocalPlayer().getAnimation() == 6752)
				& !isBankOpen())
		{
			log.debug("Consume event because not idle?");
			event.consume();
			return;
		}
		if(config.floorLevel() == FloorLevel.LOWER_LEVEL){
			doingLowerLevel(event);
		}else if(config.floorLevel() == FloorLevel.UPPER_LEVEL){
			doingUpperLevel(event);
		}else{
			System.out.println(config.floorLevel() + "is floorlevel");
		}






	}

	private void doingUpperLevel(MenuOptionClicked event){
		log.debug("state = " + state);
		int playerx = client.getLocalPlayer().getWorldLocation().getX();
		int playery = client.getLocalPlayer().getWorldLocation().getY();
		switch (state)
		{
			case "DETERMINE_STATE":
				if (!playerUtils.isItemEquipped(Set.of(ItemID.RUNE_PICKAXE,ItemID.DRAGON_PICKAXE,ItemID.MITHRIL_PICKAXE,ItemID.ADAMANT_PICKAXE,ItemID.BLACK_PICKAXE,ItemID.DRAGON_PICKAXE_OR))){
					utils.sendGameMessage("No PickAxe Equipped, but IDC");
					//return;
				}else if(inventory.isFull() && !inventory.containsItem(ItemID.PAYDIRT)){
					utils.sendGameMessage("Empty Inventory before Starting");
					return;
				}
				state = "CLIMB_STAIRS";
				break;
			case "CLIMB_STAIRS":
				System.out.println(playerx +" "+ playery + (playerx == 3755 && playery == 5675));
				if(playerx == 3755 && playery == 5675){
					state = "MINE_PRIMARY_ROCK";
					break;
				}else if(client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3755,5675,0))>1){
					event.setMenuEntry(climbUpLadder());
					timeout += 3;
					break;
				}else{
					utils.sendGameMessage(String.valueOf(playerx) +" "+ String.valueOf(playery) + " are player x and y, Please restart");
					break;
				}
			case "MINE_PRIMARY_ROCK":
				WorldPoint primaryRockLoc = new WorldPoint(3757,5677,0);
				GameObject targetRock = object.getGameObjectAtWorldPoint(primaryRockLoc);
				if(targetRock == null){//Skip to finding an ore to mine and click it
					state = "GO_MINE";
					break;
				}else{//If we have to mine rock
					event.setMenuEntry(mineRockAt(primaryRockLoc));
					timeout += 2;
					break;
				}
			case "GO_MINE":
				if(inventory.isFull()){
					state = "RETURN";
					timeout += 1;
					break;
				}
				if(config.useSpec()){
					if(client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000 && calc.getRandomIntBetweenRange(0,10) > 3){//If we need to use Special Attack, but still want to renadomize it
						event.setMenuEntry(useSpecialAttack());
						return;
					}
				}

				/**To DO: Implement Stuck Check**/
				WorldPoint primaryRockLocs = new WorldPoint(3757,5677,0);
				GameObject targetRocks = object.getGameObjectAtWorldPoint(primaryRockLocs);
				if((client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3757,5676,0)) == 0 || client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3756,5676,0)) == 0) && targetRocks != null){
					state = "MINE_PRIMARY_ROCK";
					break;
				}
				//Find appropratite rock to mine
				wToMine = findBestUpperLevelWall();
				if(wToMine != null){
					event.setMenuEntry(mineWallAt(wToMine));
					timeout += config.delayAfterClickingOre();
				}else{//No valid walls found
					timeout += 2;
				}
				break;
			case "RETURN":
				WorldPoint primaryRockReturnLoc = new WorldPoint(3757,5677,0);
				GameObject targetReturnRock = object.getGameObjectAtWorldPoint(primaryRockReturnLoc);
				if(targetReturnRock == null){//Skip to finding an ore to mine and click it
					event.setMenuEntry(climbDownLadder());
					state = "WAIT_CLIMB_DOWN";
				}else{//If we have to mine rock
					event.setMenuEntry(mineRockAt(primaryRockReturnLoc));
				}
				timeout += 2;
				break;
			case "WAIT_CLIMB_DOWN":
				int localPlayerX = client.getLocalPlayer().getWorldLocation().getX();
				int localPlayerY = client.getLocalPlayer().getWorldLocation().getY();
				//Check if got stuck, if rock respawned while runnning
				String check = checkStuck(localPlayerX,localPlayerY);
				if(check != null){
					state = check;
					timeout += 2;
					break;
				}
				timeout += 2;
				break;
			case "DEPOSIT":
				event.setMenuEntry(depositShit());
				timeout += 2;
				state = "GRAB_SHIT";
				break;
			case "GRAB_SHIT":
				if(client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3755,5672,0)) == 0){//Check tick delay on click to deposit
					state = "DEPOSIT";
					return;
				}
				if(inventory.containsItem(ItemID.PAYDIRT)){//If we are still running to deposit stuff
					timeout += 4;
					state = "DEPOSIT";
					return;
				}
				if(loadsDeposited == config.loadsBeforeCollection()){//We need to empty sack
					System.out.println("We need to empty sack");
					loadsDeposited += 1;
					state = "EMPTY_SACK";
				}else{//Deposit and go back to mining
					loadsDeposited += 1;
					state = "CLIMB_STAIRS";
				}
				timeout += 1;
				break;

			case "OPEN_BANK":
				event.setMenuEntry(openDepositLoot());
				timeout += 2;
				state = "DEPOSIT_LOOT";
				break;
			case "DEPOSIT_LOOT":
				if(bank.isOpen() == false){
					timeout += 1;
					return;
				}
				tripsDone += 1;
				event.setMenuEntry(depositLoot());
				String sStat = getSackStatus();
				if(loadsDeposited != 0 && !sStat.equalsIgnoreCase("Empty Sack")){
					state = "EMPTY_SACK";
					loadsDeposited--;
					timeout += 3;
					break;
				}else if(loadsDeposited != 0 && sStat.equalsIgnoreCase("Empty Sack")){
					System.out.println("Loads Deposited isnt 0 but its broken");
					loadsDeposited = 1;
					state = "CLIMB_STAIRS";
					timeout += 1;
					break;
				}else if(loadsDeposited==0 && getSackStatus().equalsIgnoreCase("Empty Sack")){
					loadsDeposited =0;
					state = "CLIMB_STAIRS";
					timeout += 1;
					break;
				}else{
					utils.sendGameMessage("There is an errors Value of Loads is: " + loadsDeposited);
					state = "CLIMB_STAIRS";
					break;
				}
			case "EMPTY_SACK":
				GroundObject colBag = object.findNearestGroundObject(26688);
				ObjectComposition x = client.getObjectDefinition(26688);
				if(inventory.containsItem(Set.of(ItemID.COAL,ItemID.GOLDEN_NUGGET,ItemID.MITHRIL_ORE, ItemID.RUNITE_ORE, ItemID.ADAMANTITE_ORE))){
					state = "OPEN_BANK";
					timeout += 1;
					break;
				}
				if(colBag != null && x.getImpostor().getName().equalsIgnoreCase("Sack")){//Ready to be picked up
					event.setMenuEntry(pickUpShit());
					timeout += 2;
					state = "OPEN_BANK";
					break;
				}else{
					System.out.println("Something is wrong in EMPTY_SACK");
					break;
				}


		}
	}

	private String checkStuck(int x, int y) {
		if(x == 3757 ||  y ==5678){
			System.out.println("Got stuck on north rock");
			return "RETURN";
		}else if(x == 3758 ||  y ==5677){
			System.out.println("Got stuck on south rock");
			return "RETURN";
		}else if(x == 3755 ||  y ==5672){
			return "DEPOSIT";
		}else{//We just waiting to finsih coming down
			return null;
		}
	}



	private void doingLowerLevel(MenuOptionClicked event){
		log.debug("state = " + state);
		switch (state)
		{
			case "DETERMINE_STATE":
				if (!playerUtils.isItemEquipped(Set.of(ItemID.RUNE_PICKAXE,ItemID.DRAGON_PICKAXE,ItemID.MITHRIL_PICKAXE,ItemID.ADAMANT_PICKAXE,ItemID.BLACK_PICKAXE))){
					utils.sendGameMessage("No PickAxe Equipped, but its your Life");
				}else if(inventory.isFull() && !inventory.containsItem(ItemID.PAYDIRT)){
					utils.sendGameMessage("Empty Inventory before Starting");
					return;
				}
				state = "GO_TO_MINING_AREA";
				break;
			case "GO_TO_MINING_AREA":
				event.setMenuEntry(shortCutBankSide());
				timeout += 2;
				state = "WAIT_SHORTCUT";
				break;
			case "WAIT_SHORTCUT":

				//Wait for player to finsihs ShortCut
				if(client.getLocalPlayer().getWorldLocation().distanceTo(afterShortCutLocation)> 1){
					timeout +=1;
					return;
				}

				WorldPoint southRockPos = new WorldPoint(3766,5670,0);
				GameObject southRock = object.getGameObjectAtWorldPoint(southRockPos);
				//If we dont have to mines roock
				if(southRock == null){
					System.out.println("South rock is null");
					WorldPoint middleSpot = new WorldPoint(3768,5666,0);
					walkTile(middleSpot);
					state = "START_MINING";
					timeout += 1;
					break;
				}else{//If we have to mine the rock
					System.out.println("Mining Rock");
					event.setMenuEntry(mineRockAt(southRockPos));
					timeout +=2;
					return;
				}
			case "START_MINING":
				WorldPoint firstSpot = new WorldPoint(3768,5671,0);
				WorldPoint secondSpot = new WorldPoint(3766,5671,0);
				if(client.getLocalPlayer().getWorldLocation().distanceTo(firstSpot) == 0|| client.getLocalPlayer().getWorldLocation().distanceTo(secondSpot) == 0){
					//If we get stuck or Rock Respawns go back to mining step
					state = "WAIT_SHORTCUT";
					break;
				}
				//If all good start mining
				state = "MINING";
				break;
			case "MINING":
				if(inventory.isFull()){//If we have to stop mining
					state = "GO_BACK";
					break;
				}
				WorldPoint firstSpots = new WorldPoint(3768,5671,0);
				WorldPoint secondSpots = new WorldPoint(3766,5671,0);
				if(client.getLocalPlayer().getWorldLocation().distanceTo(firstSpots) == 0|| client.getLocalPlayer().getWorldLocation().distanceTo(secondSpots) == 0 || client.getLocalPlayer().getWorldLocation().distanceTo(afterShortCutLocation) == 0){
					//If we get stuck or Rock Respawns go back to mining step
					state = "WAIT_SHORTCUT";
					break;
				}
				if(config.useSpec()){
					if(client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000){//If we need to use Special Attack
						event.setMenuEntry(useSpecialAttack());
						return;
					}
				}

				WallObject closestWallThatWasFound = findClosestWallObject();
				if(closestWallThatWasFound == null){
					System.out.println("Wtomine are null");
					WallObject edged = checkEdgeCaseWalls();
					if(edged != null){//If edge case walls are alive
						event.setMenuEntry(mineWallAt(edged));
					}
					timeout += 3;
					break;
				}else{
					event.setMenuEntry(mineWallAt(closestWallThatWasFound));
					timeout +=3;
					break;
				}




			case "GO_BACK":
				WorldPoint southRPos = new WorldPoint(3766,5670,0);
				GameObject southRockReturn = object.getGameObjectAtWorldPoint(southRPos);
				//If we dont have to mines roock
				if(southRockReturn == null){
					System.out.println("South rock is null");
					event.setMenuEntry(shortCutBankSide());
					state = "DEPOSIT";
					timeout += 5;
					break;
				}else{//If we have to mine the rock
					System.out.println("Mining Rock");
					event.setMenuEntry(mineRockAt(southRPos));
					timeout +=2;
					return;
				}
			case "DEPOSIT":
				if(client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3766,5669,0)) == 0){//Check if got stuck on way back
					state = "GO_BACK";
					return;
				}
				if(client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3759,5670,0)) != 0){
					timeout += 4;
					return;
				}
				event.setMenuEntry(depositShit());
				timeout += 1;
				state = "GRAB_SHIT";
				break;
			case "GRAB_SHIT":
				if(client.getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3759,5670,0)) == 0){
					state = "DEPOSIT";
					timeout += 1;
					return;
				}
				if(inventory.containsItem(ItemID.PAYDIRT)){//If we are still running to deposit stuff
					timeout += 1;
					return;
				}
				if(loadsDeposited == 2){//We need to empty sack
					System.out.println("We need to empty sack");
					loadsDeposited += 1;
					state = "EMPTY_SACK";
					timeout += 1;
					break;
				}else{//DEposit and go back to minign
					loadsDeposited += 1;
					state = "GO_TO_MINING_AREA";
					timeout += 1;
					break;
				}

			case "OPEN_BANK":
				event.setMenuEntry(openDepositLoot());
				timeout += 2;
				state = "DEPOSIT_LOOT";
				break;
			case "DEPOSIT_LOOT":
				if(bank.isOpen() == false){
					timeout += 1;
					return;
				}
				tripsDone += 1;
				event.setMenuEntry(depositLoot());
				String sStat = getSackStatus();
				if(loadsDeposited != 0 && !sStat.equalsIgnoreCase("Empty Sack")){
					state = "EMPTY_SACK";
					loadsDeposited--;
					timeout += 3;
					break;
				}else if(loadsDeposited != 0 && sStat.equalsIgnoreCase("Empty Sack")){
					System.out.println("Loads Deposited isnt 0 but its broken");
					loadsDeposited = 1;
					state = "GO_TO_MINING_AREA";
					timeout += 1;
					break;
				}else if(loadsDeposited==0 && getSackStatus().equalsIgnoreCase("Empty Sack")){
					loadsDeposited =0;
					state = "GO_TO_MINING_AREA";
					timeout += 1;
					break;
				}else{
					utils.sendGameMessage("There is an errors Value of Loads is: " + loadsDeposited);
					state = "GO_TO_MINING_AREA";
					break;
				}
			case "EMPTY_SACK":
				GroundObject colBag = object.findNearestGroundObject(26688);
				ObjectComposition x = client.getObjectDefinition(26688);
				if(inventory.containsItem(Set.of(ItemID.COAL,ItemID.GOLDEN_NUGGET,ItemID.MITHRIL_ORE, ItemID.RUNITE_ORE, ItemID.ADAMANTITE_ORE))){
					state = "OPEN_BANK";
					timeout += 1;
					break;
				}
				if(colBag != null && x.getImpostor().getName().equalsIgnoreCase("Sack")){//Ready to be picked up
					event.setMenuEntry(pickUpShit());
					timeout += 2;
					state = "OPEN_BANK";
					break;
				}else{
					System.out.println("Something is wrong in EMPTY_SACK");
					break;
				}

		}

	}

	private WallObject findBestUpperLevelWall() {

		List<WallObject> validObject = new ArrayList<WallObject>();;
		List<WallObject> waobs = object.getWallObjects(26661,26664,26662,26663);

		for (int i = 0; i < waobs.size(); i++) {
			if(isUpstairs(waobs.get(i).getLocalLocation())){
				WorldPoint xx = waobs.get(i).getWorldLocation();
				int wox = xx.getX();
				int woy = xx.getY();
				if(wox >= 3755 && woy>=5675){
					if(wox == 3758 && woy == 5675){
						System.out.println("Invalid spot ourside of reach");
					}else if(wox == 3755 && woy == 5677){
						System.out.println("Invalid spot 2 outside of reach");
					}else{
						validObject.add(waobs.get(i));
					}

				}
			}
		}
		System.out.println(waobs.size() + " is size");
		if(validObject.size()== 0){
			wToMine = null;
			return null;
		}
		int distClosestWall = client.getLocalPlayer().getWorldLocation().distanceTo(validObject.get(0).getWorldLocation());
		WallObject closestWall = validObject.get(0);

		for (int i = 0; i < validObject.size(); i++) {
			int newDist = client.getLocalPlayer().getWorldLocation().distanceTo(validObject.get(i).getWorldLocation());
			if(newDist < distClosestWall){
				closestWall = validObject.get(i);
				distClosestWall = newDist;
			}
		}


		wToMine = closestWall;
		return closestWall;
	}

	private MenuEntry climbDownLadder(){
		GameObject ladder = object.findNearestGameObject(19045);

		return createMenuEntry(
				ladder.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(ladder).getX(),
				getLocation(ladder).getY(),
				true);
	}

	private MenuEntry climbUpLadder(){
		GameObject ladder = object.findNearestGameObject(19044);

		return createMenuEntry(
				ladder.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(ladder).getX(),
				getLocation(ladder).getY(),
				true);
	}

	private String getSackStatus(){
		GroundObject colBag = object.findNearestGroundObject(26688);
		ObjectComposition x = client.getObjectDefinition(26688);
		return x.getImpostor().getName();

	}
	private WallObject checkEdgeCaseWalls() {
		System.out.println("Edging analyssi");
		WorldPoint walloneLocation = new WorldPoint(3764,5660,0);
		WorldPoint walltwoLocation = new WorldPoint(3767,5657,0);
		WallObject wal1 = object.findWallObjectWithin(walloneLocation,1,26661);
		WallObject wal2 = object.findWallObjectWithin(walltwoLocation,1,26661);

		if(wal1 != null){
			System.out.println(wal1 + " wal1 isnt null so we return it");
			return wal1;
		}else if(wal2 != null){
			System.out.println(wal2 + " wal2 isnt null so we return it");
			return wal2;
		}else{
			System.out.println("Both of them are nul");
			return  null;
		}

	}

	private MenuEntry useSpecialAttack(){


		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				10485791,
				false);
	}

	private MenuEntry depositLoot(){


		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				786474,
				false);
	}

	private MenuEntry openDepositLoot(){
		GameObject bankChest = object.findNearestGameObject(26707);

		return createMenuEntry(
				bankChest.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(bankChest).getX(),
				getLocation(bankChest).getY(),
				true);
	}


	private MenuEntry pickUpShit(){
		GroundObject pickUpSack = object.findNearestGroundObject(26688);

		return createMenuEntry(
				pickUpSack.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(pickUpSack).getX(),
				getLocation(pickUpSack).getY(),
				true);
	}


	private MenuEntry depositShit(){
		GameObject despoitBox = object.findNearestGameObject(26674);

		return createMenuEntry(
				despoitBox.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(despoitBox).getX(),
				getLocation(despoitBox).getY(),
				true);
	}
	private MenuEntry mineWallAt(WallObject wo){


		return createMenuEntry(
				wo.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(wo).getX(),
				getLocation(wo).getY(),
				true);
	}

	private WallObject findClosestWallObject(){
		WorldPoint middleSpot = new WorldPoint(3768,5666,0);
		List<WallObject> validObject = new ArrayList<WallObject>();;
		List<WallObject> waobs = object.getWallObjects(26661,26664,26662);

		for (int i = 0; i < waobs.size(); i++) {

			if(waobs.get(i).getWorldLocation().getPlane() == 0){
				WorldPoint xx = waobs.get(i).getWorldLocation();
				int wox = xx.getX();
				int woy = xx.getY();
				if(woy < 5670 && woy > 5659 && wox > 3764 && wox < 3777){
					validObject.add(waobs.get(i));
				}
			}
		}
		if(validObject.size()== 0){
			wToMine = null;
			return null;
		}
		int distClosestWall = client.getLocalPlayer().getWorldLocation().distanceTo(validObject.get(0).getWorldLocation());
		WallObject closestWall = validObject.get(0);

		for (int i = 0; i < validObject.size(); i++) {
			int newDist = client.getLocalPlayer().getWorldLocation().distanceTo(validObject.get(i).getWorldLocation());
			if(newDist < distClosestWall){
				closestWall = validObject.get(i);
				distClosestWall = newDist;
			}
		}


		wToMine = closestWall;
		return closestWall;


	}

	private MenuEntry mineRockAt(WorldPoint w){
		GameObject rockToMine = object.getGameObjectAtWorldPoint(w);

		return createMenuEntry(
				rockToMine.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(rockToMine).getX(),
				getLocation(rockToMine).getY(),
				true);
	}

	private MenuEntry shortCutBankSide(){
		GameObject shortCutTunnel = object.findNearestGameObject(10047);

		return createMenuEntry(
				shortCutTunnel.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(shortCutTunnel).getX(),
				getLocation(shortCutTunnel).getY(),
				true);
	}



	public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick)
	{
		return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
				.setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
	}

	private boolean isBankOpen()
	{
		return client.getItemContainer(InventoryID.BANK) != null;
	}



	private Point getLocation(TileObject tileObject)
	{
		if (tileObject == null)
		{
			return new Point(0, 0);
		}
		if (tileObject instanceof GameObject)
			return ((GameObject) tileObject).getSceneMinLocation();
		return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
	}



	private void walkTile(WorldPoint worldpoint) {
		int x = worldpoint.getX() - client.getBaseX();
		int y = worldpoint.getY() - client.getBaseY();
		RSClient rsClient = (RSClient) client;
		rsClient.setSelectedSceneTileX(x);
		rsClient.setSelectedSceneTileY(y);
		rsClient.setViewportWalking(true);
		rsClient.setCheckClick(false);
	}

	boolean isUpstairs(LocalPoint localPoint)
	{
		return Perspective.getTileHeight(client, localPoint, 0) < UPPER_FLOOR_HEIGHT;
	}






}


