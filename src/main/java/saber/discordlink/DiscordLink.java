package saber.discordlink;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import saber.discordlink.commands.Link;
import saber.discordlink.commands.Unlink;
import saber.discordlink.commands.LookUp;
import saber.discordlink.database.DataManager;
import saber.discordlink.discord.DiscordBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public final class DiscordLink extends Plugin {

    private DataManager datman;
    private Configuration config;
    private DiscordBot bot;

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Make the config if it doesn't exist then load
        try{
            makeConfig();
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e){
            e.printStackTrace();
        }

        if (config.getString("MySQL.address").equalsIgnoreCase("0.0.0.0")){
            getLogger().log(Level.INFO, "Please enter database info in config, Disabling");
            return;
        }

        //Initialize bot and data manager
        datman = new DataManager(this);
        bot = new DiscordBot(this);

        //Register commands
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Unlink(this));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Link(this));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new LookUp(this));

    }

    public void makeConfig() throws IOException {
        // Create plugin config folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
            InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            in.transferTo(outputStream); // Throws IOException
        }
    }

    public Configuration getConfig(){
        return config;
    }

    public DataManager getDataManager(){ return datman; }

    public DiscordBot getBot(){ return bot; }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
