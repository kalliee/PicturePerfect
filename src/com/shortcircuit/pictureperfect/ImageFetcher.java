package com.shortcircuit.pictureperfect;

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
    /*
     * Download an image
     */
    public String saveImage(String image_url){
        System.setProperty("http.keepAlive", "false");
        try{
            URLConnection imageURL = new URL(image_url).openConnection();
            imageURL.setConnectTimeout(500);
            /*
             * TODO: Limit input stream size
             */
            InputStream stream = imageURL.getInputStream();
            String type = "." + image_url.split("\\.")[image_url.split("\\.").length - 1];
            String image_file = plugin.getDataFolder() + "/Images/" + makeSafe(image_url) + type;
            Files.copy(stream, new File(image_file).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return image_file.replace("\\", "/");
        }
        catch(Exception e){
            //e.printStackTrace();
            return null;
        }
    }
    /*
     * Check if an image file exists
     */
    public boolean hasImage(String image_url){
        File file = new File(image_url);
        return file.exists();
    }
    /*
     * Load an image from a file
     */
    public BufferedImage loadImage(String image_url){
        try{
            return ImageIO.read(new File(image_url));
        }
        catch(Exception e){
            //e.printStackTrace();
            return null;
        }
    }
    /*
     * Remove special characters from a file name
     */
    public String makeSafe(String original){
        return StringUtils.replaceChars(original, "./\\:*<>|?\"", "-");
    }
}
