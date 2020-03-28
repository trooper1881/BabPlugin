package com.trooper.babplugin;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;


public class main extends JavaPlugin 
{
	// vault
	private static Economy econ = null;
	private static Permission perms = null;
	private static Chat chat = null;

	public static int version = 15;
	// main timer for plugin
	Timer timer;
	// location of all baby data
	public static  Map<UUID,PlayerData> BABS = new HashMap<>();
	public static Map<String,ItemStack> CustomItemStacks = new HashMap<>();

	// location of all server config including diapers
	public static ServerConfig CONFIG = new ServerConfig();
	// location of babplugin
	public  static String SAVELOC = new String();


	// Fired when plugin is first enabled
	@Override
	public void onEnable() 
	{			

		System.out.println("[babplugin] Loading Babplugin Version "+version+". things just got REAL!.");
		SAVELOC = this.getDataFolder().getAbsolutePath();

		// FIND THE MANUAL OH NOO!

		// setup vault connection

		if (!setupEconomy() ) {
			System.out.println(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		setupChat();

		System.out.println("[babplugin] Commands Implemented.");

		// RTFM!
		gson.ReadConfig();



		// load objects
		try
		{

			CONFIG.CustomObjects.forEach((string,customobject) -> {LoadCustomObjects(customobject); });

		}

		catch(Exception e)
		{
			System.out.println("[babplugin] Reload With PlugMan Detected.");
		}
		System.out.println("[babplugin] Custom Objects loaded.");

		// load players
		for ( Player onlineuser : Bukkit.getOnlinePlayers())
		{
			gson.read(onlineuser);
		}
		System.out.println("[babplugin] Babs loaded.");


		// start listener
		getServer().getPluginManager().registerEvents(new MyHandlerClass(), this);

		// Implement commands
		this.getCommand("check").setExecutor(new CommandCheck());
		this.getCommand("change").setExecutor(new CommandChange());
		this.getCommand("babstats").setExecutor(new CommandStats());
		this.getCommand("load").setExecutor(new CommandLoad());
		this.getCommand("save").setExecutor(new CommandSave());
		this.getCommand("rubtummy").setExecutor(new CommandRubTummy());
		this.getCommand("tickle").setExecutor(new CommandTickle());
		this.getCommand("pee").setExecutor(new CommandPee());
		this.getCommand("poo").setExecutor(new CommandPoo());
		this.getCommand("optin").setExecutor(new CommandOptIn());
		this.getCommand("optout").setExecutor(new CommandOptIn());
		this.getCommand("setage").setExecutor(new CommandSetAge());
		this.getCommand("toilet").setExecutor(new CommandToilet());
		this.getCommand("setrole").setExecutor(new CommandSetRole());
		this.getCommand("setrace").setExecutor(new CommandSetRace());
		this.getCommand("paci").setExecutor(new CommandPaci());
		this.getCommand("verbosewet").setExecutor(new CommandVerboseBladder());
		this.getCommand("verbosemessy").setExecutor(new CommandVerboseBowel());
		this.getCommand("lockbowel").setExecutor(new CommandLockBowel());	
		this.getCommand("lockbladder").setExecutor(new CommandLockBladder());
		this.getCommand("invitecaretaker").setExecutor(new CommandInviteCareTaker());		
		this.getCommand("acceptcaretaker").setExecutor(new CommandAcceptCareTaker());
		this.getCommand("removecaretaker").setExecutor(new CommandRemoveCareTaker());
		this.getCommand("removeLittle").setExecutor(new CommandRemoveLittle());
		System.out.println("[babplugin] Commands Implemented.");


		// body functions timer
		timer = new Timer();
		// run timer every 30 seconds and start!
		timer.scheduleAtFixedRate(new BodyFunctionsTimer(), 30000, 30000);

		System.out.println("[babplugin] Bodyfunctions scheduled.");



	}


	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) 
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
		{
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	private boolean setupPermissions() 
	{
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public static Economy getEconomy() 
	{
		return econ;
	}

	public static Permission getPermissions() 
	{
		return perms;
	}

	public static Chat getChat() 
	{
		return chat;
	}

	public void unloadCustomObjects(CustomObject Object) 
	{
		/// this is how we do it!




		ItemStack NewItem = new ItemStack(Object.material);

		// The meta of the diamond sword where we can change the name, and properties of the item.
		ItemMeta meta = NewItem.getItemMeta();
		meta.setDisplayName(Object.Name);
		NewItem.setItemMeta(meta);

		NamespacedKey key = new NamespacedKey(this, Object.Name);

		// Create our custom recipe variable
		ShapedRecipe recipe = new ShapedRecipe(key, NewItem);

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape(Object.Shape[0],Object.Shape[1],Object.Shape[2]);


		// Set what the letters represent load material from config.
		Object.Ingredients.forEach((Char,mat) -> {recipe.setIngredient(Char,mat);});

		CustomItemStacks.put(Object.Name,NewItem);

		// Finally, remove the recipe from the bukkit recipes

		Iterator<Recipe> iter = getServer().recipeIterator();
		while (iter.hasNext()) {
			Recipe r = iter.next();

			if (r == recipe) {
				iter.remove();
			}
		}
	}


	public void LoadCustomObjects(CustomObject Object) 
	{
		/// this is how we do it!




		ItemStack NewItem = new ItemStack(Object.material);

		// The meta of the diamond sword where we can change the name, and properties of the item.
		ItemMeta meta = NewItem.getItemMeta();
		meta.setDisplayName(Object.Name);
		NewItem.setItemMeta(meta);

		NamespacedKey key = new NamespacedKey(this, Object.Name);

		// Create our custom recipe variable
		ShapedRecipe recipe = new ShapedRecipe(key, NewItem);

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape(Object.Shape[0],Object.Shape[1],Object.Shape[2]);


		// Set what the letters represent load material from config.
		Object.Ingredients.forEach((Char,mat) -> {recipe.setIngredient(Char,mat);});

		CustomItemStacks.put(Object.Name,NewItem);

		// Finally, add the recipe to the bukkit recipes

		Bukkit.addRecipe(recipe);
	}
	// Fired when plugin is disabled
	@Override
	public void onDisable() 
	{
		// stop main timer
		timer.cancel();
		System.out.println("[babplugin] body functions stopped.");
		// for each player in the babs storage find and write
		for ( UUID playeruuid : BABS.keySet())
		{

			Player player  = Bukkit.getPlayer(playeruuid);
			gson.write(player);
		}
		CONFIG.CustomObjects.forEach((string,customobject) -> {unloadCustomObjects(customobject); });
		System.out.println("[babplugin] disabled.");
	}
	// any bab variables we want to play with
	public static class PlayerData
	{
		Boolean optIn;
		int age;
		String gender;
		int wet;
		int messy;
		String diaper;
		String role;
		int pluginversion;
		String description;
		int bladder;
		int bowel;
		int bladdercontrol;
		int bowelcontrol;
		boolean sleepAnyway;
		boolean paci;
		String Stage;
		boolean lockbladder;
		boolean lockbowel;
		boolean verbosewet;
		boolean verbosemessy;
		String Race;

		// who takes care of you
		Map<UUID,CareTakerSettings> Caretakers = new HashMap<>();
		// who you take care of
		Map<UUID,LittleSettings> Littles = new HashMap<>();



	}

	public static class CareTakerSettings
	{
		Boolean accepted;
		String name;

	}
	public static class LittleSettings
	{
		String name;
	}

	public static class CustomObject
	{
		Material material;
		String Name;
		String[] Shape;
		Map<Character,Material> Ingredients = new HashMap<>();
	}


	public static class ServerConfig
	{
		int RegressionWet;
		int RegressionMess;
		int BodyFuctionTimerSpan;
		int NoWarningThreashHold;
		float BladderWarningPercentage;
		float BowelWarningPercentage;

		int Newborn;
		int Baby;
		int Toddler;
		int PreSchooler;
		int BigKid;
		int PreTeen;
		int Teen;
		int Adult;

		Map<String,UnderGarment> UnderGarments = new HashMap<>();
		Map<String,RegresStage> RegressStages = new HashMap<>();
		Map<String,CustomObject> CustomObjects = new HashMap<>();

	}
	public static class RegresStage
	{
		String StageIntro;
		String UseToilet;
		String JoinServer;
		String BladderFullBeforeBed;
		String BowelFullBeforeBed;
		String WetTheBed;
		String WetTheBedWake;
		String MessTheBed;
		String MessTheBedWake;
		String DiaperChecker;
		String DiaperChecked;
		String poo;
		String Pee;


	}
	public static class UnderGarment
	{
		// max value to still get bonus 
		int WetWet;
		int MessMessy;
		// Min value To get Negative effect
		int WetFull;
		int MessFull;
		//store postive effects
		ArrayList<PotionEffect> CleanEffects = new ArrayList<PotionEffect>();
		//store negative effects
		ArrayList<PotionEffect> DirtyEffects = new ArrayList<PotionEffect>();

		// msg for when you are changed into
		String Changeinto;
		String Changeconfirm;
		String TummyRubbed;
		String TummyRubber;
		String Tickled;
		String Tickler;
		String Poo;
		String Pee;
		// wet

		String WetMsgFirst;
		String WetMsgWet;
		String WetMsgfull;
		String WetMsgBursting;

		String WetWarnFirst;
		String WetWarnWet;
		String WetWarnfull;
		String WetWarnBursting;

		// mess

		String MessMsgFirst;
		String MessMsgMessy;
		String MessMsgFull;
		String MessMsgBursting;

		String MessWarnFirst;
		String MessWarnMessy;
		String MessWarnFull;
		String MessWarnBursting;


	}



	// handle save and load
	public static class gson 

	{
		public static void ReadConfig() 
		{
			File file = new File(SAVELOC , "config.json");

			boolean exists = file.exists();

			if (exists) 
			{
				try (FileReader reader = new FileReader(file)) 
				{
					// read CONFIG at plugins folder then convert from json into object then place in config
					CONFIG = GsonFactory.getPrettyGson().fromJson(reader,ServerConfig.class);
					// if baby wants to play
					System.out.println("[babplugin] loaded server config.");
				}
				catch (Exception e) 
				{
					System.out.println("[babplugin] Failed load server config.");
				} 
			}
			else
			{
				ServerConfig newconfig = new ServerConfig();
				newconfig.BodyFuctionTimerSpan = 30;
				newconfig.NoWarningThreashHold = 50;
				newconfig.BladderWarningPercentage = .75f;
				newconfig.BowelWarningPercentage = .75f;

				newconfig.Adult = 80;
				newconfig.Teen = 70;
				newconfig.PreTeen = 60;
				newconfig.BigKid = 50;
				newconfig.PreSchooler = 40;
				newconfig.Toddler=30;
				newconfig.Baby=20;
				newconfig.Newborn=10;

				RegresStage Adult = new RegresStage();
				RegresStage Teen = new RegresStage();
				RegresStage PreTeen = new RegresStage();
				RegresStage BigKid = new RegresStage();
				RegresStage PreSchooler = new RegresStage();
				RegresStage Toddler = new RegresStage();
				RegresStage Baby = new RegresStage();
				RegresStage Newborn = new RegresStage();

				Adult.BladderFullBeforeBed = "Forgot the bathroom in my routine.";
				Teen.BladderFullBeforeBed = "Woops, need to pee...";
				PreTeen.BladderFullBeforeBed = "Ugh, I don’t want to get back up to go to the bathroom!";
				BigKid.BladderFullBeforeBed = "I won’t pee the bed. I can make it to morning.";
				PreSchooler.BladderFullBeforeBed = "Dun wanna wet my bed… *Wriggle*";
				Toddler.BladderFullBeforeBed = "NO, potty!";
				Baby.BladderFullBeforeBed = "*SQUIRM*";
				Newborn.BladderFullBeforeBed = "*Giggle*";

				Adult.BowelFullBeforeBed = "Crap, how could I forget to use the bathroom?";
				Teen.BowelFullBeforeBed = "No way am I messing the bed.";
				PreTeen.BowelFullBeforeBed = "I am way too mature to poop in my sleep.";
				BigKid.BowelFullBeforeBed = "I should go to the bathroom first!";
				PreSchooler.BowelFullBeforeBed = "I don’t want to wake up stinky...";
				Toddler.BowelFullBeforeBed = "Bafwoom!";
				Baby.BowelFullBeforeBed = "Where’s my stuffie?";
				Newborn.BowelFullBeforeBed = "Coo...";


				Adult.DiaperChecked = "Hey! I’m an adult! I shouldn’t be getting checked by [agent-name]...";
				Teen.DiaperChecked = "OMG! I just got checked by [agent-name]!";
				PreTeen.DiaperChecked = "Come on... I’m mature! Why did I just get checked by [agent-name]?";
				BigKid.DiaperChecked = "Hey! People can see us... Don’t check me here, [agent-name]!";
				PreSchooler.DiaperChecked = "Pff, I dun need a change! But I just got checked by [agent-name]...";
				Toddler.DiaperChecked = "What, aren’t I clean? But I’m getting checked by [agent-name]...";
				Baby.DiaperChecked = "You squirm as you’re checked by [agent-name].";
				Newborn.DiaperChecked = "*Giggle* You’re being checked by [agent-name].";


				Adult.DiaperChecker = "You check [subject-name], but they are an adult.";
				Teen.DiaperChecker = "You check [subject-name], but they are a teenager.";
				PreTeen.DiaperChecker = "You check [subject-name] and find";
				BigKid.DiaperChecker = "You check  [subject-name]. They try so hard to stay clean.";
				PreSchooler.DiaperChecker = "Accidents happen,  [subject-name]. Don’t be surprised.";
				Toddler.DiaperChecker = "[subject-name] is just starting to potty train, but sometimes they forget.";
				Baby.DiaperChecker  = "Aww, did someone make a stinky,  [subject-name]?";
				Newborn.DiaperChecker = "Who is just the cutest?  [subject-name]!";


				Adult.JoinServer = "Welcome back! Shouldn't you be working or something?";
				Teen.JoinServer = "Welcome back! Ready for more digging?";
				PreTeen.JoinServer = "Welcome back! Time to play!";
				BigKid.JoinServer = "Welcome back! Did you sleep well?";
				PreSchooler.JoinServer = "Welcome back! Ready for another big day?";
				Toddler.JoinServer = "Welcome back! Oh, you’re adorable!";
				Baby.JoinServer = "Welcome back! Who is the cutest?";
				Newborn.JoinServer = "Welcome back! Aww!";


				Adult.MessTheBed = "After waking, you realize you soiled the bed at some point in your sleep... How will you live with the shame?";
				Teen.MessTheBed = "You feel a mushy squish as you sit up… No way! How did I poop my bed without even waking up?";
				PreTeen.MessTheBed = "It doesn’t take long for you to notice the dirty feeling against your rump… Wha… Did I really poop myself while sleeping?";
				BigKid.MessTheBed = "A rather yucky scent greets you as you finally wake… Oh no! I actually messed my bed! But I’m no baby!";
				PreSchooler.MessTheBed = "Something doesn’t feel quite right... Uh-oh... I went poopy in my sleep!";
				Toddler.MessTheBed = "You wake to a new day, feeling something smush underneath your weight… I dun think I should still be wakin’ up smelly...";
				Baby.MessTheBed = "Waking up stinky in the morning is expected for you.";
				Newborn.MessTheBed = "You don’t even notice the smell from your pants as you wake up to a new morning.";


				Adult.WetTheBed = "Waking up from a full night’s rest, you realize you’ve wet your bed! So much for being an adult...";
				Teen.WetTheBed = "You eventually rise from your night of slumber, only to notice a cold wetness telling you that you peed in your sleep like some sort of little kid!";
				PreTeen.WetTheBed = "Your eyes quickly widen as you wake up from a restful night of sleep and catch the unexpected scent of pee.";
				BigKid.WetTheBed = "Wait, I’m wet? Did I really pee while sleeping? Only little kids should do that...";
				PreSchooler.WetTheBed = "I can’t keep wetting the bed like a baby… I didn’t even wake up while going!";
				Toddler.WetTheBed = "I’ll stop bedwetting eventually! Good thing I can sleep through it.";
				Baby.WetTheBed = "Waking up wet is no big deal. You feel fully rested, and that’s all that matters!";
				Newborn.WetTheBed = "After waking up from a restful sleep, you barely notice the wet feeling between your legs.";


				Adult.MessTheBedWake = "You wake up tonight to a gross feeling forming in your pants... YUCK! Adults don’t mess the bed!";
				Teen.MessTheBedWake = "You’re woken from your sleep early as you feel your lower muscles tensed, as if pushing something out… You gasp in shock! You can’t be pooping your bed as a teenager!";
				PreTeen.MessTheBedWake = "Consciousness returns to you earlier than expected as you wake to the sensation of emptying your bowels into your pants… But something like this should only happen to babies!";
				BigKid.MessTheBedWake = "Your slumber is interrupted early tonight, as you wake to the feeling of fading urgency and a muddy sensation in the seat of your pants… You blush intensely as you realize you just messed your bed like a little baby!";
				PreSchooler.MessTheBedWake = "You groggily rub your eyes, still feeling sleepy, while a yucky smell enters your nostrils… *Gasp* You’ve just poopied yourself in bed!";
				Toddler.MessTheBedWake = "Your slumber is interrupted as you hear yourself grunting and feel something exiting your rump… You realize what you’re doing and feel a little disappointed that potty training isn’t taking in your sleep.";
				Baby.MessTheBedWake = "A feeling of fullness in your lower body is fading away as you wake up a bit early to your liking. You just feel like rolling over and going back to sleep.";
				Newborn.MessTheBedWake = "Your eyes blink open during the night as you feel your lower belly getting emptier while your pants feel fuller. You wanna giggle to the funny feeling and shut your eyes again.";

				Adult.WetTheBedWake = "You wake up to a warmth spreading from your groin tonight… YUCK! Adults don’t wet the bed!";
				Teen.WetTheBedWake = "You wake up to a wet bed. teens don’t wet the bed!";
				PreTeen.WetTheBedWake = "Consciousness returns to you earlier than expected as you wake to the sensation of emptying your bladder into your pants… But something like this should only happen to babies!";
				BigKid.WetTheBedWake = "Your slumber is interrupted early tonight, as you wake to the feeling of fading urgency and a wet sensation in the groin of your pants… You blush intensely as you realize you just wet your bed like a little baby!";
				PreSchooler.WetTheBedWake = "You groggily rub your eyes, still feeling sleepy, while you notice *Gasp* You’ve wet the bed!";
				Toddler.WetTheBedWake = "Your slumber is interrupted as you find yourself squirming and feel yourself peeing… You realize what you’re doing and feel a little disappointed that potty training isn’t taking in your sleep.";
				Baby.MessTheBedWake =  "A feeling of fullness in your lower body is fading away as you wake up a bit early to your liking. You just feel like rolling over and going back to sleep.";
				Newborn.MessTheBedWake =  "Your eyes blink open during the night as you feel your lower belly getting emptier while your pants feel wetter. You wanna giggle to the funny feeling and shut your eyes again.";


				Adult.Pee = "You cringe at the pain of holding yourself this long before sighing in defeat as you feel your control slip, resulting in your [pants-type] growing quite wet between your legs!";
				Teen.Pee = "You struggle at holding your bladder. in defeat as you feel your control slip, resulting in your [pants-type] becoming wet!";
				PreTeen.Pee = "You struggle at holding your bladder.tears in the corners of your eyes you lower your head in defeat as you feel your control slip, resulting in your [pants-type] becoming wet!";
				BigKid.Pee = "Your attempt at holding your bladder for so long finally begins to fail, your [pants-type] becoming as warm as your face as you start wetting.";
				PreSchooler.Pee = "Your cheeks flush a little as you feel yourself suddenly starting to wet your [pants-type]!";
				Toddler.Pee = "Your cheeks flush a little as you feel a comforting warmth in your [pants-type]!";
				Baby.Pee = "You barely notice the front of your [pants-type] growing warmer while you play.";
				Newborn.Pee = "You don't even notice your [pants-type] growing warmer while you play.";


				Adult.poo = "ugh I am going to have to change my [pants-type] right now";
				Teen.poo = "what was I thinking now there is a huge mess in my [pants-type]";
				PreTeen.poo = "what if someone can smell that I pooped my [pants-type] on purpose";
				BigKid.poo = "what was I thinking I am supposed to be older then pooping in my [pants-type]";
				PreSchooler.poo = "uh oh the adults are going to get mad! I hope they dont notice my poopy [pants-type]";
				Toddler.poo = "It's so warm! Giggle! I really should find someone to change my [pants-type]";
				Baby.poo = "What smells bad? Oh! It's me! I must have pooped my [pants-type]";
				Newborn.poo = "Giggle";

				Adult.StageIntro = "Ahh, you have made it to the stage of taxes and jobs. Welcome to adulthood.";
				Teen.StageIntro = "You feel a little angry and confused. You must be a teen.";
				PreTeen.StageIntro = "Welcome to being a preteen! Time to play games.";
				BigKid.StageIntro = "Your now old enough to go to school and to be trusted to not make a mess in your undies";
				PreSchooler.StageIntro = "Maybe someday you will be big enough for undies again but just focus on keeping your bed dry";
				Toddler.StageIntro = "Aww you're adorable make sure you tell an adult when you need to use the bathroom";
				Baby.StageIntro = "who is just the cutest little baby ever!";
				Newborn.StageIntro = "Oh no you have lost all control of everything!";

				Adult.UseToilet = "You use the toilet. This feels right.";
				Teen.UseToilet = "You take care of business!";
				PreTeen.UseToilet = "at least  you don't have to explain wet [pants-type] again";
				BigKid.UseToilet ="aww your a big kid now!";
				PreSchooler.UseToilet = "yay! You did it!";
				Toddler.UseToilet = "EEEP you luckily escape the toilet monster";
				Baby.UseToilet = "be careful the toilet monster will eat you";
				Newborn.UseToilet = "OH NO! The toilet monster is going to get you crawl away!";

				newconfig.RegressStages.put("Adult", Adult);
				newconfig.RegressStages.put("Teen", Teen);
				newconfig.RegressStages.put("PreTeen", PreTeen);
				newconfig.RegressStages.put("BigKid", BigKid);
				newconfig.RegressStages.put("PreSchooler", PreSchooler);
				newconfig.RegressStages.put("Toddler", Toddler);
				newconfig.RegressStages.put("Baby", Baby);
				newconfig.RegressStages.put("Newborn", Newborn);

				// this is how you hardcode a diaper now

				UnderGarment Undies = new UnderGarment();
				Undies.MessMessy = 0;
				Undies.WetWet = 0;
				Undies.MessFull = 2;
				Undies.WetFull = 2;

				Undies.Changeconfirm = "You get cleaned up and change into a clean pair of undies.";
				Undies.Changeinto = "You got cleaned up and changed into a clean pair of undies by [agent-name].";

				Undies.Pee = "yellow stained undies";
				Undies.Poo = "now filled undies";

				Undies.Tickler = "You tickled [subject-name] until they wet their undies. The puddle forming underneath them is definitely your fault!";
				Undies.Tickled = "You were tickled mercilessly by [agent-name] until you wet your undies. The puddle forming underneath you is totally their fault!";

				Undies.TummyRubber = "You rub [subject-name]’s tummy, knowing they are only wearing undies. Then you very quickly realize they need a change of pants as soon as possible… What a mess!";
				Undies.TummyRubbed = "Your tummy was rubbed by [agent-name] until you made a mess in your undies. You need a change of pants as soon as possible… What a mess!";

				Undies.MessMsgFirst = "your undies are messy so much for staying clean";
				Undies.MessMsgMessy = "your undies are now very messy go change";
				Undies.MessMsgFull = "poop is everywhere this is getting out of hand";
				Undies.MessMsgBursting = "just call a hazmat team";

				Undies.MessWarnFirst = "hey. You might want to go to the bathroom before you smell up the place.";
				Undies.MessWarnMessy = "the room already smells of you. If you don't want to make it worse, go find a toilet.";
				Undies.MessWarnFull = "there is no hope. Send a rescue team.";
				Undies.MessWarnBursting = "well that pair of undies is ruined forever but if you're not careful your going to make it worse.";

				Undies.WetMsgFirst = "your undies turn yellow as you stain them.";
				Undies.WetMsgWet = "your already yellow undies are soaked";
				Undies.WetMsgfull = "your leaving a trail";
				Undies.WetMsgBursting = "you need to be hosed down";

				Undies.WetWarnFirst = "hey you need to use the bathroom unless you want to stain your undies";
				Undies.WetWarnWet = "they are already wet. But you should really try to make it to the bathroom";
				Undies.WetWarnfull = "I am not convinced your mature enough for undies";
				Undies.WetWarnBursting = "yeah. Your going to wet your undies again. I think you like it.";



				Undies.CleanEffects.add(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1));

				Undies.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1));
				Undies.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,1));

				newconfig.UnderGarments.put("undies",Undies);

				UnderGarment Pullup = new UnderGarment();
				Pullup.MessMessy = 1;
				Pullup.WetWet = 3;
				Pullup.MessFull = 3;
				Pullup.WetFull = 5;

				Pullup.Changeconfirm = "You clean up [subject-name] and help them into a new pull-up.";
				Pullup.Changeinto = "You got cleaned up and helped into a new pull-up by [agent-name].";

				Pullup.Pee = "yellow stained pull-up";
				Pullup.Poo = "now filled pull-up";

				Pullup.Tickler = "You tickled [subject-name] until they wet their pull-up. They’ll be getting a rain cloud sticker this time!";
				Pullup.Tickled = "You were tickled mercilessly by [agent-name] until you wet your pull-up. You’ll be getting a rain cloud sticker this time!";

				Pullup.TummyRubber = "You rub [subject-name]’s tummy, knowing they are in a pull-up. Potty training can wait, but the smell now rising from their pants can’t!";
				Pullup.TummyRubbed = "Your tummy was rubbed by [agent-name] until you made a mess in your pull-up. So much for potty training!";

				Pullup.MessMsgFirst = "your pullup fills with poop";
				Pullup.MessMsgMessy = "your already messy pullup is now stinkier";
				Pullup.MessMsgFull = "your pullup is very full. If you go again it might explode";
				Pullup.MessMsgBursting = "EXPLOSION!";

				Pullup.MessWarnFirst = "you feel full and your about to make a mess in your clean pullup";
				Pullup.MessWarnMessy = "your slightly messy pull up will get more messy soon";
				Pullup.MessWarnFull = "your pullup is a mess and it's going to get worse soon";
				Pullup.MessWarnBursting = "this pullup is beyond full. Adding more at this point will hardly make a difference";

				Pullup.WetMsgFirst = "your pullup sags slightly as you wet";
				Pullup.WetMsgWet = "your saggy pullup is almost full";
				Pullup.WetMsgfull = "your saggy pullup is now completely full";
				Pullup.WetMsgBursting = "your pullup didnt soak up any more liquid instead it just ran down your legs";

				Pullup.WetWarnFirst = "you need to pee. Your pullup is dry why not try using the toilet?";
				Pullup.WetWarnWet = "your bladder is getting full again and your pullup is damp tough choice";
				Pullup.WetWarnfull = "your pullup is really saggy it's time for the bathroom again";
				Pullup.WetWarnBursting = "it's going to puddle at your feet time to change your pullup";


				Pullup.CleanEffects.add(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,0));

