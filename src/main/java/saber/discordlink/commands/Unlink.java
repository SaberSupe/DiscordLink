package saber.discordlink.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import saber.discordlink.DiscordLink;

public class Unlink extends Command {

    private final DiscordLink plugin;
    public Unlink(DiscordLink p1){
        super("Unlink");
        plugin = p1;
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)){
            commandSender.sendMessage(new TextComponent(plugin.getConfig().getString("msg.mustBePlayer")));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        long snowflake = plugin.getDataManager().getSnowflake(player.getUniqueId());
        if (snowflake == -1){
            player.sendMessage(new TextComponent(plugin.getConfig().getString("msg.discordNotLinked")));
            return;
        }
        plugin.getBot().removeLinkRoles(snowflake);

        plugin.getDataManager().unlinkUser(player.getUniqueId());

        player.sendMessage(new TextComponent(plugin.getConfig().getString("msg.rolesRemoved")));
    }
}
