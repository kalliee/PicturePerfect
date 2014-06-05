package com.shortcircuit.pictureperfect;

import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
@SuppressWarnings("deprecation")
public class MapListener implements Listener{
    private PicturePerfect plugin;
    public MapListener(PicturePerfect plugin){
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMapInitialize(final PlayerItemHeldEvent event){
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if(item != null){
            try{
                plugin.image_file = YamlConfiguration.loadConfiguration(plugin.file);
                if(item.getType().equals(Material.MAP) && plugin.image_file.contains("ImageMaps." + item.getDurability())){
                    MapView map = Bukkit.getMap(item.getDurability());
                    // If the map does not have the appropriate renderer, mark it as dirty
                    for(MapRenderer render : map.getRenderers()){
                        if(render instanceof ImageMapRenderer){
                            return;
                        }
                    }
                    // Mark the map as dirty
                    plugin.image_file.set("ImageMaps." + item.getDurability() + ".Dirty", true);
                    try{
                        plugin.image_file.save(plugin.file);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            catch(NullPointerException e){

            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinGame(final PlayerJoinEvent event){
        plugin.image_file = YamlConfiguration.loadConfiguration(plugin.file);
        try{
            Set<String> maps = plugin.image_file.getConfigurationSection("ImageMaps").getKeys(false);
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
                    plugin.image_file.set("ImageMaps." + key + ".Dirty", true);
                    try{
                        plugin.image_file.save(plugin.file);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(NullPointerException e){

        }
    }
}
