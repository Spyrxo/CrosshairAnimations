package xyz.peptobepismol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

/*
 *
 * - when mod loads up:
 *      > check for this directory and other sub-directories
 *      > if doesn't exist, then manually add them and use packaged defaults
 *      > next, determine current cross-hair type via .config file (or default)
 *      > load the selected cross-hair images into renderer
 *
 * - commands:
 *      > /refreshcrosshair - refreshes the loaded cross-hair
 *      > /setcrosshairtype - sets the cross-hair type (with tab-autocomplete which can index through all sub-package folder names) (also auto-refreshes)
 * */

/**
 * {@link PlayerEventsHandler} class which is generated
 * when the {@link CrosshairAnimations} mod is loaded on init.
 *
 * This class handles all player related logic in regard to checking
 * if the player is holding/using a bow, rendering the bow and the crosshair,
 * removing the original crosshair and replacing it with our custom image render.
 */
public class PlayerEventsHandler {

    /**
     * Is the player currently holding a bow in his hand?
     * (used to make sure render event method logic is only fired once)
     */
    private boolean isNowHoldingBow = false;

    /**
     * Is the player currently charging back their bow?
     * (used to keep track of if a player is still charging their bow)
     */
    private boolean isChargingBow = false;

    /**
     * Incrementing charging ticker
     * (this value is ++ every render tick and handles the "timing" of animation frames)
     */
    private int chargingTicker = 0;

    /**
     * Bow state decrementing counter
     * (this value is -- every x chargingTicks and handles setting the next stage of the animation frame)
     */
    private int bowState = 0;

    /**
     * {@link ArrowNockEvent} called whenever a {@link EntityPlayer} begins
     * charging their bow with a valid arrow in their inventory (or in creative).
     *
     * This handler resets the associated class functions to their correct states.
     * We do this because the crosshair animation needs to "restart" its logic.
     * **/
    @SubscribeEvent
    public void onPlayerNockArrowInBowHandle(ArrowNockEvent e) {
        isChargingBow = true; // player begun charging an arrow
        chargingTicker = 0; // reset the ticker
        bowState = 12; // highest amount of texture states
    }

    /**
     * {@link ArrowLooseEvent} called whenever a {@link EntityPlayer} releases
     * their bow.
     *
     * This handler resets the bow charging state functions, because the game
     * now assumes they are back to state0 of the bow firing process.
     * **/
    @SubscribeEvent
    public void onPlayerShootArrowResetHandle(ArrowLooseEvent e) {
        resetBowCharging();
    }

    /**
     * {@link RenderHandEvent} called whenever the Minecraft client attempts
     * to render the players hand on the screen.
     *
     * This handler checks to make sure if they are holding a bow, the relevant
     * functions are correctly set to allow the crosshair to correctly render.
     * Every time the render event is called, the game is also attempting to render
     * the hand animation ~(1 tick), so the bow charging ticker will increment if
     * the game knows the player is charging up their bow. This controls which
     * crosshair animation frame will be displayed for the player when rendering GUI.
     * **/
    @SubscribeEvent
    public void onRenderPlayersHandCheckForBow(RenderHandEvent e) {
        EntityPlayer player = CrosshairAnimations.getClientPlayer();
        if (player == null) return;

        // check the players current hand item
        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem == null || heldItem.getItem() != Items.bow) {

            // cheeky little check to make sure we are resetting the boolean
            // (because if they were previously holding a bow and swapped off, we need to track that)
            if (isNowHoldingBow) {
                isNowHoldingBow = false;
            }

            // also make sure we are resetting the "isCharging" because
            // obviously at this stage, if they swapped off the bow they aren't charging
            resetBowCharging();
            return;
        }

        // player is holding a bow in their hand
        if (heldItem.getItem() == Items.bow) {

            // make sure they aren't previously holding a bow
            // (we do this to make sure the logic for rendering & checking only fires once)
            if (!isNowHoldingBow) {
                isNowHoldingBow = true;
            }

            // player is charging back their bow
            // increment the charging ticker
            if (isChargingBow) {
                chargingTicker++;

                // every x divisible ticks, we lower the bow state
                // (this updates the animation frame to render on the screen in the draw function)
                if (chargingTicker % 9 == 0) { // roughly matches bow charging animation speed o_O
                    bowState--;
                    if (bowState <= 0) bowState = 0;
                }
            }
        }
    }

    /**
     * {@link RenderGameOverlayEvent} called whenever the Minecraft client attempts
     * to render the entire on-screen GUI to the player. (health, hotbar, boss-bar, etc)
     *
     * This handler checks to make sure the player is holding a bow and is charging
     * up the bow, and if they are, cancel rendering the original minecraft crosshair (+)
     * and instead we call our own custom crosshair rendering method to put our
     * crosshair that is loaded, onto the players screen with the correct animation frame.
     * **/
    @SubscribeEvent
    public void onRenderPlayersCrosshairCheckForBow(RenderGameOverlayEvent e) {
        EntityPlayer player = CrosshairAnimations.getClientPlayer();
        if (player == null) return;

        // check the players current hand item
        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem == null) return;

        // player is holding a bow and the game is attempting to render a crosshair
        if (heldItem.getItem() == Items.bow && isNowHoldingBow && CrosshairAnimations.isAllowedToRenderCrosshair
                && e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            e.setCanceled(true); // [!] CANCEL THE ORIGINAL CROSSHAIR RENDER

            // now we will draw our own custom animated crosshair in the center of the screen
            try {
                drawCrosshair();
            } catch (Exception ex) {
                CrosshairAnimations.modLogger.log(Level.ERROR, "Error when trying to render crosshair texture");
            }
        }
    }

    // todo figure out a better way to draw this, make it feel "smoother"
    private void drawCrosshair() {

        // begin rendering
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        // load and bind texture
        ResourceLocation defaultState = CrosshairAnimations.getResource("crosshair_state" + bowState + ".png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(defaultState);

        // inverse color
        GlStateManager.tryBlendFuncSeparate(775, 769,
                1, 0);

        // init colorization
        GlStateManager.color(1, 1, 1);

        // draw texture on screen
        GuiIngame playerGUI = Minecraft.getMinecraft().ingameGUI;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        // center the texture to the middle of the screen
        int imgWidth = 256;
        int imgHeight = 256;
        int guiLeft = (scaledResolution.getScaledWidth() - imgWidth) / 2;
        int guiTop = (scaledResolution.getScaledHeight() - imgHeight) / 2;

        // render the image
        playerGUI.drawTexturedModalRect(
                guiLeft, guiTop,
                0, 0,
                imgWidth, imgHeight
        );

        // set color once drawn
        GlStateManager.color(1, 1, 1);

        // finished
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }

    private void resetBowCharging() {
        if (isChargingBow) {
            isChargingBow = false; // no longer pulling back
            chargingTicker = 0; // ticker back to default state
            bowState = 0; // default crosshair render state
        }
    }

}
