package com.shortcircuit.pictureperfect;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
@SuppressWarnings("deprecation")
public class MapListener implements Listener{
    private Plugin plugin;
    public MapListener(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMapInitialize(final PlayerItemHeldEvent event){
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if(item != null){
            plugin.reloadConfig();
            if(item.getType().equals(Material.MAP) && plugin.getConfig().contains("ImageMaps." + item.getDurability())){
                MapView map = Bukkit.getMap(item.getDurability());
                // If the map does not have the appropriate renderer, mark it as dirty
                for(MapRenderer render : map.getRenderers()){
                    if(render instanceof ImageMapRenderer){
                        return;
                    }
                }
                // Mark the map as dirty
                plugin.getConfig().set("ImageMaps." + item.getDurability() + ".Dirty", true);
                plugin.saveConfig();
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinGame(final PlayerJoinEvent event){
        Set<String> maps = plugin.getConfig().getConfigurationSection("ImageMaps").getKeys(false);
        if(maps != null){
            for(String key : maps){
                MapView map = Bukkit.getMap(Short.parseShort(key));
                // If the map does not have the appropriate renderer, mark it as dirty
                for(MapRenderer render : map.getRenderers()){
                    if(render instanceof ImageMapRenderer){
                        return;
                    }
                }
                // Mark the map as dirty
                plugin.getConfig().set("ImageMaps." + key + ".Dirty", true);
                plugin.saveConfig();
            }
        }
    }
}
