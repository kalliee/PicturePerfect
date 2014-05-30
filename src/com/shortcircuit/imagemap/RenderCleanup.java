package com.shortcircuit.imagemap;

import java.awt.image.BufferedImage;
import java.util.Set;

import org.bukkit.Bukkit;
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
        boolean found_dirty = false;
        int maps_rendered = 0;
        long start_time = System.currentTimeMillis();
        plugin.reloadConfig();
        Set<String> maps = plugin.getConfig().getConfigurationSection("ImageMaps").getKeys(false);
        if(maps != null){
            for(String key : maps){
                // Only render the map if it is marked as dirty
                boolean dirty = plugin.getConfig().getBoolean("ImageMaps." + key + ".Dirty");
                if(dirty){
                    found_dirty = true;
                    maps_rendered++;
                    MapView map = Bukkit.getMap(Short.parseShort(key));
                    BufferedImage image = null;
                    // Attempt to get the filename of the linked image. If there is no file, remove the map
                    String file = plugin.getConfig().getString("ImageMaps." + key + ".Image.File");
                    if(file == null){
                        for(MapRenderer renderer : map.getRenderers()){
                            if(renderer instanceof ImageMapRenderer){
                                // Get and add the map's default renderer
                                ImageMapRenderer imageRenderer = (ImageMapRenderer)renderer;
                                map.addRenderer(imageRenderer.getDefaultMapRenderer());
                                map.removeRenderer(renderer);
                            }
                        }
                        plugin.getConfig().set("ImageMaps." + key, null);
                        plugin.saveConfig();
                        file = "None";
                        //break;
                    }
                    // If the image cannot be found, attempt to download it
                    if(!fetcher.hasImage(file)){
                        file = fetcher.saveImage(plugin.getConfig().getString("ImageMaps." + key + ".Image.URL"));
                        plugin.getConfig().set("ImageMaps." + key + ".Image.File", file);
                    }
                    // If the image cannot be download, remove the map
                    if(file == null){
                        for(MapRenderer renderer : map.getRenderers()){
                            if(renderer instanceof ImageMapRenderer){
                                // Get and add the map's default renderer
                                ImageMapRenderer imageRenderer = (ImageMapRenderer)renderer;
                                map.addRenderer(imageRenderer.getDefaultMapRenderer());
                                map.removeRenderer(renderer);
                            }
                        }
                        plugin.getConfig().set("ImageMaps." + key, null);
                        plugin.saveConfig();
                        //break;
                    }
                    // Otherwise, load the image and add the new renderer
                    else{
                        image = fetcher.loadImage(file);
                        ImageMapRenderer renderer = new ImageMapRenderer(image);
                        for(MapRenderer render : map.getRenderers()){
                            map.removeRenderer(render);
                            // Set the new renderer's default map renderer
                            renderer.setDefaultMapRenderer(render);
                        }
                        map.addRenderer(renderer);
                        // Mark the map as clean
                        plugin.getConfig().set("ImageMaps." + key + ".Dirty", false);
                        plugin.saveConfig();
                    }
                }
            }
        }
        long end_time = System.currentTimeMillis();
        if(found_dirty){
            Bukkit.getLogger().info("[ImageMap] Rendered " + maps_rendered + " dirty maps (" + (end_time - start_time) + "ms)");
        }
    }
}
