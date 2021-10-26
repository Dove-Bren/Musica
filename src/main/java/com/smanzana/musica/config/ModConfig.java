package com.smanzana.musica.config;

import java.util.HashSet;
import java.util.Set;

import com.smanzana.musica.Musica;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Musica.MODID)
@Config.LangKey("musica.config.title")
public class ModConfig {

//	public static enum Key {
//		SHOW_POPUP(Category.GENERAL, "show_popup", false, "Show the information popup the next time Minecraft is started. Defaults to true, and turned off when the popup is shown."),
//		DISABLE_VANILLA(Category.GENERAL, "disable_vanilla", true, "Disable vanilla Minecraft music but let other music set up by mods play"),
//		;
//		
//		
//		public static enum Category {
//			GENERAL("general", "General options for the Musica mod");
//			
//			private String categoryName;
//			
//			private String comment;
//			
//			private Category(String name, String tooltip) {
//				categoryName = name;
//				comment = tooltip;
//			}
//			
//			public String getName() {
//				return categoryName;
//			}
//			
//			@Override
//			public String toString() {
//				return getName();
//			}
//			
//			protected static void deployCategories(Configuration config) {
//				for (Category cat : values()) {
//					config.setCategoryComment(cat.categoryName, cat.comment);
//					config.setCategoryRequiresWorldRestart(cat.categoryName, false);
//					config.setCategoryLanguageKey(cat.categoryName,
//							"config.musica." + cat.categoryName);
//				}
//			}
//			
//		}
//		
//		private Category category;
//		
//		private String key;
//		
//		private String desc;
//		
//		private Object def;
//		
//		private Key(Category category, String key, Object def, String desc) {
//			this.category = category;
//			this.key = key;
//			this.desc = desc;
//			this.def = def;
//			
//			if (!(def instanceof Float || def instanceof Integer || def instanceof Boolean
//					|| def instanceof String)) {
//				Musica.logger.warn("Config property defaults to a value type that's not supported: " + def.getClass());
//			}
//		}
//		
//		protected String getKey() {
//			return key;
//		}
//		
//		protected String getDescription() {
//			return desc;
//		}
//		
//		protected String getCategory() {
//			return category.getName();
//		}
//		
//		protected Object getDefault() {
//			return def;
//		}
//		
//		/**
//		 * Returns whether this config option can be changed at runtime
//		 * @return
//		 */
//		public boolean isRuntime() {
//			return true;
//		}
//		
//		public void saveToNBT(ModConfig config, NBTTagCompound tag) {
//			if (tag == null)
//				tag = new NBTTagCompound();
//			
//			if (def instanceof Float)
//				tag.setFloat(key, config.getFloatValue(this)); 
//			else if (def instanceof Boolean)
//				tag.setBoolean(key, config.getBooleanValue(this));
//			else if (def instanceof Integer)
//				tag.setInteger(key, config.getIntValue(this));
//			else if (def.getClass().isArray())
//				tag.setIntArray(key,  config.getIntArrayValue(this));
//			else
//				tag.setString(key, config.getStringValue(this));
//		}
//
//		public Object valueFromNBT(NBTTagCompound tag) {
//			if (tag == null)
//				return null;
//			
//			if (def instanceof Float)
//				return tag.getFloat(key); 
//			else if (def instanceof Boolean)
//				return tag.getBoolean(key);
//			else if (def instanceof Integer)
//				return tag.getInteger(key);
//			else
//				return tag.getString(key);
//		}
//		
//		public Object getFromString(String val) {
//			if (val == null)
//				return null;
//			
//			Object out = null;
//			if (def instanceof Float) {
//				try {
//					out = Float.parseFloat(val);
//				} catch (NumberFormatException e) {
//					return null;
//				}
//			} else if (def instanceof Boolean) {
//				out = Boolean.parseBoolean(val);
//			}
//			else if (def instanceof Integer) {
//				try {
//					out = Integer.parseInt(val);
//				} catch (NumberFormatException e) {
//					return null;
//				}
//			}
//			else {
//				out = val;
//			}
//			
//			return out;
//		}
//		
//		public static Collection<Key> getCategoryKeys(Category category) {
//			Set<Key> set = new HashSet<Key>();
//			
//			for (Key key : values()) {
//				if (key.category == category)
//					set.add(key);
//			}
//			
//			return set;
//		}
//	}
//	
//	public static ModConfig config;
//	
//	public Configuration base;
//	
//	private Set<IConfigWatcher> watchers;
//	
//	public ModConfig(Configuration config) {
//		this.base = config;
//		this.watchers = new HashSet<IConfigWatcher>();
//		ModConfig.config = this;
//		
//		Key.Category.deployCategories(base);
//		initConfig();
//		
//		MinecraftForge.EVENT_BUS.register(this);
//		
//	}
//	
//	private void initConfig() {
//		for (Key key : Key.values())
//		/*if (!base.hasKey(key.getCategory(), key.getKey()))*/
//		{
//			if (key.getDefault() instanceof Float) {
//				base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
//						Float.MIN_VALUE, Float.MAX_VALUE, "This if float",//key.getDescription(),
//						"config.musica." + key.getCategory() + "." + key.getKey());
//			}
//			else if (key.getDefault() instanceof Boolean)
//				base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
//						key.getDescription(), "config.musica." + key.getCategory() + "." + key.getKey());
//			else if (key.getDefault() instanceof Integer)
//				base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
//						Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription(),
//						"config.musica." + key.getCategory() + "." + key.getKey());
//			else if (key.getDefault().getClass().isArray())
//				base.get(key.getCategory(), key.getKey(), (int[]) key.getDefault(),
//						key.getDescription());
//			else
//				base.getString(key.getKey(), key.getCategory(), key.getDefault().toString(),
//						key.getDescription(), "config.musica." + key.getCategory() + "." + key.getKey());
//		}
//		
//		if (base.hasChanged())
//			base.save();
//	}
//	
//	@SubscribeEvent
//	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
//		if(eventArgs.getModID().equals(Musica.MODID)) {
//
//			//tell each watcher the c onfig has been updated
//			if (watchers != null)
//			for (IConfigWatcher watcher : watchers) {
//				watcher.onConfigUpdate(this);
//			}
//		}
//		
//		base.save();
//	}
//	
//	public void registerWatcher(IConfigWatcher watcher) {
//		this.watchers.add(watcher);
//	}
//	
//	///////////////////////////////////////ENUM FILLING/////////
//	// I wanted to make this dynamic, but there's no
//	// configuration.get() that returns a blank object
//	////////////////////////////////////////////////////////////
//	protected boolean getBooleanValue(Key key) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		return base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
//				key.getDescription());
//	}
//
//	protected float getFloatValue(Key key) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		return base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
//				Float.MIN_VALUE, Float.MAX_VALUE, key.getDescription());
//	}
//
//	protected int getIntValue(Key key) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		return base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
//				Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription());
//	}
//	
//	protected int[] getIntArrayValue(Key key) {
//		int[] def = (int[]) key.getDefault();
//		return base.get(key.getCategory(), key.getKey(), def).getIntList();
//	}
//
//	protected String getStringValue(Key key) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		return base.getString(key.getKey(), key.getCategory(), (String) key.getDefault(),
//				key.getDescription());
//	}
//	
////	private Object getRawObject(Key key) {
////		if (key.getDefault() instanceof Float)
////			return getFloatValue(key); 
////		else if (key.getDefault() instanceof Boolean)
////			return getBooleanValue(key);
////		else if (key.getDefault() instanceof Integer)
////			return getIntValue(key);
////		else if (key.getDefault().getClass().isArray())
////			return getIntArrayValue(key);
////		else
////			return getStringValue(key);
////	}	
//	
//	public boolean shouldShowPopup() {
//		return getBooleanValue(Key.SHOW_POPUP);
//	}
//	
//	public boolean shouldDisableVanilla() {
//		return getBooleanValue(Key.DISABLE_VANILLA);
//	}
//	
//	public void setShouldShowPopup(boolean should) {
//		base.get(Key.SHOW_POPUP.getKey(), Key.SHOW_POPUP.getCategory(), (Boolean) Key.SHOW_POPUP.getDefault())
//			.set(should);
//	}
//	
//	public void setDisableVanilla(boolean disable) {
//		base.get(Key.DISABLE_VANILLA.getKey(), Key.DISABLE_VANILLA.getCategory(), (Boolean) Key.DISABLE_VANILLA.getDefault())
//			.set(disable);
//	}
	
