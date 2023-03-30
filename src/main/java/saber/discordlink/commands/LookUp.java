package saber.discordlink.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import saber.discordlink.DiscordLink;
import saber.discordlink.utils.LookUpFunction;

public class LookUp extends Command {

    private final DiscordLink plugin;

    public LookUp(DiscordLink p1){
        super("lookup");
        plugin = p1;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length == 0){
            commandSender.sendMessage(new TextComponent("Too few args"));
            return;
        }
        commandSender.sendMessage(new TextComponent(LookUpFunction.lookUp(strings[0], plugin)));
    }
}
