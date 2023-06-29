package xyz.peptobepismol.directory;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import xyz.peptobepismol.CrosshairAnimations;

import java.io.File;
import java.io.Serializable;

public class Configuration implements Serializable {

    public static net.minecraftforge.common.config.Configuration forgeConfig;

    public static String crosshairType;

    public Configuration(FMLPreInitializationEvent preInitEvent) {
        try {
            forgeConfig = new net.minecraftforge.common.config.Configuration(
                    new File(preInitEvent.getModConfigurationDirectory().getPath(), CrosshairAnimations.MOD_ID + ".cfg"));
        } catch (Exception ex) {
            CrosshairAnimations.modLogger.log(Level.FATAL, "Failed to parse/generate mod .cfg config file");
        }
    }

    //todo finish this method, which determines the type of crosshair animation to use
    public net.minecraftforge.common.config.Configuration initialize() {
        String category;

        category = "CrosshairType";
        forgeConfig.addCustomCategoryComment(category, "Selected Crosshair Type");
        // load all types found here

        // [] save data & return
        forgeConfig.save();
        return forgeConfig;
    }
}
