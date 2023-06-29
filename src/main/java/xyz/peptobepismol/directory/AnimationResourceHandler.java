package xyz.peptobepismol.directory;

import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import xyz.peptobepismol.CrosshairAnimations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link AnimationResourceHandler} class which is generated
 * when the {@link CrosshairAnimations} mod is loaded on init.
 *
 * This class attempts to initialize and check all related
 * directories within the mods .minecraft folder to ensure
 * all custom animation content can be loaded and used.
 */
public class AnimationResourceHandler {

    /** Animation directory (folder within .minecraft) **/
    private static File animationDirectory;

    /** Animation states 0-12 (state0 = default) **/
    private static final int MAX_ANIMATION_FRAMES = 13;

    private static HashMap<String, ArrayList<File>> customAnimationData = new HashMap<>();

    /*
     * > mod instance load:
     * - check the custom animation directory
     * - get all folders and add their names to a list
     *
     * > mod pre init:
     * - generate and check config
     * - if DEFAULT, then use internal textures, else validate TYPE
     *      * type validation checks to make sure there are the correct amount of textures present
     * - add all frames to a Hashmap<String[customanimationame], Arraylist<File>[animationList] (should be 13)
     *
     * > event stuff:
     * - when determining resource location, either use default resource OR custom texture
     * - handle rendering the same way (making sure the index's are all correctly aligned)
     */

    //todo do the above and finish the custom crosshairs
    public static synchronized boolean initialize() {
        try {

            // [] lets try to locate the main animation directory within the .minecraft folder
            animationDirectory = FileUtils.getFile(Minecraft.getMinecraft().mcDataDir + CrosshairAnimations.ANIMATION_DATA_DIR);

            // if a directory doesn't exist, we want to create a new one
            if (!animationDirectory.exists()) {
                try {
                    animationDirectory.mkdirs();
                    CrosshairAnimations.modLogger.log(Level.INFO, "Successfully generated new animation directory");
                } catch (Exception ex) {
                    CrosshairAnimations.modLogger.log(Level.FATAL, "Failed to generate essential " + CrosshairAnimations.ANIMATION_DATA_DIR + " directory. Please restart game");
                    return false;
                }
            }

            // now check for any custom crosshair types the player has added
            try {
                File[] directories = animationDirectory.listFiles(File::isDirectory);

                // check the animations directory folder
                // if we have valid folders, we want to check them for .pngs
                if (directories != null) {
                    for (File dir : directories) {
                        CrosshairAnimations.modLogger.log(Level.INFO, "Found custom animation directory: " + dir.getName());

                        // loop through and check all files within the folder and make sure
                        // they are .png files and also have the correct "state[numb0-12].png" ending name
                        try {
                            File[] checkFiles = dir.listFiles((curDir, name) ->
                                    name.endsWith(".png") // end of the file must be a .png
                                            && containsSpecificKeywords(name.substring(0, name.length() - 4)) // make sure to remove the .png when checking for ending keyword
                            );

                            // if we cannot find files, or we don't have enough files in the directory
                            // then we can just continue in the loop and move onto the next folder
                            if (checkFiles == null) {
                                CrosshairAnimations.modLogger.log(Level.ERROR, "Failed checking files for " + dir.getName() + " because folder is empty");
                                continue;
                            }

                            if (checkFiles.length < MAX_ANIMATION_FRAMES) {
                                CrosshairAnimations.modLogger.log(Level.ERROR, "Failed checking files for " + dir.getName() + " because there are less than the required amount of image files (needs to be 12)");
                                continue;
                            }

                            // [] at this point, we can generate if we need too, valid data for the custom animation frames
                            // [key = animationName] [value = list of animation files]
                            String dirKeyName = dir.getName().toLowerCase();
                            customAnimationData.putIfAbsent(dirKeyName, new ArrayList<>());

                            for (File imgFile : checkFiles) {
                                CrosshairAnimations.modLogger.log(Level.INFO, "Found png file: " + imgFile.getName());

                                customAnimationData.get(dirKeyName).add(imgFile);
                            }

                            /*try {
                                Collections.sort(customAnimationData.get(dirKeyName), (t1, t2) -> {

                                    // first, determine the animation frame value
                                    // (this can be done as a constant, because we make sure our files always end in "x.png" [where x is a number from 0-13])
                                    Integer t1AnimationFrame = Integer.parseInt(String.valueOf(t1.getName().charAt(t1.getName().length() - 5)));
                                    Integer t2AnimationFrame = Integer.parseInt(String.valueOf(t2.getName().charAt(t2.getName().length() - 5)));

                                    CrosshairAnimations.modLogger.log(Level.INFO, "t1animationframe: " + t1AnimationFrame);
                                    CrosshairAnimations.modLogger.log(Level.INFO, "t2animationframe: " + t2AnimationFrame);

                                    // second, compare between the 2 values and see which one is higher, then order them
                                    return t1AnimationFrame.compareTo(t2AnimationFrame);
                                });
                            } catch (Exception ex) {
                                CrosshairAnimations.modLogger.log(Level.WARN, "Failed to sort the animation list");
                                return false;
                            }*/

                            CrosshairAnimations.modLogger.log(Level.INFO, "sorted list: " + customAnimationData.get(dirKeyName));

                        } catch (Exception ex) {
                            CrosshairAnimations.modLogger.log(Level.ERROR, "Directory (" + dir.getName() + ") contains invalid " +
                                    "or not enough correct .png files to render a crosshair with");
                            return false;
                        }
                    }
                }

            } catch (Exception ex) {
                CrosshairAnimations.modLogger.log(Level.ERROR, "Failed to check animation directory (" + CrosshairAnimations.ANIMATION_DATA_DIR + "). " +
                        "Maybe the folder is missing or has been renamed? Try restarting game");
                return false;
            }

            // the entire processes epically failed!
        } catch (Exception ex) {
            CrosshairAnimations.modLogger.log(Level.ERROR, "Could not find or generate " + CrosshairAnimations.ANIMATION_DATA_DIR + " directory. Please restart game");
            return false;
        }

        // [] process was successful
        return true;
    }

    private static boolean containsSpecificKeywords(String mainString) {
        Matcher matcher = Pattern.compile("[-+]?\\d+").matcher(mainString);
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group());
            if (number >= 0 && number <= MAX_ANIMATION_FRAMES) return true;
        }
        return false;
    }
}
