package com.craftaro.epicsellwands.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicsellwands.EpicSellWands;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    EpicSellWands plugin;

    public CommandReload() {
        super(CommandType.CONSOLE_OK, "reload");
        plugin = EpicSellWands.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        plugin.reloadConfig();
        plugin.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
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
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }

}
