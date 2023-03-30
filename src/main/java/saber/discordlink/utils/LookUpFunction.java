package saber.discordlink.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import saber.discordlink.DiscordLink;

import java.util.UUID;

public class LookUpFunction {

    public static String lookUp(String lookUpValue, DiscordLink plugin){

        //Check if the given string is an online player
        ProxiedPlayer player = plugin.getProxy().getPlayer(lookUpValue);
        UUID userUUID;
        if (player != null){
            userUUID = player.getUniqueId();
        } else{
            try {
                //Check if the given string is a UUID
                userUUID = UUID.fromString(lookUpValue);
            } catch (IllegalArgumentException e){
                userUUID = null;
            }
        }

        if (userUUID != null){
            //send back snowflake id if it was minecraft player or uuid
            long snowflake = plugin.getDataManager().getSnowflake(userUUID);
            if (snowflake == -1) return plugin.getConfig().getString("msg.lookUp.noLink");
            else return plugin.getConfig().getString("msg.lookUp.linkSnowflake").replace("{snowflake}",snowflake + "");
        }

        long snowflake = -1;
        try {
            //Check if they entered a long
            snowflake = Long.parseLong(lookUpValue);
        } catch (NumberFormatException ignored){

        }

        if (snowflake != -1){
            //Check if that long is a snowflake id and send result if so
            userUUID = plugin.getDataManager().getUUID(snowflake);
            if (userUUID != null){
                return plugin.getConfig().getString("msg.lookUp.linkUUID").replace("{UUID}", userUUID.toString());
            } else {
                return plugin.getConfig().getString("msg.lookUp.noLinkSnowflake").replace("{snowflake}", snowflake + "");
            }
        }

        //Return invalid
        return plugin.getConfig().getString("msg.lookUp.invalidEntry").replace("{lookup}", lookUpValue);
    }
}