	private static Set<IConfigWatcher> watchers = new HashSet<>();
	
	public static void registerWatcher(IConfigWatcher watcher) {
		watchers.add(watcher);
	}
	
	@Config.LangKey("config.musica.show_popup")
	@Config.Comment("Show the information popup the next time Minecraft is started. Defaults to true, and turned off when the popup is shown.")
	public static boolean showPopup = true;

	@Config.LangKey("config.musica.disable_vanilla")
	@Config.Comment("Disable vanilla Minecraft music but let other music set up by mods play")
	public static boolean disableVanilla = false;
	
	public static boolean shouldShowPopup() {
		return ModConfig.showPopup;
	}
	
	public static boolean shouldDisableVanilla() {
		return ModConfig.disableVanilla;
	}
	
	public static void setShouldShowPopup(boolean should) {
		ModConfig.showPopup = should;
		saveChanges();
	}
	
	public static void setDisableVanilla(boolean disable) {
		ModConfig.disableVanilla = disable;
		saveChanges();
	}
	
	protected static void notifyWatchers() {
		for (IConfigWatcher watcher : watchers) {
			watcher.onConfigUpdate();
		}
	}
	
	protected static void saveChanges() {
		ConfigManager.sync(Musica.MODID, Config.Type.INSTANCE);
		notifyWatchers();
	}
	
	@Mod.EventBusSubscriber(modid = Musica.MODID)
	private static class EventHandler {
		
		@SubscribeEvent
		public static void onConfigChanged(OnConfigChangedEvent event) {
			if (event.getModID().equals(Musica.MODID)) {
				ConfigManager.sync(Musica.MODID, Config.Type.INSTANCE);
				notifyWatchers();
			}
		}
	}
}
