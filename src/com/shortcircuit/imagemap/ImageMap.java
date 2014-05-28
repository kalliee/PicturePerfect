package com.shortcircuit.imagemap;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
@SuppressWarnings("deprecation")
public class ImageMap extends JavaPlugin{
    public void onEnable(){
        saveDefaultConfig();
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RenderCleanup(this), 100L, 100L);
        getServer().getPluginManager().registerEvents(new MapListener(this), this);
        Set<String> maps = getConfig().getConfigurationSection("ImageMaps").getKeys(false);
        if(maps != null){
            for(String key : maps){
                MapView map = Bukkit.getMap(Short.parseShort(key));
                if(map == null){
                    map = Bukkit.createMap(Bukkit.getWorlds().get(0));
                }
                boolean dirty = true;
                for(MapRenderer render : map.getRenderers()){
                    if(render instanceof ImageMapRenderer){
                        dirty = false;;
                    }
                }
                getConfig().set("ImageMaps." + key + ".Dirty", dirty);
                saveConfig();
            }
        }
    }
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(commandLabel.equalsIgnoreCase("image")){
                if(args.length < 1){
                    player.sendMessage(ChatColor.RED + "Too few arguments");
                    return true;
                }
                if(args[0].equalsIgnoreCase("clean")){
                    clean();
                    return true;
                }
                if(args[0].equalsIgnoreCase("remove")){
                    if(player.getItemInHand().getType().equals(Material.MAP)){
                        getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Image", null);
                        getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Dirty", true);
                        saveConfig();
                    }
                    return true;
                }
                if(player.getItemInHand().getType().equals(Material.MAP)){
                    getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.URL", args[0]);
                    getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.File", "plugins/ImageMap/Images/" + args[0]);
                    getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Dirty", true);
                    saveConfig();
                }  
                return true;
            }
        }
        return false;
    }
    public void clean(){
        reloadConfig();
        Set<String> maps = getConfig().getConfigurationSection("ImageMaps").getKeys(false);
        if(maps != null){
            File directory = new File(getDataFolder() + "/Images");
            for(File file : directory.listFiles()){
                boolean toDelete = true;
                for(String key : maps){
                    if(getConfig().getString("ImageMaps." + key + ".Image.File").equalsIgnoreCase((file + "").replace("\\", "/"))){
                        toDelete = false;
                        break;
                    }
                }
                if(toDelete){
                    file.delete();
                }
            }
        }
    }
}
