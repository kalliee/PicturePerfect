package com.shortcircuit.pictureperfect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.bukkit.map.MapRenderer;
import org.bukkit.plugin.Plugin;

public class RenderWriterReader {
    private Plugin plugin;
    public RenderWriterReader(Plugin plugin){
        this.plugin = plugin;
    }
    public void saveRenderer(short map_id, MapRenderer map_renderer){
        try{
            File file = new File(plugin.getDataFolder() + "/Renderers/" + map_id);
            file.createNewFile();
            file.mkdirs();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(map_renderer);
            out.flush();
            out.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public void saveRenderer(String map_id, MapRenderer map_renderer){
        try{
            File file = new File(plugin.getDataFolder() + "/Renderers/" + map_id);
            file.createNewFile();
            file.mkdirs();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(map_renderer);
            out.flush();
            out.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public MapRenderer loadRenderer(short map_id){
        try{
            File file = new File(plugin.getDataFolder() + "/Renderers/" + map_id);
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            MapRenderer map_renderer = (MapRenderer)in.readObject();
            in.close();
            return map_renderer;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public MapRenderer loadRenderer(String map_id){
        try{
            File file = new File(plugin.getDataFolder() + "/Renderers/" + map_id);
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            MapRenderer map_renderer = (MapRenderer)in.readObject();
            in.close();
            return map_renderer;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
