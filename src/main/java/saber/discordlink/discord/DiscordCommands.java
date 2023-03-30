package saber.discordlink.discord;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.listener.interaction.InteractionCreateListener;
import saber.discordlink.DiscordLink;
import saber.discordlink.database.linkCode;
import saber.discordlink.utils.LookUpFunction;

import java.util.Set;
import java.util.logging.Level;

public class DiscordCommands implements InteractionCreateListener {

    private final DiscordLink plugin;
    private final DiscordApi api;
    private final DiscordBot bot;

    public DiscordCommands(DiscordLink p1, DiscordBot bot){
        plugin = p1;
        this.bot = bot;
        api = bot.getApi();
    }

    @Override
    public void onInteractionCreate(InteractionCreateEvent event) {

        //Convert to slash command
        if (!event.getSlashCommandInteraction().isPresent()) return;
        SlashCommandInteraction slash = event.getSlashCommandInteraction().get();


        if (slash.getCommandName().equalsIgnoreCase("link")){
            InteractionImmediateResponseBuilder resp = event.getInteraction().createImmediateResponder();

            //Get the entered link code
            linkCode code = plugin.getDataManager().retrieveLinkCode(slash.getArgumentStringValueByName("LinkCode").get());
            if (code == null){
                resp.setContent(plugin.getConfig().getString("msg.linkExpiredInvalid"));
                resp.respond();
                return;
            }


            //Link the user in the database
            plugin.getDataManager().linkUser(code.getUser(), slash.getUser().getId());

            //Add the verified role
            Set<Role> verified = api.getRolesByName(plugin.getConfig().getString("verifiedRoleName"));
            if (verified.isEmpty()) plugin.getLogger().log(Level.INFO, "Verified role not found");
            else slash.getUser().addRole((Role) verified.toArray()[0]);

            //Loop through ranks and start adding when a match is found
            boolean higher = false;
            for (String x : plugin.getConfig().getStringList("ranksToRoles")){
                String[] rankrole = x.split(":");
                if (rankrole.length<2){
                    plugin.getLogger().log(Level.INFO, "Rank to role '" + x + "' not valid");
                }
                String rank = rankrole[0];
                String role = rankrole[1];
                if (higher || rank.equals(code.getRank())){
                    Set<Role> roleset = api.getRolesByName(role);
                    if (role.isEmpty()){
                        plugin.getLogger().log(Level.INFO, "Role " + role + " not recognized");
                    } else {
                        slash.getUser().addRole((Role) roleset.toArray()[0]);
                        higher = true;
                    }
                }
            }
            //Clear the link code
            plugin.getDataManager().removeCode(code.getCode());
            resp.setContent(plugin.getConfig().getString("msg.rolesAdded"));
            resp.respond();
        }
        else if (slash.getCommandName().equalsIgnoreCase("unlink")){
            //Remove the roles from the user
            bot.removeLinkRoles(slash.getUser());

            //Remove link from database
            plugin.getDataManager().unlinkUser(slash.getUser().getId());

            //Report success
            event.getInteraction()
                    .createImmediateResponder()
                    .setContent(plugin.getConfig().getString("msg.rolesRemoved"))
                    .respond();
        }
        else if (slash.getCommandName().equalsIgnoreCase("lookup")){

            //Run a lookup and return the value
            event.getInteraction()
                    .createImmediateResponder()
                    .setContent(LookUpFunction.lookUp(slash.getArgumentStringValueByName("LookUpValue").get(),plugin))
                    .respond();
        }

    }
}
