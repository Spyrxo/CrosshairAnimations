package xyz.peptobepismol.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import xyz.peptobepismol.CrosshairAnimations;

import java.util.ArrayList;
import java.util.List;

public class CommandToggleCrosshair extends CommandBase {

    @Override
    public String getCommandName() {
        return "togglecrosshair";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "togglecrosshair";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("tcrosshair");
        aliases.add("togglech");
        aliases.add("tch");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        CrosshairAnimations.setIsAllowedToRenderCrosshair(!CrosshairAnimations.isAllowedToRenderCrosshair);
        String chatMessage = CrosshairAnimations.isAllowedToRenderCrosshair ?
                EnumChatFormatting.GREEN + "Crosshair Animations toggled on" :
                EnumChatFormatting.RED + "Crosshair Animations toggled off";
        CrosshairAnimations.getClientPlayer().addChatMessage(new ChatComponentText(chatMessage));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings, BlockPos blockPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
