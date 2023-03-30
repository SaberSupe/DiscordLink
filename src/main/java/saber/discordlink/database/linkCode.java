package saber.discordlink.database;

import java.util.UUID;

public class linkCode {

    private String code;
    private UUID user;
    private String rank;
    private long timestamp;


    public linkCode(UUID user, String rank, String code){
        this.user = user;
        this.rank = rank;
        this.code = code;
        timestamp = System.currentTimeMillis()/1000;
    }

    public String getCode(){return code;}

    public String getRank(){return rank;}

    public UUID getUser(){return user;}

    public long getTimestamp(){return timestamp;}

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof linkCode)){ return false;}

        return ((linkCode) obj).code.equals(this.code);
    }
}
