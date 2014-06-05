package com.shortcircuit.pictureperfect;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

public class RenderCleanup extends BukkitRunnable{
    private PicturePerfect plugin;
    private ImageFetcher fetcher;
    public RenderCleanup(PicturePerfect plugin){
        this.plugin = plugin;
        fetcher = new ImageFetcher(plugin);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        boolean found_dirty = false;
        int maps_rendered = 0;
        long start_time = System.currentTimeMillis();
        plugin.image_file = YamlConfiguration.loadConfiguration(plugin.file);
        try{
            Set<String> maps = plugin.image_file.getConfigurationSection("ImageMaps").getKeys(false);
            if(maps != null){
                for(String key : maps){
                    // Only render the map if it is marked as dirty
                    boolean dirty = plugin.image_file.getBoolean("ImageMaps." + key + ".Dirty");
                    if(dirty){
                        found_dirty = true;
                        maps_rendered++;
                        MapView map = Bukkit.getMap(Short.parseShort(key));
                        BufferedImage image = null;
                        // Attempt to get the filename of the linked image. If there is no file, remove the map
                        String file = plugin.image_file.getString("ImageMaps." + key + ".Image.File");
                        if(file == null){
                            for(MapRenderer renderer : map.getRenderers()){
                                if(renderer instanceof ImageMapRenderer){
                                    // Get and add the map's default renderer
                                    ImageMapRenderer imageRenderer = (ImageMapRenderer)renderer;
                                    map.addRenderer(imageRenderer.getDefaultMapRenderer());
                                    map.removeRenderer(renderer);
                                }
                            }
                            plugin.image_file.set("ImageMaps." + key, null);
                            try{
                                plugin.image_file.save(plugin.file);
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                            file = "None";
                            //break;
                        }
                        // If the image cannot be found, attempt to download it
                        if(!fetcher.hasImage(file)){
                            file = fetcher.saveImage(plugin.image_file.getString("ImageMaps." + key + ".Image.URL"));
                            plugin.image_file.set("ImageMaps." + key + ".Image.File", file);
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
                            plugin.image_file.set("ImageMaps." + key, null);
                            try{
                                plugin.image_file.save(plugin.file);
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
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
                            plugin.image_file.set("ImageMaps." + key + ".Dirty", false);
                            try{
                                plugin.image_file.save(plugin.file);
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                        if(plugin.getConfig().getBoolean("ResourceSaver.UseResourceSaver")){
                            if(maps_rendered >= plugin.getConfig().getInt("ResourceSaver.MapsToRender")){
                                long end_time = System.currentTimeMillis();
                                Bukkit.getLogger().info("[PicturePerfect] Rendered " + maps_rendered + " dirty maps (" + (end_time - start_time) + "ms)");
                                return;
                            }
                        }
                    }
                }
            }
            long end_time = System.currentTimeMillis();
            if(found_dirty){
                Bukkit.getLogger().info("[PicturePerfect] Rendered " + maps_rendered + " dirty maps (" + (end_time - start_time) + "ms)");
            }
        }
        catch(NullPointerException e){
            
        }
    }
}
