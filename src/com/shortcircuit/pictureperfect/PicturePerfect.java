package com.shortcircuit.pictureperfect;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
@SuppressWarnings("deprecation")
public class PicturePerfect extends JavaPlugin{
    public File file;
    public FileConfiguration image_file;
    public void onEnable(){
        file = new File(this.getDataFolder() + "/Images.yml");
        boolean exists = false;
        try{
            exists = file.createNewFile();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        image_file = YamlConfiguration.loadConfiguration(file);
        if(exists){
            image_file.set("ImageMaps", new Object[]{});
            try{
                image_file.save(file);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        saveDefaultConfig();
        // Start the cleanup task
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RenderCleanup(this), 100L, 100L);
        // Register the map listener
        getServer().getPluginManager().registerEvents(new MapListener(this), this);
        // Force-render each map
        try{
            Set<String> maps = image_file.getConfigurationSection("ImageMaps").getKeys(false);
            if(maps != null){
                for(String key : maps){
                    MapView map = Bukkit.getMap(Short.parseShort(key));
                    if(map == null){
                        // Create maps for any outstanding image maps
                        map = Bukkit.createMap(Bukkit.getWorlds().get(0));
                    }
                    // Only mark the map as dirty if it doesn't already have a renderer on it
                    boolean dirty = true;
                    for(MapRenderer render : map.getRenderers()){
                        if(render instanceof ImageMapRenderer){
                            dirty = false;;
                        }
                    }
                    image_file.set("ImageMaps." + key + ".Dirty", dirty);
                    try{
                        image_file.save(file);
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
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(commandLabel.equalsIgnoreCase("image")){
            if(args.length < 1){
                sender.sendMessage(ChatColor.RED + "Too few arguments");
                return true;
            }
            if(args[0].equalsIgnoreCase("remove")){
                if(sender instanceof Player){
                    Player player = (Player)sender;
                    if(player.hasPermission("PicturePerfect.Remove")){
                        if(player.getItemInHand().getType().equals(Material.MAP)){
                            // Mark an image map for removal
                            image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Image", null);
                            // Mark the map as dirty
                            image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Dirty", true);
                            try{
                                image_file.save(file);
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        player.sendMessage(ChatColor.RED + "Insufficient permissions");
                    }
                }
            }
            else if(args[0].equalsIgnoreCase("list")){
                if(sender.hasPermission("PicturePerfect.List")){
                    File directory = new File(getDataFolder() + "/Images");
                    sender.sendMessage(ChatColor.GREEN + "[PicturePerfect] Available images:");
                    File[] files = directory.listFiles();
                    for(int i = 0; i < files.length; i++){
                        if(files[i].isDirectory()){
                            File[] nested = files[i].listFiles();
                            files = (File[])ArrayUtils.removeElement(files, files[i]);
                            files = (File[])ArrayUtils.addAll(files, nested);
                        }
                        sender.sendMessage(ChatColor.GREEN + "[" + (i + 1) + "] " + ChatColor.LIGHT_PURPLE + files[i].getPath().replace("plugins\\ImageMap\\Images\\", ""));
                    }
                }
                else{
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions");
                }
            }
            else if(args[0].equalsIgnoreCase("clean")){
                if(sender.hasPermission("PicturePerfect.Clean")){
                    clean();
                }
                else{
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions");
                }
            }
            else{
                if(sender instanceof Player){
                    Player player = (Player)sender;
                    if(player.hasPermission("PicturePerfect.Create")){
                        if(player.getItemInHand().getType().equals(Material.MAP)){
                            // If the 
                            try{
                                URLConnection url = new URL(args[0]).openConnection();
                                url.setConnectTimeout(500);
                                if(player.hasPermission("PicturePerfect.Create.URL")){
                                    // Set the URL to download the image
                                    image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.URL", args[0]);
                                    image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.File", "DownloadQueued");
                                }
                                else{
                                    player.sendMessage(ChatColor.RED + "You may not use an online image");
                                    return true;
                                }
                            }
                            catch(IOException e){
                                image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.URL", null);
                                image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.File", "plugins/ImageMap/Images/" + args[0]);
                            }
                            // Mark the map as dirty
                            image_file.set("ImageMaps." + player.getItemInHand().getDurability() + ".Dirty", true);
                            try{
                                image_file.save(file);
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        player.sendMessage(ChatColor.RED + "Insufficient permissions");
                    }
                }
            }
            return true;
        }
        return false;
    }
    // Delete any image files not linked to a map
    public void clean(){
        Bukkit.getLogger().info("[PicturePerfect] Initiating image cleanup");
        image_file = YamlConfiguration.loadConfiguration(file);
        int count = 0;
        Set<String> maps = new HashSet<String>();
        try{
            maps = image_file.getConfigurationSection("ImageMaps").getKeys(false);
        }
        catch(NullPointerException e){
            
        }
        if(maps != null){
            // Get all the image files in the Images folder
            File directory = new File(getDataFolder() + "/Images");
            File[] files = directory.listFiles();
            for(int i = 0; i < files.length; i++){
                boolean toDelete = true;
                File file = files[i];
                if(file.isDirectory()){
                    files = (File[])ArrayUtils.addAll(files, file.listFiles());
                    toDelete = false;
                }
                for(String key : maps){
                    // If the image is linked to a map, don't delete it
                    if(image_file.getString("ImageMaps." + key + ".Image.File").replace("\\", "/").equalsIgnoreCase((file + "").replace("\\", "/"))){
                        toDelete = false;
                        break;
                    }
                }
                // Delete the file
                if(toDelete){
                    Bukkit.getLogger().info("[PicturePerfect] Deleted " + (file + "").replace("\\", "/").replace("plugins/PicturePerfect/Images/", ""));
                    file.delete();
                    count++;
                }
            }
        }
        Bukkit.getLogger().info("[PicturePerfect] Image cleanup complete (" + count + ") images deleted");
    }
}
