package saber.discordlink.database;

import com.zaxxer.hikari.HikariDataSource;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import saber.discordlink.DiscordLink;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private final DiscordLink plugin;
    private HikariDataSource hikari;
    private ArrayList<linkCode> codeList = new ArrayList<>();
    private Random rand = new Random(System.nanoTime());

    public DataManager(DiscordLink p1){
        plugin = p1;

        Configuration conf = plugin.getConfig();

        //Set up the database connection
        hikari = new HikariDataSource();
        hikari.setJdbcUrl("jdbc:mysql://" + conf.get("MySQL.address") + ":" + conf.get("MySQL.port") + "/" + conf.get("MySQL.database") + "?autoReconnect=true&useSSL=false");
        hikari.addDataSourceProperty("user", conf.get("MySQL.username"));
        hikari.addDataSourceProperty("password", conf.get("MySQL.password"));

        createTable();
    }


    private void createTable(){
        //Create the database tables
        try (Connection datacon = hikari.getConnection();
             Statement state = datacon.createStatement()) {

            state.executeUpdate("CREATE TABLE IF NOT EXISTS DiscordRankLink (MinecraftUUID VARCHAR(36), SnowflakeID bigint unsigned)");

            state.close();
        } catch (SQLException e){
            plugin.getLogger().log(Level.INFO, "createTable Failed, is the database correct in the config?");
        }
    }

    public void linkUser(UUID user, long snowflake){
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO DiscordRankLink(MinecraftUUID, SnowflakeID) VALUES(?, ?)")){

            //Prepare and run sql insert statement to put the link into the database
            pstmt.setString(1, user.toString());
            pstmt.setLong(2, snowflake);
            pstmt.execute();
            pstmt.close();


        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "linkUser Failed");
            e.printStackTrace();
        }
    }

    public void unlinkUser(UUID user){
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM DiscordRankLink WHERE MinecraftUUID = ?;")){

            //Prepare and run sql delete to remove link from database
            pstmt.setString(1, user.toString());
            pstmt.execute();
            pstmt.close();


        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "unlinkUser Failed");
            e.printStackTrace();
        }
    }

    public void unlinkUser(long snowflake){
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM DiscordRankLink WHERE SnowflakeID = ?;")){

            //Prepare and run sql delete to remove link from database
            pstmt.setLong(1, snowflake);
            pstmt.execute();
            pstmt.close();


        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "unlinkUser Failed");
            e.printStackTrace();
        }
    }

    public long getSnowflake(UUID uniqueId) {
        try(Connection connection = hikari.getConnection();
            //Set up select statements
            PreparedStatement pstmt = connection.prepareStatement("SELECT SnowflakeID FROM  DiscordRankLink WHERE MinecraftUUID = ?;")) {

            //Run query to get the snowflake
            pstmt.setString(1, uniqueId.toString());
            ResultSet rs = pstmt.executeQuery();
            long snowflake;
            if(rs.next()){
                snowflake = rs.getLong(1);
            } else {return -1;}
            pstmt.close();

            return snowflake;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "getSnowflake failed");
            e.printStackTrace();
            return -1;
        }
    }

    public UUID getUUID(long snowflake) {
        try(Connection connection = hikari.getConnection();
            //Set up select statements
            PreparedStatement pstmt = connection.prepareStatement("SELECT MinecraftUUID FROM  DiscordRankLink WHERE SnowflakeID = ?;")) {

            //Run query to get UUID
            pstmt.setLong(1, snowflake);
            ResultSet rs = pstmt.executeQuery();
            UUID user = null;
            if(rs.next()){
                user = UUID.fromString(rs.getString(1));
            } else {return null;}
            pstmt.close();

            return user;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "getUUID failed");
            e.printStackTrace();
            return null;
        }
    }

    public linkCode createLinkCode(ProxiedPlayer player){
        //Get the highest user rank
        String rank = null;
        for (String x : plugin.getConfig().getStringList("ranksToRoles")){
            String temprank = x.split(":")[0];
            if (player.hasPermission("group." + temprank)){
                rank = x.split(":")[0];
                break;
            }
        }

        //If they don't have a donor rank
        if (rank == null){return null;}

        purgeExpired();

        //check if they already have an active linkcode
        linkCode code = null;
        for (linkCode x : codeList){
            if (x.getUser().equals(player.getUniqueId())) code = x;
        }
        //If not, make one and add to list
        if (code == null) {
            code = new linkCode(player.getUniqueId(), rank, generateCode());
            while (codeList.contains(code)) code = new linkCode(player.getUniqueId(), rank, generateCode());
            codeList.add(code);
        }

        return code;
    }

    public linkCode retrieveLinkCode(String code){
        purgeExpired();


        for (linkCode x : codeList){
            if (x.getCode().equals(code)){
                return x;
            }
        }
        return null;
    }

    public void removeCode(String code){
        purgeExpired();

        codeList.removeIf(x -> x.getCode().equals(code));
    }

    private String generateCode(){

        //Generate a unique 8 char alphanumerical linkcode
        int linkCodeLength = 8;
        String code = "";
        for (int i = 0; i < linkCodeLength; i++){
            int random = rand.nextInt(); //Get random number
            if (random < 0) random = random*-1; //Make it positive
            random = random % (10+26+26); //Reduce it to the number of digits + Capital Letters + lower case letters
            random+=48; //Increase it so that value 0 lines up with char '0'
            if (random > 57) random+=7; //Increase any values that don't line up with digits to line up with capital letters
            if (random > 90) random+=6; //Increase any that don't line up with capitals to line up with lower case letters
            code = code + (char) random; //Add the char to the code
        }

        return code;
    }

    private void purgeExpired(){
        long curtime = System.currentTimeMillis()/1000;
        long life = plugin.getConfig().getLong("codeTimeValid");

        //Remove any linkCodes older than the max time allowed
        codeList.removeIf(x -> (curtime - x.getTimestamp()) > life);
    }


}
