package com.shortcircuit.imagemap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageMapRenderer extends MapRenderer{
    private BufferedImage image;
    private boolean isRendered = false;
    private MapRenderer default_renderer;
    public ImageMapRenderer(BufferedImage image){
        this.image = image;
    }
    /*
     * Only render the map once, to reduce server load
     */ 

    @Override
    public void render(MapView arg0, MapCanvas arg1, Player arg2) {
        if(!isRendered){
            arg1.drawImage(0, 0, MapPalette.resizeImage(image));
            setRendered(true);
        }
    }
    public void setRendered(boolean isRendered){
        this.isRendered = isRendered;
    }
    public boolean getRendered(){
        return isRendered;
    }
    /*
     * We want to be able to return the map to it's original state once the image is removed
     */
    public MapRenderer getDefaultMapRenderer(){
        return default_renderer;
    }
    public void setDefaultMapRenderer(MapRenderer default_renderer){
        this.default_renderer = default_renderer;
    }
}