				Pullup.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1));
				Pullup.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,1));

				newconfig.UnderGarments.put("pullup",Pullup);

				UnderGarment ThickDiaper = new UnderGarment();
				ThickDiaper.MessMessy = 3;
				ThickDiaper.WetWet = 7;
				ThickDiaper.MessFull = 8;
				ThickDiaper.WetFull = 5;

				ThickDiaper.Changeconfirm = " You give [subject-name] a change, putting them into a thick diaper. They’re going to walk with a noticeable waddle!";
				ThickDiaper.Changeinto = "You got changed by [agent-name], who put you into a thick diaper. You’re going to walk with a noticeable waddle!";

				ThickDiaper.Pee = "yellow stained thick diaper";
				ThickDiaper.Poo = "now filled thick diaper";

				ThickDiaper.Tickler = "You tickled [subject-name] until they wet their diaper. That’s what they’re wearing those for, after all!";
				ThickDiaper.Tickled = "You were tickled mercilessly by [agent-name] until you wet your diaper. That’s what you’re wearing these for, after all!";

				ThickDiaper.TummyRubber = "You rub [subject-name]’s tummy, knowing they are wearing a thick diaper. With so much padding between their legs, you’d barely be able to tell they added a bit of a bulge to their seat, but you can certainly smell it!";
				ThickDiaper.TummyRubbed = "Your tummy was rubbed by [agent-name] until you made a mess in your thick diaper. With so much padding between your legs, it’d be hard for someone to tell you added a bit of a bulge to your seat, but everyone can certainly smell it!";

				ThickDiaper.MessMsgFirst = "your thick diaper now has a little poop in it  but its a big diaper";
				ThickDiaper.MessMsgMessy = "your diaper slows you down because its getting a little full";
				ThickDiaper.MessMsgFull = "the thick diaper sags a little lower as you empty into it again";
				ThickDiaper.MessMsgBursting = "you are dragging a 20 pound sack around its time for a change";

				ThickDiaper.MessWarnFirst = "you feel a rumble in your tummy you will have to go poop soon";
				ThickDiaper.MessWarnMessy = "your slightly poopy diaper is about to get more poopy luckily it is a thick diaper and can handle it";
				ThickDiaper.MessWarnFull = "your thick diaper is full you might want to think about changing it before you have to poop soon";
				ThickDiaper.MessWarnBursting = "your thick diaper is going to make it impossible to walk you need to change";

				ThickDiaper.WetMsgFirst = "your thick diaper is now a little wet but its nothing";
				ThickDiaper.WetMsgWet = "your thick diaper is now a little saggy buts its a big diaper";
				ThickDiaper.WetMsgfull = "your thick diaper is really slowing you down now";
				ThickDiaper.WetMsgBursting = "you can hardly move your thick diaper is like trying to walk with a watermelon between your legs";

				ThickDiaper.WetWarnFirst = "you feel like you need to pee your thick diaper can handle it";
				ThickDiaper.WetWarnWet = "you feel the urge to pee again not like it matters";
				ThickDiaper.WetWarnfull = "you need to pee again maybe you're drinking too much liquids";
				ThickDiaper.WetWarnBursting = "I don't know if you have noticed but it's almost impossible to walk and its about to get harder";


				ThickDiaper.CleanEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,1));
				ThickDiaper.CleanEffects.add(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,0));
				ThickDiaper.CleanEffects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,0));


				ThickDiaper.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1));
				ThickDiaper.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,1));

				newconfig.UnderGarments.put("thickdiaper",ThickDiaper);


				UnderGarment Diaper = new UnderGarment();
				Diaper.MessMessy = 2;
				Diaper.WetWet = 5;
				Diaper.MessFull = 4;
				Diaper.WetFull = 7;

				Diaper.Changeconfirm = " You give [subject-name] a change, putting them into a fresh diaper. A telltale crinkling follows their every step!";
				Diaper.Changeinto = "You got changed by [agent-name], who put you into a fresh diaper. A telltale crinkling follows your every step!";

				Diaper.Pee = "yellow stained diaper";
				Diaper.Poo = "now filled diaper";

				Diaper.Tickler = "You tickled [subject-name] until they wet their diaper. It’s not like they weren’t going to do that on their own, anyway, right?";
				Diaper.Tickled = "You were tickled mercilessly by [agent-name] until you wet your diaper. You get the feeling they expected you’d end up wetting yourself eventually, anyway!";

				Diaper.TummyRubber = "You rub [subject-name]’s tummy, knowing they are wearing a diaper. They now no longer smell of just baby powder, but this is why they’re padded in the first place!";
				Diaper.TummyRubbed = "Your tummy was rubbed by [agent-name] until you made a mess in your diaper. You now no longer smell of just baby powder, but this is why you’re padded in the first place!";

				Diaper.MessMsgFirst = "your body grunts and you fill your clean diaper";
				Diaper.MessMsgMessy = "your already messy diaper gets a little messier";
				Diaper.MessMsgFull = "the smell already bad but you add to it";
				Diaper.MessMsgBursting = "someone needs an adult and a diaper change and a bath.";

				Diaper.MessWarnFirst = "your tummy grumbles it's time to go to the toilet";
				Diaper.MessWarnMessy = "your messy diaper is going to get messier soon";
				Diaper.MessWarnFull = "your full diaper cant handle much more you should change";
				Diaper.MessWarnBursting = "its spread up your back now and your diaper is useless and your going to add more?";

				Diaper.WetMsgFirst = "you feel your bladder empty giving you a warm feeling";
				Diaper.WetMsgWet = "your warm wet diaper just got a little wetter";
				Diaper.WetMsgfull = "your full diaper is now starting to leak";
				Diaper.WetMsgBursting = "your diaper leaks everywhere making a mess";

				Diaper.WetWarnFirst = "your bladder is full it's time to go to the bathroom";
				Diaper.WetWarnWet = "your diaper is a little wet and going to get wetter";
				Diaper.WetWarnfull = "you think about using the toilet but at this point you might as well use your diaper";
				Diaper.WetWarnBursting = "your soaked diaper cant hold anymore it's going to be a mess";


				Diaper.CleanEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,0));

				Diaper.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1));
				Diaper.DirtyEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,1));

				newconfig.UnderGarments.put("diaper",Diaper);
				// custom object section

				CustomObject ThickDiaperObj = new CustomObject();
				ThickDiaperObj.material = Material.PINK_WOOL;
				ThickDiaperObj.Name = "thickdiaper";
				ThickDiaperObj.Shape = new String[]{"SWS","SWS","WWW"};
				ThickDiaperObj.Ingredients.put('W',Material.WHITE_WOOL);
				ThickDiaperObj.Ingredients.put('S',Material.SLIME_BALL);
				newconfig.CustomObjects.put("thickdiaper",ThickDiaperObj);

				CustomObject DiaperObj = new CustomObject();
				DiaperObj.material = Material.WHITE_WOOL;
				DiaperObj.Name = "diaper";
				DiaperObj.Shape = new String[]{"   ","SWS","WWW"};
				DiaperObj.Ingredients.put('W',Material.WHITE_WOOL);
				DiaperObj.Ingredients.put('S',Material.SLIME_BALL);
				newconfig.CustomObjects.put("diaper",DiaperObj);

				CustomObject PullupObj = new CustomObject();
				PullupObj.material = Material.YELLOW_WOOL;
				PullupObj.Name = "pullup";
				PullupObj.Shape = new String[]{"   ","YWY","WWW"};
				PullupObj.Ingredients.put('W',Material.WHITE_WOOL);
				PullupObj.Ingredients.put('Y',Material.YELLOW_WOOL);
				newconfig.CustomObjects.put("pullup",PullupObj);

				CustomObject UndiesObj = new CustomObject();
				UndiesObj.material = Material.LIGHT_GRAY_WOOL;
				UndiesObj.Name = "undies";
				UndiesObj.Shape = new String[]{"   ","SWS"," W "};
				UndiesObj.Ingredients.put('W',Material.WHITE_WOOL);
				UndiesObj.Ingredients.put('S',Material.STRING);
				newconfig.CustomObjects.put("undies",UndiesObj);



				// MERRRRGGGEEE
				CONFIG = newconfig;

				writeConfig();
			}
		}
		// you should never need to call this except when you first start
		public static void writeConfig() 
		{
			// made out of life serial
			//Gson gson = new Gson();
			File file = new File(SAVELOC , "config.json");
			File directory = new File (SAVELOC);

			if (!directory.exists())
			{
				directory.mkdir();
			}
			try (FileWriter file1 = new FileWriter(file)) 
			{

				//File Writer creates a file in write mode at the given location 
				//,ServerConfig.class
				file1.write (GsonFactory.getPrettyGson().toJson(CONFIG));
				file1.flush();
			}
			catch (Exception E)
			{

			}
		}


		public static void optin( Player player) 
		{
			// toggle if bab wants to play

			// find the uuid for the player
			UUID babUUID = player.getUniqueId();

			File file = new File(SAVELOC , babUUID.toString() + ".json");

			boolean exists = file.exists();

			// first time user or save corruption make new from birth
			if (!exists)
			{

				PlayerData newborn = new PlayerData();
				newborn.pluginversion = version;
				newborn.age = 0;
				newborn.bladdercontrol =90;
				newborn.bowelcontrol =90;
				newborn.optIn = true;
				newborn.diaper = "diaper";
				newborn.role = "new";
				BABS.put(babUUID,newborn);
				write(player);

				msg(player,"Welcome to the babplugin version "+version+".");

			}
			else
			{

				try 
				{
					// not sure something to do with raisins
					Gson gson = new Gson();
					// read file at plugins folder then convert from json into object then add into babs
					PlayerData baby = gson.fromJson(new FileReader(file),PlayerData.class);
					// if baby wants to play
					if (baby.optIn)
					{
						baby.optIn = false;
						BABS.put(babUUID,baby);
						write(player);
						BABS.remove(babUUID);
						msg(player, "you have opted out");

					}
					else if (!baby.optIn)
					{
						//read(player);

						baby.optIn = true;
						BABS.put(babUUID,baby);
						msg(player,"you have opted in");
					}
				}

				catch ( FileNotFoundException e) 
				{
					e.printStackTrace();
				} 
			}
		}

		public static void read( Player player) 
		{

			// find the uuid for the player
			UUID babUUID = player.getUniqueId();

			File file = new File(SAVELOC , babUUID.toString() + ".json");

			boolean exists = file.exists();

			if (exists) 
			{
				try 
				{
					// not sure something to do with raisins
					Gson gson = new Gson();
					// read file at plugins folder then convert from json into object then add into babs
					PlayerData baby = gson.fromJson(new FileReader(file),PlayerData.class);
					// if baby wants to play
					if (baby.optIn)
					{
						BABS.put(babUUID,baby);
					}
				}
				catch ( FileNotFoundException e) 
				{
					e.printStackTrace();
				} 
			}
		}

		public static void write( Player player) 
		{
			UUID babUUID = player.getUniqueId();
			Gson gson = new Gson();
			File file = new File(SAVELOC,babUUID.toString() +".json");
			File directory = new File(SAVELOC);

			if (! directory.exists())
			{
				directory.mkdir();
			}
			try (FileWriter file1 = new FileWriter(file)) 
			{
				//File Writer creates a file in write mode at the given location 
				file1.write(gson.toJson(BABS.get(babUUID)));
				file1.flush();
			}
			catch ( IOException e) {
				e.printStackTrace();

			}
		}
	}

	//handle all change on server
	public class MyHandlerClass implements Listener
	{


		@EventHandler
		public void AsyncPlayerChatEvent ( org.bukkit.event.player.AsyncPlayerChatEvent event)
		{
			PlayerData playerdata = BABS.get(event.getPlayer().getUniqueId());
			Player player = event.getPlayer();
			String name = event.getPlayer().getDisplayName();
			String race = playerdata.Race;
			String role = playerdata.role;
			String rank = getPermissions().getPrimaryGroup(event.getPlayer());



			ChatColor rankcolor = ChatColor.WHITE;
			rankcolor = ChatColor.WHITE;
			if (getPermissions().playerInGroup(event.getPlayer(), "tree-muncher")) { rankcolor = ChatColor.GRAY;}
			if (getPermissions().playerInGroup(event.getPlayer(), "coal-cruncher")) { rankcolor = ChatColor.BLUE;}
			if (getPermissions().playerInGroup(event.getPlayer(), "iron-bapper")) { rankcolor = ChatColor.DARK_GRAY;}
			if (getPermissions().playerInGroup(event.getPlayer(), "gold-musher")) { rankcolor = ChatColor.GOLD;}
			if (getPermissions().playerInGroup(event.getPlayer(), "redstone-spiller")) { rankcolor = ChatColor.DARK_RED;}
			if (getPermissions().playerInGroup(event.getPlayer(), "blue-shinys")) { rankcolor = ChatColor.AQUA;}
			if (getPermissions().playerInGroup(event.getPlayer(), "green-shinys")) { rankcolor = ChatColor.GREEN;}
			if (getPermissions().playerInGroup(event.getPlayer(), "black-shinys")) { rankcolor = ChatColor.BLACK;}
			if (getPermissions().playerInGroup(event.getPlayer(), "glowy-shinys")) { rankcolor = ChatColor.YELLOW;}
			if (getPermissions().playerInGroup(event.getPlayer(), "hatchy-egg")) { rankcolor = ChatColor.RED;}
			if (getPermissions().playerInGroup(event.getPlayer(), "hatchling")) { rankcolor = ChatColor.DARK_AQUA;}
			if (getPermissions().playerInGroup(event.getPlayer(), "neberite-hewo")) { rankcolor = ChatColor.DARK_PURPLE;}

			//if (playerdata.messy >=  playerdata.diaper) 
			event.setFormat( rank +  "[" + rankcolor + role + " " + race + ChatColor.WHITE + "] " + name+": " + event.getMessage()); 
		}


		@EventHandler
		public void playerJoined( PlayerLoginEvent event)

		{
			gson.read(event.getPlayer());

			Player player = (Player) event.getPlayer();
			UUID babUUID = player.getUniqueId();
			if (BABS.containsKey(babUUID))
			{
				msg(player, CONFIG.RegressStages.get(BABS.get(babUUID).Stage).JoinServer);
			}
		}
		@EventHandler
		public void playerQuit( PlayerQuitEvent event)
		{
			Player player = event.getPlayer();
			UUID playerUUID = player.getUniqueId();
			if (!BABS.containsKey(playerUUID)) {return;}

			gson.write(event.getPlayer());
			// clean up clean up everybab do there share
			BABS.remove(event.getPlayer().getUniqueId());

		}

		@EventHandler 
		public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) 
		{
			Player player = event.getPlayer();

			if(!player.isSneaking()) {return;}


			Player baby = (Player) player;
			UUID babUUID = baby.getUniqueId();
			if (!BABS.containsKey(babUUID)) {return;}
			if (!(baby.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.CAULDRON)) 
			{return;}

			UseToilet(baby,babUUID);



		}
		@EventHandler 
		public void playerEnterBed( PlayerBedEnterEvent event)


		{


			Player player = (Player) event.getPlayer();
			UUID babUUID = player.getUniqueId();
			if (BABS.containsKey(babUUID))
			{

				PlayerData baby = BABS.get(babUUID);

				int warningThreashHold = 40;
				int youAreBab = 20;

				// if you havent already tried to sleep
				if (!baby.sleepAnyway)
				{
					// if your bladder is full enough for you to notice AND your adult enough to know its full AND you havent tried to sleep before
					if (baby.bladdercontrol > warningThreashHold && baby.bladder > Math.round(baby.bladdercontrol*.50))
					{
						msg(player, CONFIG.RegressStages.get(baby.Stage).BladderFullBeforeBed);
						baby.sleepAnyway = true;
						event.setUseBed(Event.Result.DENY);
					}

					// if your bladder is full enough for you to notice AND your adult enough to know its full AND you havent tried to sleep before		
					if (baby.bowelcontrol > warningThreashHold && baby.bowel > Math.round(baby.bowelcontrol*.50 ))
					{
						msg(player, CONFIG.RegressStages.get(baby.Stage).BowelFullBeforeBed);
						event.setUseBed(Event.Result.DENY);
						baby.sleepAnyway = true;
					}
					baby.sleepAnyway = true;
				}

				else if (baby.sleepAnyway)
				{
					// if you went to bed dry you get more control
					if (baby.wet == 0) {baby.bladdercontrol++;};
					if (baby.messy == 0) {baby.bowelcontrol++;};
					{
						// if your bladder fullness is greater then 75% full
						if (baby.bladder > Math.round(baby.bladdercontrol*.75))
						{
							// you wet the bed 
							baby.wet++;
							baby.bladder=0;

							msg(player, CONFIG.RegressStages.get(baby.Stage).WetTheBed);
							// if your mature enough to wake up because you wet the bed
							if (baby.bladdercontrol > youAreBab)
							{
								msg(player, CONFIG.RegressStages.get(baby.Stage).WetTheBedWake);
								event.setUseBed(Event.Result.DENY);
							}
						}

						if (baby.bowel > Math.round(baby.bowelcontrol*.75))
						{
							baby.messy++;
							baby.bowel=0;
							msg(player, CONFIG.RegressStages.get(baby.Stage).MessTheBed);

							if (baby.bowelcontrol > youAreBab)
							{
								msg(player, CONFIG.RegressStages.get(baby.Stage).MessTheBedWake);
								event.setUseBed(Event.Result.DENY);
							}
						}
						baby.sleepAnyway = false;



					}
				}
				BABS.put(player.getUniqueId(),baby);
			}
		}
	}



	//this.getCommand("removecaretaker").setExecutor(new CommandRemoveCareTaker());
	//this.getCommand("removeLittle").setExecutor(new CommandRemoveLittle());

	// /acceptcaretaker 
	public class CommandRemoveCareTaker implements CommandExecutor
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();
			if (BABS.containsKey(babUUID))
			{
				PlayerData bab = BABS.get(babUUID);


				// convert speach to baby talk give some bonus
				Player caretaker;
				UUID caretakerUUID;
				try
				{
					caretaker = Bukkit.getPlayer(args[0]);
					caretakerUUID = caretaker.getUniqueId();
				}
				catch (Exception E)
				{
					msg((Player) sender, "Are you sure they are exist? I dont see them" );
					return false;
				}

				if (bab.Caretakers.containsKey(caretakerUUID))
				{

					CareTakerSettings Cts = bab.Caretakers.get(caretakerUUID);
					// if accepted
					if(Cts.accepted == true)
					{
						msg((Player) sender, "removed your CareTaker" );
						return true;
					}
					if(Cts.accepted == false)
					{
						msg((Player) sender, "they have not accepted your offer removing the request." );
						return true;
					}
					bab.Caretakers.remove(caretakerUUID);
					BABS.put(babUUID, bab);
					return true;
				}
				else
				{

					msg((Player) sender, "they are not on your list of caretakers." );
					return false;
				}
			}
			return true;
		}
	} 
	public class CommandRemoveLittle implements CommandExecutor
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player caretaker = (Player) sender;
			UUID caretakerUUID = caretaker.getUniqueId();
			if (BABS.containsKey(caretakerUUID))
			{

				// convert speach to baby talk give some bonus
				Player little;
				PlayerData littledata;
				UUID littleUUID;
				try
				{
					little = Bukkit.getPlayer(args[0]);
					littleUUID = little.getUniqueId();
					littledata = BABS.get(littleUUID);
				}
				catch (Exception E)
				{
					msg((Player) sender, "Are you sure they exist? I dont see them" );
					return false;
				}
				// if little data does not contain caretaker uuid
				if (!littledata.Caretakers.containsKey(caretakerUUID))
				{
					msg((Player) sender, "you are not the babs caretaker" );
					return true;
				}
				else
				{

					CareTakerSettings Cts = littledata.Caretakers.get(caretakerUUID);
					// if accepted
					if(Cts.accepted == true)
					{
						msg((Player) sender, "removing " );

					}
					if(Cts.accepted == false)
					{
						msg((Player) sender, "you never accepted. removing request." );

					}
					littledata.Caretakers.remove(caretakerUUID);
					BABS.put(littleUUID,littledata);			


				}
			}


			return true;
		}
	} 

	public class CommandAcceptCareTaker implements CommandExecutor
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player caretaker = (Player) sender;
			UUID caretakerUUID = caretaker.getUniqueId();
			if (BABS.containsKey(caretakerUUID))
			{
				// convert speach to baby talk give some bonus
				Player little;
				PlayerData littledata;
				UUID littleUUID;
				try
				{
					little = Bukkit.getPlayer(args[0]);
					littleUUID = little.getUniqueId();
					littledata = BABS.get(littleUUID);
				}
				catch (Exception E)
				{
					msg((Player) sender, "Are you sure they exist? I dont see them" );
					return false;
				}
				// if little data does not contain caretaker uuid
				if (!littledata.Caretakers.containsKey(caretakerUUID))
				{
					msg((Player) sender, "Bab has not send you a request to be caretaker" );
					return true;
				}
				else
				{

					CareTakerSettings Cts = littledata.Caretakers.get(caretakerUUID);
					// if accepted
					if(Cts.accepted == true)
					{
						msg((Player) sender, "you are already there CareTaker" );
						return true;
					}
					if(Cts.accepted == false)
					{
						msg((Player) sender, "you have accepted to be Caretaker." );
						Cts.accepted = true;
						littledata.Caretakers.put(caretakerUUID,Cts);
						BABS.put(littleUUID,littledata);
						return true;
					}


				}
			}


			return true;
		}
	} 


	public class CommandInviteCareTaker implements CommandExecutor
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();
			if (BABS.containsKey(babUUID))
			{
				PlayerData bab = BABS.get(babUUID);


				// convert speach to baby talk give some bonus
				Player caretaker;
				UUID caretakerUUID;
				try
				{
					caretaker = Bukkit.getPlayer(args[0]);
					caretakerUUID = caretaker.getUniqueId();
				}
				catch (Exception E)
				{
					msg((Player) sender, "Are you sure they are exist? I dont see them" );
					return false;
				}

				if (bab.Caretakers.containsKey(caretakerUUID))
				{

					CareTakerSettings Cts = bab.Caretakers.get(caretakerUUID);
					// if accepted
					if(Cts.accepted == true)
					{
						msg((Player) sender, "They are already your CareTaker" );
						return true;
					}
					if(Cts.accepted == false)
					{
						msg((Player) sender, "they have not accepted. you can try asking them very nicely." );
						return true;
					}

				}
				else
				{
					CareTakerSettings Cts = new CareTakerSettings();
					Cts.accepted = false;
					Cts.name = args[0];
					bab.Caretakers.put(caretakerUUID,Cts);
					return true;
				}



				BABS.put(babUUID, bab);
			}


			return true;
		}
	} 


	public class CommandPaci implements CommandExecutor
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();
			if (BABS.containsKey(babUUID))
			{
				PlayerData bab = BABS.get(babUUID);
				// convert speach to baby talk give some bonus

				//toggle paci
				bab.paci ^=true;
				if (bab.paci == true) {msg((Player) sender,"You put a Paci in your mouth");}
				if (bab.paci == false) {msg((Player) sender,"You remove a Paci from your mouth");}
				BABS.put(babUUID, bab);
			}


			return true;
		}
	} 
	public class CommandChange implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			///code for making sure the changer is in range future impliment
			///if (event.getPlayer().canSee(Bukkit.getPlayer(words[1])))
			///getLogger().info(event.getPlayer().getDisplayName() + "saw" + Bukkit.getPlayer(words[1]) );
			///	double distance = p.getLocation().distance(shot.getLocation());		
			///	event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().toString() == "wool"


			Player baby;
			UUID babUUID;
			String undergarment;

			// undergarments does not contain garment instead of a name
			if (!CONFIG.UnderGarments.containsKey(args[0]))
			{

				try
				{
					baby = Bukkit.getPlayer(args[0]);
					babUUID = baby.getUniqueId();
				}
				catch (Exception E)
				{
					msg((Player) sender, "Are you sure they are exist? I dont see them" );
					return false;
				}

				if (!BABS.containsKey(babUUID))
				{
					msg((Player) sender, "Are you sure they are here? and Opted in?" );
					return false;
				}
				// 
				if(!CONFIG.UnderGarments.containsKey(args[1]))
				{
					msg((Player) sender, CONFIG.UnderGarments.keySet().toString() );
					return false;
				}

				undergarment = args[1];

			}
			// if args 0 is the name of a diaper
			else
			{
				baby = (Player) sender;
				babUUID = baby.getUniqueId();
				if(CONFIG.UnderGarments.containsKey(args[0]))
				{
					undergarment = args[0];

				}
				else
				{
					msg((Player) sender, CONFIG.UnderGarments.keySet().toString() );
					return false;
				}
			}

			if (!BABS.containsKey(babUUID)) {msg((Player) sender, "bab has opted out"); return false;}

			// get stats of the bab 
			PlayerData changedbab = BABS.get(babUUID);
			// change stats related to a change
			changedbab.wet = 0;
			changedbab.messy = 0;

			Player changer = (Player) sender;

			ItemStack check = CustomItemStacks.get(undergarment);
			for (int I = 0;I < 64;I++)
			{
				check.setAmount(I);
				if (changer.getInventory().contains(CustomItemStacks.get(undergarment)))
				{
					int stack = changer.getInventory().first(CustomItemStacks.get(undergarment));
					ItemStack itemstack = changer.getInventory().getItem(stack);
					int itemsInStack = itemstack.getAmount();
					if (itemsInStack > 1) 
					{
						itemsInStack--;
						itemstack.setAmount(itemsInStack);
						changer.getInventory().setItem(stack,itemstack);
						changer.updateInventory();

						changedbab.diaper = undergarment;
						// save stats about player in map
						BABS.put(babUUID,changedbab);
						// change potion effects to fit
						dirty(baby.getPlayer());

						// get the change into line

						String changeinto = CONFIG.UnderGarments.get(undergarment).Changeinto;
						String changeconfirm = CONFIG.UnderGarments.get(undergarment).Changeconfirm;

						msgSubject(baby,(Player) sender, changeinto );
						msgAgent((Player) sender, baby, changeconfirm );
						return true;

					}
					else 
					{
						changer.getInventory().removeItem(itemstack);

						changedbab.diaper = undergarment;
						// save stats about player in map
						BABS.put(babUUID,changedbab);
						// change potion effects to fit
						dirty(baby.getPlayer());

						// get the change into line

						String changeinto = CONFIG.UnderGarments.get(undergarment).Changeinto;
						String changeconfirm = CONFIG.UnderGarments.get(undergarment).Changeconfirm;

						msgSubject(baby,(Player) sender, changeinto );
						msgAgent((Player) sender, baby, changeconfirm );
						return true;

					}

				}
			}

			msg((Player) sender, "you dont have a " + undergarment + " in your inventory."  );
			return false;


		}
	}
	public class CommandCheck implements CommandExecutor 
	{
		@Override

		public boolean onCommand( CommandSender sender,  Command cmd,  String statsplayer,  String[] args) 
		{

			// check if the msg comes from a player if not return
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}


			try
			{
				Player baby = Bukkit.getPlayer(args[0]);
				UUID babUUID = baby.getUniqueId();

				if (!BABS.containsKey(babUUID)) {sender.sendMessage("that bab has opted out"); return false;}

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				msg((Player) sender, CONFIG.RegressStages.get(BABS.get(babUUID).Stage).DiaperChecker);
				String stats = gson.toJson(BABS.get(babUUID));
				stats = stats.replace('"', ' ');
				stats = stats.replace('{', ' ');
				stats = stats.replace('}', ' ');
				stats = stats.replace(',', ' ');
				msg((Player) sender, stats );
				msgSubject(baby,(Player)sender,CONFIG.RegressStages.get(BABS.get(babUUID).Stage).DiaperChecked);
			}
			catch(Exception e)
			{
				msg((Player) sender, " are you sure they are here?");
			}

			return true;
		}
	}
	public class CommandStats implements CommandExecutor 
	{
		@Override

		public boolean onCommand( CommandSender sender,  Command cmd,  String statsplayer,  String[] args) 
		{
			// list the bab statistics untested
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}
			Player player = (Player) sender;
			UUID babUUID = player.getUniqueId();			
			if (!BABS.containsKey(babUUID)) {sender.sendMessage("you have  opted out"); return false;}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String stats = gson.toJson(BABS.get(babUUID));
			stats = stats.replace('"', ' ');
			stats = stats.replace('{', ' ');
			stats = stats.replace('}', ' ');
			stats = stats.replace(',', ' ');
			msg((Player) sender, stats );

			return true;
		}

	}
	public class CommandSetAge implements CommandExecutor 
	{
		@Override

		public boolean onCommand( CommandSender sender,  Command cmd,  String statsplayer,  String[] args) 
		{
			// set player age this may effect maturity

			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID babUUID = player.getUniqueId();
			PlayerData Babstat = BABS.get(babUUID);

			if (!BABS.containsKey(babUUID)) {msg(player, " you are opted out"); return false;}

			try
			{
				Babstat.age = Integer.parseInt(args[0]);

				msg(player,"age set");

				BABS.put(babUUID,Babstat);
			}
			catch(Exception e)
			{
				msg(player," are you sure thats a number?");
			}



			return true;
		}

	}
	public class CommandSetRole implements CommandExecutor 
	{
		@Override

		public boolean onCommand( CommandSender sender,  Command cmd,  String statsplayer,  String[] args) 
		{
			// list the bab statistics untested
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID babUUID = player.getUniqueId();
			PlayerData Babstat = BABS.get(babUUID);

			if (!BABS.containsKey(babUUID)) {msg(player, " you are opted out"); return false;}

			Babstat.role = args[0];

			msg(player, " role set");
			BABS.put(babUUID,Babstat);

			return true;
		}
	}

	public class CommandSetRace implements CommandExecutor 
	{
		@Override

		public boolean onCommand( CommandSender sender,  Command cmd,  String statsplayer,  String[] args) 
		{
			// list the bab statistics untested
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID babUUID = player.getUniqueId();
			PlayerData Babstat = BABS.get(babUUID);

			if (!BABS.containsKey(babUUID)) {msg(player, " you are opted out"); return false;}

			Babstat.Race = args[0];

			msg(player, " race set");
			BABS.put(babUUID,Babstat);

			return true;
		}
	}

	public class CommandSave implements CommandExecutor

	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID babUUID = player.getUniqueId();

			if (!(BABS.containsKey(babUUID))) {msg((Player) sender,"They have opted out"); return false;}

			gson.write(player);
			msg(player, " saved");

			return true;
		}
	}
	public class CommandLoad implements CommandExecutor 

	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{

			// fixed a bug here make sure this works

			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}


			Player player = (Player) sender;

			gson.read(player);
			msg(player, " loaded");

			return true;
		}
	}
	public class CommandRubTummy implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{

			if (args[0].isEmpty()) {msg((Player) sender,"correct form is /rubtummy target"); return false;}
			Player baby = Bukkit.getPlayer(args[0]);
			UUID babUUID = baby.getUniqueId();

			if (!(BABS.containsKey(babUUID))) {msg((Player) sender,"They have opted out"); return false;}

			// get stats of the bab 
			PlayerData changedbab = BABS.get(babUUID);

			changedbab.messy++;

			// save stats about player in map
			BABS.put(babUUID,changedbab);

			// you rubbed there belly causing them to mess there tummyrubber
			// agent rubbed your belly till it caused you to mes your tummyrubbed
			msgAgent((Player) sender,baby,CONFIG.UnderGarments.get(changedbab.diaper).TummyRubber);
			msgSubject(baby,(Player) sender, CONFIG.UnderGarments.get(changedbab.diaper).TummyRubbed );

			dirty(baby.getPlayer());

			return true;
		}
	}
	public class CommandTickle implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{

			if (args[0].isEmpty()) {msg((Player) sender,"correct form is /tickle target"); return false;}
			Player baby = Bukkit.getPlayer(args[0]);
			UUID babUUID = baby.getUniqueId();

			if (!(BABS.containsKey(babUUID))) {msg((Player) sender,"They have opted out"); return false;}
			System.out.println("uuid :" + babUUID.toString());
			// get stats of the bab 
			PlayerData changedbab = BABS.get(babUUID);

			changedbab.wet++;

			// save stats about player in map
			BABS.put(babUUID,changedbab);

			msgAgent((Player) sender,baby,CONFIG.UnderGarments.get(changedbab.diaper).Tickler);
			msgSubject(baby,(Player) sender, CONFIG.UnderGarments.get(changedbab.diaper).Tickled );

			dirty(baby.getPlayer());


			return true;
		}
	}
	public class CommandPoo implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{

			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();
			if (!(BABS.containsKey(babUUID))) {msg((Player) sender,"They have opted out"); return false;}

			//System.out.println("uuid :" + babUUID.toString());
			// get stats of the bab 
			PlayerData changedbab = BABS.get(babUUID);

			changedbab.messy++;

			// save stats about player in map
			BABS.put(babUUID,changedbab);
			// format is (you feel silly as you)(poop in your diaper)
			msg(baby,CONFIG.RegressStages.get(changedbab.Stage).poo);

			dirty(baby.getPlayer());

			return true;
		}
	}
	public class CommandPee implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{

			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();

			if (!(BABS.containsKey(babUUID))) {msg((Player) sender,"They have opted out"); return false;}

			PlayerData changedbab = BABS.get(babUUID);

			changedbab.wet++;

			// save stats about player in map
			BABS.put(babUUID,changedbab);
			// format is (you feel silly as you)(poop in your diaper)
			msg(baby, CONFIG.RegressStages.get(changedbab.Stage).Pee);
			dirty(baby.getPlayer());

			return true;

		}
	}
	public class CommandOptIn implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			gson.optin(player);

			return true;
		}
	}
	public class CommandLockBowel implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			PlayerData pottybab = BABS.get(uuid);
			pottybab.lockbowel = !pottybab.lockbowel;
			BABS.put(uuid,pottybab);
			return true;
		}
	}
	public class CommandLockBladder implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			PlayerData pottybab = BABS.get(player.getUniqueId());
			pottybab.lockbladder = !pottybab.lockbladder;
			BABS.put(uuid,pottybab);
			return true;
		}
	}

	public class CommandVerboseBladder implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			PlayerData pottybab = BABS.get(player.getUniqueId());
			pottybab.verbosewet = !pottybab.verbosewet;
			BABS.put(uuid,pottybab);
			return true;
		}
	}
	public class CommandVerboseBowel implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			PlayerData pottybab = BABS.get(player.getUniqueId());
			pottybab.verbosemessy = !pottybab.verbosemessy;
			BABS.put(uuid,pottybab);
			return true;
		}
	}

	public class CommandToilet implements CommandExecutor 
	{
		@Override
		public boolean onCommand( CommandSender sender,  Command cmd,  String commandword,  String[] args) 
		{
			if (!(sender instanceof Player)){ msg((Player) sender,"You must be a player!"); return false;}

			Player baby = (Player) sender;
			UUID babUUID = baby.getUniqueId();
			if (!BABS.containsKey(babUUID)) {msg((Player) sender,"you didnt want to play" ); return false;}

			if (!(baby.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.CAULDRON)) 
			{msg(baby,"your not on the potty" ); return false;}

			UseToilet(baby, babUUID);

			return true;
		}


	}

	public void UseToilet(Player baby, UUID babUUID) 
	{
		PlayerData pottybab = BABS.get(babUUID);
		if ((pottybab.bowel > 0 || pottybab.bladder > 0))
		{
			pottybab.bowel = 0;
			pottybab.bladder = 0;
			pottybab.bladdercontrol++;
			pottybab.bowelcontrol++;
			BABS.put(babUUID,pottybab);
			msg(baby,CONFIG.RegressStages.get(pottybab.Stage).UseToilet );
		}
		else if (pottybab.bladdercontrol < 20 || pottybab.bowelcontrol < 20)
		{
			pottybab.bowel = 0;
			pottybab.bladder = 0;
			pottybab.bladdercontrol++;
			pottybab.bowelcontrol++;
			BABS.put(babUUID,pottybab);
			msg(baby,CONFIG.RegressStages.get(pottybab.Stage).UseToilet );
		}
	}

	public void dirty(Player player)
	{
		PlayerData babystats = BABS.get(player.getUniqueId());

		for ( PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}

		UnderGarment undergarment = CONFIG.UnderGarments.get(babystats.diaper);

		if (babystats.wet <= undergarment.WetWet  && babystats.messy <= undergarment.MessMessy )
		{
			player.getPlayer().addPotionEffects(undergarment.CleanEffects);
		}
		if (babystats.wet > undergarment.WetFull || babystats.messy > undergarment.MessFull)
		{
			player.getPlayer().addPotionEffects(undergarment.DirtyEffects);
		}
		// regression

		if (babystats.wet > CONFIG.RegressionWet && babystats.bladdercontrol > 0 && babystats.lockbladder == false) {babystats.bladdercontrol--; }
		if (babystats.messy > CONFIG.RegressionMess && babystats.bowelcontrol > 0 && babystats.lockbowel == false) {babystats.bowelcontrol--; }
	}

	class BodyFunctionsTimer extends TimerTask 
	{
		public void run() 
		{
			// this checks to see if anyone is online if not skip all steps
			if (!Bukkit.getOnlinePlayers().isEmpty())
			{
				try
				{
					for ( UUID playeruuid : BABS.keySet())
					{

						PlayerData baby = BABS.get(playeruuid);
						UnderGarment garment = CONFIG.UnderGarments.get(baby.diaper);
						Player onlineuser = Bukkit.getPlayer(playeruuid);
						if (baby.optIn)
						{

							baby = SetStage(baby);
							baby = WetDiaperUpdate(baby, garment, onlineuser);
							baby = MessyDiaperUpdate(baby, garment, onlineuser);

							BABS.put(onlineuser.getUniqueId(),baby);
						}
					}
				}
				catch ( Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		public PlayerData SetStage(PlayerData baby) {
			double stage = (baby.bladdercontrol + baby.bowelcontrol)/2;
			if (stage >= 0 && stage < CONFIG.Newborn) {baby.Stage = "Newborn";}
			else if (stage >= CONFIG.Newborn && stage < CONFIG.Baby){baby.Stage = "Baby";}
			else if (stage >= CONFIG.Baby && stage < CONFIG.Toddler){baby.Stage = "Toddler";}
			else if (stage >= CONFIG.Toddler && stage < CONFIG.PreSchooler){baby.Stage = "PreSchooler";}
			else if (stage >= CONFIG.PreSchooler && stage < CONFIG.BigKid){baby.Stage = "BigKid";}
			else if (stage >= CONFIG.BigKid && stage < CONFIG.PreTeen){baby.Stage = "PreTeen";}
			else if (stage >= CONFIG.PreTeen && stage < CONFIG.Teen){baby.Stage = "Teen";}
			else if (stage >= CONFIG.Teen){baby.Stage = "Adult"; }
			return baby;
		}

		public PlayerData MessyDiaperUpdate(PlayerData baby, UnderGarment garment, Player onlineuser) {
			baby.bowel = baby.bowel + ((int)(2.0 * Math.random()));
			//if (baby.bowelcontrol > CONFIG.NoWarningThreashHold && baby.bowel == Math.round(baby.bowelcontrol*CONFIG.BowelWarningPercentage))

			if ((baby.bowelcontrol > CONFIG.NoWarningThreashHold ||  baby.bowelcontrol + ((int)(100.0 * Math.random())) >= 75) && baby.bowel == Math.round(baby.bowelcontrol*CONFIG.BowelWarningPercentage) && baby.verbosewet == true)
			{


				// first time
				if(baby.messy == 0) {msg(onlineuser, garment.MessWarnFirst);}
				// until the garment is saturated
				else if(baby.messy > 0 && baby.messy <= garment.MessMessy) {msg(onlineuser, garment.MessWarnMessy);}
				// until garment is full
				else if(baby.messy > garment.MessMessy && baby.messy <= garment.MessFull) {msg(onlineuser, garment.MessWarnFull);}
				// until garment is bursting
				else if(baby.messy > garment.MessFull) {msg(onlineuser, garment.MessWarnBursting);}
			}

			if (baby.bowel > baby.bowelcontrol)
			{
				baby.messy++;
				baby.bowel=0;

				if (baby.bowelcontrol > CONFIG.NoWarningThreashHold || baby.bowel + ((int)(100.0 * Math.random())) >= 75 && baby.verbosemessy == true)
				{

					if(baby.messy == 0) {msg(onlineuser, garment.MessMsgFirst);}
					// until the garment is saturated
					else if(baby.messy > 0 && baby.messy <= garment.MessMessy) {msg(onlineuser, garment.MessMsgMessy);}
					// garment is full
					else if(baby.messy > garment.MessMessy && baby.messy <= garment.MessFull) {msg(onlineuser, garment.MessMsgFull);}
					// garment is full
					else if(baby.messy > garment.MessFull) {msg(onlineuser, garment.MessMsgBursting);}
				}
			}
			return baby;
		}

		public PlayerData WetDiaperUpdate(PlayerData baby, UnderGarment garment, Player onlineuser) {
			baby.bladder = baby.bladder + ((int)(2.0 * Math.random()));

			if ((baby.bladdercontrol > CONFIG.NoWarningThreashHold ||  baby.bladdercontrol + ((int)(100.0 * Math.random())) >= 75) && baby.bladder == Math.round(baby.bladdercontrol*CONFIG.BladderWarningPercentage) && baby.verbosewet == true)
			{

				// first time
				if(baby.wet == 0) {msg(onlineuser, garment.WetWarnFirst);}
				// until the garment is saturated
				else if(baby.wet > 0 && baby.wet <= garment.WetWet) {msg(onlineuser, garment.WetWarnWet);}
				// garment is full
				else if(baby.wet > garment.WetWet && baby.wet <= garment.WetFull) {msg(onlineuser, garment.WetWarnfull);}
				// garment is bursting
				else if(baby.wet > garment.WetFull) {msg(onlineuser, garment.WetWarnBursting);}

			}
			// determine if you have wet your self
			if (baby.bladder > baby.bladdercontrol)
			{
				baby.wet++;
				baby.bladder=0;
				if (baby.bladdercontrol > CONFIG.NoWarningThreashHold || baby.bladdercontrol + ((int)(100.0 * Math.random())) >= 75 && baby.verbosewet == true)
				{
					// first time
					if(baby.wet == 0) {msg(onlineuser, garment.WetMsgFirst);}
					// until the garment is saturated
					else if(baby.wet > 0 && baby.wet <= garment.WetWet) {msg(onlineuser, garment.WetMsgWet);}
					// garment is full
					else if(baby.wet > garment.WetWet && baby.wet <= garment.WetFull) {msg(onlineuser, garment.WetMsgfull);}
					// garment is full
					else if(baby.wet > garment.WetFull) {msg(onlineuser, garment.WetMsgBursting);}
				}
			}
			return baby;
		}

	}


	// used for when you do something to you
	public static void msg(Player subject, String msg) 
	{//[player-name] and [pants-type]

		if (msg.contains("[subject-name]")) {msg = msg.replace("[subject-name]", subject.getName());}
		if (msg.contains("[pants-type]")) {msg = msg.replace("[pants-type]", BABS.get(subject.getUniqueId()).diaper);}
		if (msg.contains("[player-name]")) {msg = msg.replace("[player-name]",subject.getName());}

		subject.sendMessage(ChatColor.RED + "babplugin " + ChatColor.GREEN + msg );
	}

	public static void msgAgent(Player agent,Player subject, String msg)
	{//[player-name] and [pants-type]

		if (msg.contains("[subject-name]")) {msg = msg.replace("[subject-name]", subject.getName());}
		if (msg.contains("[agent-name]")) {msg = msg.replace("[agent-name]", agent.getName());}
		if (msg.contains("[pants-type]")) {msg = msg.replace("[pants-type]", BABS.get(subject.getUniqueId()).diaper);}
		if (msg.contains("[player-name]")) {msg = msg.replace("[player-name]",subject.getName());}

		agent.sendMessage(ChatColor.RED + "babplugin " + ChatColor.GREEN + msg );
	}
	// used for when someone does something to you
	public static void msgSubject(Player subject,Player agent, String msg) 
	{

		if (msg.contains("[subject-name]")) {msg = msg.replace("[subject-name]", subject.getName());}
		if (msg.contains("[agent-name]")) {msg = msg.replace("[agent-name]", agent.getName());}
		if (msg.contains("[player-name]")) {msg = msg.replace("[player-name]", subject.getName());}
		if (msg.contains("[pants-type]")) {msg = msg.replace("[pants-type]", BABS.get(subject.getUniqueId()).diaper);}
		subject.sendMessage(ChatColor.RED + "babplugin " + ChatColor.GREEN + msg );
	}











}







