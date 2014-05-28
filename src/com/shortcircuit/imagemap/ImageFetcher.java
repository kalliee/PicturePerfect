package com.shortcircuit.imagemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;

public class ImageFetcher {
    private Plugin plugin;
    public ImageFetcher(Plugin plugin){
        this.plugin = plugin;
    }
    public BufferedImage fetchImage(String image_url){
        System.setProperty("http.keepAlive", "false");
        try{
            return ImageIO.read(new URL(image_url));
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String saveImage(String image_url){
        System.setProperty("http.keepAlive", "false");
        try{
            URLConnection imageURL = new URL(image_url).openConnection();
            /*
             * TODO: Limit input stream size
             */
            InputStream stream = imageURL.getInputStream();
            String image_file = plugin.getDataFolder() + "/" + makeSafe(image_url);
            Files.copy(stream, new File(image_file).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return image_file;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public boolean hasImage(String image_url){
        //String type = image_url.split(".")[1];
        //String image_file = plugin.getDataFolder() + "/" + StringUtils.replaceChars(image_url, "./\\:*<>|?\"", "-") + type;
        File file = new File(image_url);
        return file.exists();
    }
    public BufferedImage loadImage(String image_url){
        try{
            //String type = image_url.split(".")[1];
            //String image_file = plugin.getDataFolder() + "/" + StringUtils.replaceChars(image_url, "./\\:*<>|?\"", "-") + type;
            return ImageIO.read(new File(image_url));
        }
        catch(Exception e){
            return null;
        }
    }
    public String makeSafe(String original){
        return StringUtils.replaceChars(original, "./\\:*<>|?\"", "-");
    }
}
