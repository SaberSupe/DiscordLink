package saber.discordlink.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import saber.discordlink.DiscordLink;
import saber.discordlink.database.linkCode;

public class Link extends Command {

    private final DiscordLink plugin;
    public Link(DiscordLink p1){
        super("Link");
        plugin = p1;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {

        if (!(commandSender instanceof ProxiedPlayer)){
            commandSender.sendMessage(new TextComponent(plugin.getConfig().getString("msg.mustBePlayer")));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        if (plugin.getDataManager().getSnowflake(player.getUniqueId()) != -1){
            player.sendMessage(new TextComponent(plugin.getConfig().getString("msg.discordAlreadyLinked")));
            return;
        }

        linkCode code = plugin.getDataManager().createLinkCode(player);

        if (code == null){
            player.sendMessage(new TextComponent(plugin.getConfig().getString("msg.notDonor")));
            return;
        }

        player.sendMessage(new TextComponent(plugin.getConfig().getString("msg.codeGiven").replace("{code}",code.getCode().toString())));
    }
}
