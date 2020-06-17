package com.songoda.epicsellwands.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicsellwands.EpicSellWands;
import com.songoda.epicsellwands.gui.GuiAdmin;
import com.songoda.epicsellwands.wand.Wand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAdmin extends AbstractCommand {

    EpicSellWands plugin;

    public CommandAdmin(EpicSellWands plugin) {
        super(CommandType.PLAYER_ONLY, "admin");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        plugin.getGuiManager().showGUI((Player) sender, new GuiAdmin(plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicsellwands.admin";
    }

    @Override
    public String getSyntax() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Access admin panel.";
    }
}
