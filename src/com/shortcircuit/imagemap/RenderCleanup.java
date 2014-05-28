package com.shortcircuit.imagemap;

import java.awt.image.BufferedImage;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RenderCleanup extends BukkitRunnable{
    private Plugin plugin;
    private ImageFetcher fetcher;
    public RenderCleanup(Plugin plugin){
        this.plugin = plugin;
        fetcher = new ImageFetcher(plugin);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        plugin.reloadConfig();
        Set<String> maps = plugin.getConfig().getConfigurationSection("ImageMaps").getKeys(false);
        if(maps != null){
            for(String key : maps){
                // Only render the map if it is marked as dirty
                boolean dirty = plugin.getConfig().getBoolean("ImageMaps." + key + ".Dirty");
                if(dirty){
                    MapView map = Bukkit.getMap(Short.parseShort(key));
                    BufferedImage image = null;
                    // Attempt to get the filename of the linked image. If there is no file, remove the map
                    String file = plugin.getConfig().getString("ImageMaps." + key + ".Image.File");
                    if(file == null){
                        plugin.getConfig().set("ImageMaps." + key, null);
                        plugin.saveConfig();
                        break;
                    }
                    // If the image cannot be found, attempt to download it
                    if(!fetcher.hasImage(file)){
                        file = fetcher.saveImage(plugin.getConfig().getString("ImageMaps." + key + ".Image.URL"));
                        plugin.getConfig().set("ImageMaps." + key + ".Image.File", file);
                    }
                    // If the image cannot be download, remove the map
                    if(file == null){
                        plugin.getConfig().set("ImageMaps." + key, null);
                        plugin.saveConfig();
                        break;
                    }
                    // Otherwise, load the image and add the new renderer
                    image = fetcher.loadImage(file);
                    for(MapRenderer render : map.getRenderers()){
                        map.removeRenderer(render);
                    }
                    ImageMapRenderer renderer = new ImageMapRenderer(image);
                    map.addRenderer(renderer);
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMap(map);
                    }
                    // Mark the map as clean
                    plugin.getConfig().set("ImageMaps." + key + ".Dirty", false);
                    plugin.saveConfig();
                }
            }
        }
    }
}
