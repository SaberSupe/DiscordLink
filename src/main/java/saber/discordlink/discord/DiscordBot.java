package saber.discordlink.discord;


import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import saber.discordlink.DiscordLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DiscordBot {
    private DiscordApi api;
    private final DiscordLink plugin;

    public DiscordBot(DiscordLink p1){
        plugin = p1;

        //Initialize bot
        api = new DiscordApiBuilder()
                .setToken(plugin.getConfig().getString("discordBotToken"))
                .addIntents(Intent.MESSAGE_CONTENT)
                .login().join();

        //Register commands
        SlashCommand linkCommand = SlashCommand.with("link", "Links your minecraft and discord accounts",
                        Arrays.asList(SlashCommandOption.createStringOption("LinkCode","The code given from /link in minecraft",true)))
                .createGlobal(api)
                .join();

        SlashCommand unlinkcommand = SlashCommand.with("unlink", "Unlinks your minecraft and discord accounts")
                .createGlobal(api)
                .join();

        SlashCommand lookupcommand = SlashCommand.with("lookup", "Lookup link information for a minecraft username, UUID or discord snowflake",
                        Arrays.asList(SlashCommandOption.createStringOption("LookUpValue","A online minecraft player, a minecraft UUID or a discord snowflake id",true)))
                .createGlobal(api)
                .join();

        //Register listened
        api.addInteractionCreateListener(new DiscordCommands(plugin, this));
    }

    public void removeLinkRoles(User user){
        //Get the roles
        List<String> rankRole = plugin.getConfig().getStringList("ranksToRoles");
        List<String> roles = new ArrayList<>();
        for (String x : rankRole){
            String[] split = x.split(":");
            if (split.length > 1) roles.add(split[1]);
        }

        //include the verified role
        roles.add(plugin.getConfig().getString("verifiedRoleName"));

        //Loop through removing all the relevant ones
        for (Server y : api.getServers()){
            for (Role x : y.getRoles()){
                if (roles.contains(x.getName())){
                    y.removeRoleFromUser(user, x);
                }
            }
        }
    }

    public void removeLinkRoles(long snowflake){
        //Get the user from the server
        User user;
        try {
            CompletableFuture<User> temp = api.getUserById(snowflake);
            temp.join();
            user = temp.get();
        }catch(Exception e){
            plugin.getLogger().log(Level.INFO, "Get user by snowflake failed");
            e.printStackTrace();
            return;
        }
        if (user == null){
            plugin.getLogger().log(Level.INFO, "User not found from snowflake");
            return;
        }

        //Remove roles
        removeLinkRoles(user);

    }

    public DiscordApi getApi(){return api;}
}
