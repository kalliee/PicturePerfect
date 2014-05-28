package com.shortcircuit.imagemap;

import java.util.Set;

import org.bukkit.Bukkit;
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
        Player player = (Player)sender;
        if(commandLabel.equalsIgnoreCase("image")){
            reloadConfig();
            if(player.getItemInHand().getType().equals(Material.MAP)){
                /*
                MapView map = Bukkit.getMap(player.getItemInHand().getDurability());
                for(MapRenderer render : map.getRenderers()){
                    map.removeRenderer(render);
                }
                try{
                    BufferedImage image = ImageIO.read(new URL(args[0]));
                    ImageMapRenderer derp = new ImageMapRenderer(image);
                    map.addRenderer(derp);
                    player.sendMap(map);
                    derp.setRendered(true);
                 */
                getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.URL", args[0]);
                getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Image.File", "null");
                getConfig().set("ImageMaps." + player.getItemInHand().getDurability() + ".Dirty", true);
                saveConfig();
                /*
                    player.sendMessage(ChatColor.GREEN + "Image rendered!");
                }
                catch(MalformedURLException e){
                    player.sendMessage(ChatColor.RED + "Not a valid URL");
                }
                catch(UnknownHostException e){
                    player.sendMessage(ChatColor.RED + "Image not found");
                }
                catch(IOException e){
                    player.sendMessage(ChatColor.RED + "Something went wrong while retrieving the image");
                }
                 */
            }            
            return true;
        }
        return false;
    }
}
