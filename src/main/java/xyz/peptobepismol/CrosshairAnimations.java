package xyz.peptobepismol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.peptobepismol.command.CommandToggleCrosshair;

/* This is where the magic happens... */
@Mod(
        modid = CrosshairAnimations.MOD_ID,
        name = CrosshairAnimations.MOD_NAME,
        version = CrosshairAnimations.VERSION,
        clientSideOnly = true
)

/**
 * CrosshairAnimations mod, for those who enjoy stylish animated crosshairs!
 * @author Spyrxo
 */
public class CrosshairAnimations {

    /** Mod dependent signing information **/
    public static final String MOD_ID = "crosshairanimations";
    public static final String MOD_NAME = "CrosshairAnimations";
    public static final String VERSION = "1.0";

    /** Environment logger (client-console) **/
    public static final Logger modLogger = LogManager.getLogger(MOD_ID);

    /** Custom bow animation directory **/
    public static final String ANIMATION_DATA_DIR = "/ch-anims";

    /** Is the mod allowed to render the crosshair? (toggable using command) **/
    public static boolean isAllowedToRenderCrosshair = true;

    //public static net.minecraftforge.common.config.Configuration config;

    /** Mod instance created by forge **/
    @Mod.Instance(MOD_ID)
    public static CrosshairAnimations MOD_INSTANCE;


    /** {@link CrosshairAnimations} Mod Constructor. **/
    public CrosshairAnimations() {
        MOD_INSTANCE = this;

        // todo finish anims folder and logic around loading custom types
        //if (!AnimationResourceHandler.initialize()) {
        //    modLogger.log(Level.FATAL, "Error initializing resource handler. Please restart game");
        //}
    }

    /** This is the first initialization event called by the mod. Any registry events
     * will not run before this one, it is the first process that is called by forge. **/
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        //Configuration crosshairConfig = new Configuration(e);
        //config = crosshairConfig.initialize();
    }

    /** Initialization event called after the {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent}
     * has successfully run. The registry events in this method run after the aforementioned event has passed. **/
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {

        // register our events handle class - listening to stuff related to bows
        MinecraftForge.EVENT_BUS.register(new PlayerEventsHandler());

        // register our commands - allow the player to control aspects of the mod in-game
        ClientCommandHandler.instance.registerCommand(new CommandToggleCrosshair());
    }

    /** Event called after the {@link net.minecraftforge.fml.common.event.FMLPostInitializationEvent}
     * has successfully run. The events in this method run always after the entire mod has been loaded. **/
    @Mod.EventHandler
    public void initComplete(FMLLoadCompleteEvent e) {

        // log to console - successfully loaded the mod
        modLogger.log(Level.INFO, "Successfully loaded " + MOD_NAME + " mod  [v" + VERSION + "]");
    }

    /** Sets whether to allow the custom crosshair animation to be rendered.
     * (by default, when the mod is launched, this boolean function is true) **/
    public static void setIsAllowedToRenderCrosshair(boolean toggle) {
        isAllowedToRenderCrosshair = toggle;
    }

    /** @return {@link EntityPlayerSP} instance (client-side)
     * (the user who is enjoying the mod!) **/
    public static EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    /** @return Resource Location for {@link CrosshairAnimations}.
     * @param name = Resource path (internal plugin directory) **/
    public static ResourceLocation getResource(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
