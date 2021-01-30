package org.cherry.config;

public class DbConfig {
    public static final String DB_HOST = "127.0.0.1";
    public static final String DB_PORT = "27017";
    public static final String DB_URL = "mongodb://" + DB_HOST + ":" + DB_PORT;

    public static final String BASE_NAME = "MagicRating";
    public static final String COLL_SESSION = "sessions";
    public static final String COLL_TEAMS = "teams";
    public static final String COLL_COMMENTS = "comments";
    public static final String COLL_SCHEDULE = "schedule";

    public static final String DATE_FORMAT = "dd/MM/yyyy";
}
