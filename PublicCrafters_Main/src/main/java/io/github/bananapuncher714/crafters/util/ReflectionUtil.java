package io.github.bananapuncher714.crafters.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.implementation.API.CraftInventoryManager;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.bananapuncher714.nbteditor.NBTEditor.MinecraftVersion;

/**
 * Yet another simplified reflection utility to load the proper {@link CraftInventoryManager} for each version;
 * Created on 2017-12-07
 * 
 * @author BananaPuncher714
 */
public final class ReflectionUtil {
	private static Class< CraftInventoryManager > containerManager = null;
	private static String version;

	private static final Map< String, Class<?> > classCache;
	private static final Map< String, Method > methodCache;
	private static final Map< Class< ? >, Constructor< ? > > constructorCache;
	private static Field connection;
	private static Object entityTypeArmorStand;
	
	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			containerManager = ( Class< CraftInventoryManager > ) Class.forName( "io.github.bananapuncher714.crafters.implementation." + version + ".ContainerManager_" + version );
		} catch ( ClassNotFoundException e) {
			PublicCrafters.getInstance().getLogger().severe( "'" + version + "' is not implemented at the moment! Please contact BananaPuncher714 for future support!" );
		}
		
		classCache = new HashMap< String, Class<?> >();
		try {
			classCache.put( "CraftPlayer", Class.forName( "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer" ) );
			classCache.put( "PlayerConnection", Class.forName( "net.minecraft.server." + version + "." + "PlayerConnection" ) );
			classCache.put( "DataWatcher", Class.forName( "net.minecraft.server." + version + "." + "DataWatcher" ) );
			
			classCache.put( "CraftWorld", Class.forName( "org.bukkit.craftbukkit." + version + "." + "CraftWorld" ) );
			classCache.put( "CraftItemStack", Class.forName( "org.bukkit.craftbukkit." + version + ".inventory." + "CraftItemStack" ) );
			classCache.put( "CraftArmorStand", Class.forName( "org.bukkit.craftbukkit." + version + ".entity.CraftArmorStand" ) );
			classCache.put( "ItemStack", Class.forName( "net.minecraft.server." + version + "." + "ItemStack" ) );
			
			classCache.put( "EntityArmorStand", Class.forName( "net.minecraft.server." + version + "." + "EntityArmorStand" ) );
			classCache.put( "Entity", Class.forName( "net.minecraft.server." + version + "." + "Entity" ) );
			classCache.put( "EntityItem", Class.forName( "net.minecraft.server." + version + "." + "EntityItem" ) );
			classCache.put( "EntityLiving", Class.forName( "net.minecraft.server." + version + "." + "EntityLiving" ) );
			classCache.put( "EntityPlayer", Class.forName( "net.minecraft.server." + version + "." + "EntityPlayer" ) );
			
			classCache.put( "Packet", Class.forName( "net.minecraft.server." + version + "." + "Packet" ) );
			classCache.put( "PacketPlayOutEntityMetadata", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutEntityMetadata" ) );
			classCache.put( "PacketPlayOutEntityDestroy", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutEntityDestroy" ) );
			classCache.put( "PacketPlayOutEntityEquipment", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutEntityEquipment" ) );
			classCache.put( "PacketPlayOutSpawnEntityLiving", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutSpawnEntityLiving" ) );
			classCache.put( "World", Class.forName( "net.minecraft.server." + version + "." + "World" ) );
			
			if ( NBTEditor.getMinecraftVersion().greaterThanOrEqualTo( MinecraftVersion.v1_9 ) ) {
				classCache.put( "EnumItemSlot", Class.forName( "net.minecraft.server." + version + "." + "EnumItemSlot" ) );
				classCache.put( "PacketPlayOutAttachMount", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutMount" ) );
			} else {
				classCache.put( "PacketPlayOutUpdateEntityNBT", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutUpdateEntityNBT" ) );
			}
			
			if ( version.contains( "v1_14" ) || version.contains( "v1_15" ) || version.contains( "v1_16" ) ) {
				classCache.put( "EntityTypes", Class.forName( "net.minecraft.server." + version + "." + "EntityTypes" ) );
			}
			
			if ( NBTEditor.getMinecraftVersion().greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
				classCache.put( "Pair", Class.forName( "com.mojang.datafixers.util.Pair" ) );
			}
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
		}

		methodCache = new HashMap< String, Method >();
		try {
			methodCache.put( "asNMSCopy", getNMSClass( "CraftItemStack" ).getMethod( "asNMSCopy", ItemStack.class ) );
			methodCache.put( "asBukkitCopy", getNMSClass( "CraftItemStack" ).getMethod( "asBukkitCopy", getNMSClass( "ItemStack" ) ) );
			
			methodCache.put( "getWorldHandle", getNMSClass( "CraftWorld" ).getMethod( "getHandle" ) );
			methodCache.put( "getHandle", getNMSClass( "CraftPlayer" ).getMethod( "getHandle" ) );
			methodCache.put( "sendPacket", getNMSClass( "PlayerConnection" ).getMethod( "sendPacket", getNMSClass( "Packet" ) ) );
			
			methodCache.put( "setLocation", getNMSClass( "EntityArmorStand" ).getMethod( "setLocation", double.class, double.class, double.class, float.class, float.class ) );
			methodCache.put( "setSmall", getNMSClass( "EntityArmorStand" ).getMethod( "setSmall", boolean.class ) );
			methodCache.put( "setInvisible", getNMSClass( "EntityArmorStand" ).getMethod( "setInvisible", boolean.class ) );
			methodCache.put( "getId", getNMSClass( "Entity" ).getMethod( "getId" ) );
			methodCache.put( "getBukkitEntity", getNMSClass( "EntityArmorStand" ).getMethod( "getBukkitEntity" ) );
			methodCache.put( "getArmorStandHandle", getNMSClass( "CraftArmorStand" ).getMethod( "getHandle" ) );
			methodCache.put( "getDataWatcher", getNMSClass( "Entity" ).getMethod( "getDataWatcher" ) );
			
			if ( NBTEditor.getMinecraftVersion() == MinecraftVersion.v1_8 ) {
				methodCache.put( "setNoGravity", getNMSClass( "EntityArmorStand" ).getMethod( "setGravity", boolean.class ) );
				methodCache.put( "getEquipment", getNMSClass( "EntityArmorStand" ).getMethod( "getEquipment", int.class ) );
			} else {
				methodCache.put( "setNoGravity", getNMSClass( "EntityArmorStand" ).getMethod( "setNoGravity", boolean.class ) );
				methodCache.put( "getEquipment", getNMSClass( "EntityArmorStand" ).getMethod( "getEquipment", getNMSClass( "EnumItemSlot" ) ) );
				methodCache.put( "valueOf", getNMSClass( "EnumItemSlot" ).getMethod( "valueOf", String.class ) );
				methodCache.put( "setInvulnerable", getNMSClass( "EntityArmorStand" ).getMethod( "setInvulnerable", boolean.class ) );
				methodCache.put( "setMarker", getNMSClass( "EntityArmorStand" ).getMethod( "setMarker", boolean.class ) );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}

		constructorCache = new HashMap< Class< ? >, Constructor< ? > >();
		try {
			constructorCache.put( getNMSClass( "EntityItem" ),  getNMSClass( "EntityItem" ).getConstructor( getNMSClass( "World" ), double.class, double.class, double.class, getNMSClass( "ItemStack" ) ) );
			if ( version.contains( "v1_14" ) || version.contains( "v1_15" ) || version.contains( "v1_16" ) ) {
				constructorCache.put( getNMSClass( "EntityArmorStand" ), getNMSClass( "EntityArmorStand" ).getConstructor( getNMSClass( "EntityTypes" ), getNMSClass( "World" ) ) );
			} else {
				constructorCache.put( getNMSClass( "EntityArmorStand" ), getNMSClass( "EntityArmorStand" ).getConstructor( getNMSClass( "World" ) ) );
			}
			
			constructorCache.put( getNMSClass( "PacketPlayOutEntityMetadata" ),  getNMSClass( "PacketPlayOutEntityMetadata" ).getConstructor( int.class, getNMSClass( "DataWatcher" ), boolean.class ) );
			constructorCache.put( getNMSClass( "PacketPlayOutSpawnEntity" ),  getNMSClass( "PacketPlayOutSpawnEntity" ).getConstructor( getNMSClass( "Entity" ), int.class ) );
			constructorCache.put( getNMSClass( "PacketPlayOutSpawnEntityLiving" ),  getNMSClass( "PacketPlayOutSpawnEntityLiving" ).getConstructor( getNMSClass( "EntityLiving" ) ) );
			if ( NBTEditor.getMinecraftVersion() == MinecraftVersion.v1_8 ) {
				constructorCache.put( getNMSClass( "PacketPlayOutEntityEquipment" ), getNMSClass( "PacketPlayOutEntityEquipment" ).getConstructor( int.class, int.class, getNMSClass( "ItemStack" ) ) );
				constructorCache.put( getNMSClass( "PacketPlayOutAttachEntity" ), getNMSClass( "PacketPlayOutAttachEntity" ).getConstructor( int.class, getNMSClass( "Entity" ), getNMSClass( "Entity" ) ) );
			} else {
				if ( NBTEditor.getMinecraftVersion().greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
					constructorCache.put( getNMSClass( "PacketPlayOutEntityEquipment" ), getNMSClass( "PacketPlayOutEntityEquipment" ).getConstructor( int.class, List.class ) );
				} else {
					constructorCache.put( getNMSClass( "PacketPlayOutEntityEquipment" ), getNMSClass( "PacketPlayOutEntityEquipment" ).getConstructor( int.class, getNMSClass( "EnumItemSlot" ), getNMSClass( "ItemStack" ) ) );
				}
				constructorCache.put( getNMSClass( "PacketPlayOutMount" ), getNMSClass( "PacketPlayOutMount" ).getConstructor( getNMSClass( "Entity" ) ) );
			}
			constructorCache.put( getNMSClass( "PacketPlayOutEntityDestroy" ), getNMSClass( "PacketPlayOutEntityDestroy" ).getConstructor( int[].class ) );
			
			connection = getNMSClass( "EntityPlayer" ).getField( "playerConnection" );
            
            if ( NBTEditor.getMinecraftVersion().greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
            	entityTypeArmorStand = getNMSClass( "EntityTypes" ).getField( "ARMOR_STAND" ).get( null );
            }
            
			if ( NBTEditor.getMinecraftVersion().greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
				constructorCache.put( getNMSClass( "Pair" ), getNMSClass( "Pair" ).getConstructor( Object.class, Object.class ) );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static Method getMethod( String name ) {
		return methodCache.containsKey( name ) ? methodCache.get( name ) : null;
	}

	public static Constructor< ? > getConstructor( Class< ? > clazz ) {
		return constructorCache.containsKey( clazz ) ? constructorCache.get( clazz ) : null;
	}

	public static Class<?> getNMSClass(String name) {
		if ( classCache.containsKey( name ) ) {
			return classCache.get( name );
		}

		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Field getField() {
		return connection;
	}
	
	public static Object constructArmorStand( Object worldServer ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ( version.contains( "v1_14" ) || version.contains( "v1_15" ) || version.contains( "v1_16" ) ) {
			return getConstructor( ReflectionUtil.getNMSClass( "EntityArmorStand" ) ).newInstance( entityTypeArmorStand, worldServer );
		} else {
			return getConstructor( ReflectionUtil.getNMSClass( "EntityArmorStand" ) ).newInstance( worldServer );
		}
	}

	public static Object spawnItem( Object worldServer, Location location, ItemStack item ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object nmsItem = getMethod( "asNMSCopy" ).invoke( null, item );
		return getConstructor( getNMSClass( "EntityItem" ) ).newInstance( worldServer, location.getX(), location.getY(), location.getZ(), nmsItem );
	}
	
	public static Object getMountPacket( Object horse, Object passenger ) {
		try {
			if ( NBTEditor.getMinecraftVersion() == MinecraftVersion.v1_8 ) {
				return getConstructor( getNMSClass( "PacketPlayOutAttachEntity" ) ).newInstance( 0, passenger, horse );
			} else {
				return getConstructor( getNMSClass( "PacketPlayOutMount" ) ).newInstance( horse );
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static CraftInventoryManager getManager() {
		Object instance = null;
		try {
			instance = containerManager.newInstance();
		} catch ( Exception exception ) {
			Bukkit.getPluginManager().disablePlugin( PublicCrafters.getInstance() );
		}
		
		return ( CraftInventoryManager ) instance; 
	}
	
	public static String getVersion() {
		return version;
	}
}
