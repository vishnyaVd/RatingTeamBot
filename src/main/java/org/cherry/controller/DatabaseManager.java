package org.cherry.controller;


import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.cherry.config.DbConfig;
import org.cherry.model.Comment;
import org.cherry.model.Schedule;
import org.cherry.model.Team;
import org.cherry.properties.ChatStage;
import org.cherry.properties.DatabaseParameters;
import org.cherry.properties.DatabaseValues;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class DatabaseManager {
    private MongoDatabase database;
    private MongoClient mongoClient;

    public DatabaseManager() {
        mongoClient = new MongoClient(new MongoClientURI(DbConfig.DB_URL));
        database = mongoClient.getDatabase(DbConfig.BASE_NAME);
    }

    public void close() {
        mongoClient.close();
    }

    public void setDocumentParameter(String collectionName, String filterName, String filterValue, String parameter, String value) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        query.put(filterName, filterValue);
        MongoCursor<Document> cursor = collection.find(query).cursor();
        Bson filter = filterName.equals(DatabaseParameters.ID) ? eq(filterName, new ObjectId(filterValue)) : eq(filterName, filterValue);
        Bson updateOperation = set(parameter, value);
        try {
            collection.updateOne(filter, updateOperation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getDocumentParameter(String collectionName, String filterName, String filterValue, String parameter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        query.put(filterName, filterValue);
        MongoCursor<Document> cursor = collection.find(query).cursor();
        Document document = cursor.next();
        return document.get(parameter).toString();
    }

    public List<Team> getTeams() {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_TEAMS);
        List<Team> teams = new ArrayList<>();

        MongoCursor<Document> cursor = collection.find().cursor();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            teams.add(new Team(
                    document.get(DatabaseParameters.ID).toString(),
                    document.get(DatabaseParameters.TEAM_NAME).toString(),
                    document.get(DatabaseParameters.TEAM_RATING).toString(),
                    document.get(DatabaseParameters.PHOTO_ID).toString()
            ));
        }

        return teams;
    }

    public Team getTeamById(String id) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_TEAMS);

        BasicDBObject query = new BasicDBObject();
        query.put(DatabaseParameters.ID, new ObjectId(id));

        MongoCursor<Document> cursor = collection.find(query).cursor();
        Document document = cursor.next();

        return new Team(
                document.get(DatabaseParameters.ID).toString(),
                document.get(DatabaseParameters.TEAM_NAME).toString(),
                document.get(DatabaseParameters.TEAM_RATING).toString(),
                document.get(DatabaseParameters.PHOTO_ID).toString()
        );

    }

    public void setTeamRating(String teamId, String rating) {
        setDocumentParameter(DbConfig.COLL_TEAMS, DatabaseParameters.ID, teamId, DatabaseParameters.TEAM_RATING, rating);
    }

    public List<Comment> getCommentsByTeamId(String id) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_COMMENTS);
        List<Comment> comments = new ArrayList<>();

        BasicDBObject query = new BasicDBObject();
        query.put(DatabaseParameters.TEAM_ID, id);
        MongoCursor<Document> cursor = collection.find(query).cursor();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            String text = (document.get(DatabaseParameters.COMMENT_TEXT) == null) ? "-" : document.get(DatabaseParameters.COMMENT_TEXT).toString();
            comments.add(new Comment(
                    document.get(DatabaseParameters.ID).toString(),
                    document.get(DatabaseParameters.TEAM_ID).toString(),
                    document.get(DatabaseParameters.COMMENT_MARK).toString(),
                    text
            ));
        }

        return comments;
    }

    public void deleteDocumentParameter(String collectionName, String filterName, String filterValue, String parameter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Bson filter = eq(filterName, filterValue);
        Bson unset = unset(parameter);

        try {
            collection.updateOne(filter, unset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean containDoc(String collectionName, String filterName, String filterValue) {

        MongoCollection<Document> collection = database.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        query.put(filterName, filterValue);
        long count = collection.countDocuments(query);

        return (count == 0) ? false : true;
    }

    public Boolean containSession(String chatId) {
        return containDoc(DbConfig.COLL_SESSION, DatabaseParameters.CHAT_ID, chatId);
    }

    public void setSessionParameter(String chatId, String parameter, String value) {
        setDocumentParameter(DbConfig.COLL_SESSION, DatabaseParameters.CHAT_ID, chatId, parameter, value);
    }

    public String getSessionParameter(String chatId, String parameter) {
        return getDocumentParameter(DbConfig.COLL_SESSION, DatabaseParameters.CHAT_ID, chatId, parameter);
    }

    public void deleteSessionParameter(String chatId, String parameter) {
        deleteDocumentParameter(DbConfig.COLL_SESSION, DatabaseParameters.CHAT_ID, chatId, parameter);
    }

    public void makeSessionDoc(String chatId) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SESSION);
        Document session = new Document(DatabaseParameters.ID, new ObjectId());
        session.append(DatabaseParameters.CHAT_ID, chatId);
        session.append(DatabaseParameters.ROLE, DatabaseValues.USER);
        session.append(DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
        session.append(DatabaseParameters.LAST_RATING_MESSAGE_ID, "0");
        session.append(DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID, "0");
        try {
            collection.insertOne(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSessionDoc(String chatId) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SESSION);
        Bson filter = eq(DatabaseParameters.CHAT_ID, chatId);
        try {
            collection.deleteOne(filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearSessionDoc(String chatId) {
        try {
            deleteSessionDoc(chatId);
            makeSessionDoc(chatId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserRole(String chatId) {
        return getDocumentParameter(DbConfig.COLL_SESSION, DatabaseParameters.CHAT_ID, chatId, DatabaseParameters.ROLE);
    }

    public String addTeam() {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_TEAMS);

        ObjectId teamId = new ObjectId();
        Document chatStatus = new Document(DatabaseParameters.ID, teamId);
        chatStatus.append(DatabaseParameters.TEAM_NAME, DatabaseValues.TEAM_DEFAULT_NAME)
                .append(DatabaseParameters.TEAM_RATING, DatabaseValues.TEAM_DEFAULT_RATING)
                .append(DatabaseParameters.PHOTO_ID, DatabaseValues.TEAM_DEFAULT_PHOTO_ID);
        try {
            collection.insertOne(chatStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teamId.toString();
    }

    public void deleteTeam(String id) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_TEAMS);
        Bson filter = eq(DatabaseParameters.ID, new ObjectId(id));
        try {
            collection.deleteOne(filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String addCommentMark(String teamId, String mark) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_COMMENTS);

        ObjectId commentId = new ObjectId();
        Document chatStatus = new Document(DatabaseParameters.ID, commentId);
        chatStatus.append(DatabaseParameters.TEAM_ID, teamId)
                .append(DatabaseParameters.COMMENT_MARK, mark);
        try {
            collection.insertOne(chatStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentId.toString();
    }

    public void addCommentText(String commentId, String text) {
        setDocumentParameter(DbConfig.COLL_COMMENTS, DatabaseParameters.ID, commentId, DatabaseParameters.COMMENT_TEXT, text);
    }

    public void sumRating(String teamId) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_COMMENTS);

        List<Comment> comments = new ArrayList<>();
        Bson filter = eq(DatabaseParameters.TEAM_ID, teamId);

        MongoCursor<Document> cursor = collection.find(filter).cursor();

        Integer sum = 0;
        while (cursor.hasNext()) {
            Document document = cursor.next();
            sum += Integer.parseInt(document.get(DatabaseParameters.COMMENT_MARK).toString());
        }
        setTeamRating(teamId, sum.toString());
    }

    public void setTeamName(String teamId, String name) {
        setDocumentParameter(DbConfig.COLL_TEAMS, DatabaseParameters.ID, teamId, DatabaseParameters.TEAM_NAME, name);
    }

    public void setTeamPhoto(String teamId, String photoId) {
        setDocumentParameter(DbConfig.COLL_TEAMS, DatabaseParameters.ID, teamId, DatabaseParameters.PHOTO_ID, photoId);
    }

    public List<Schedule> getFullSchedule(){
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SCHEDULE);
        List<Schedule> schedules = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().cursor();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            Date tmpDate = null;
            try {
                tmpDate = new SimpleDateFormat(DbConfig.DATE_FORMAT).parse(document.get(DatabaseParameters.SCHEDULE_DATE).toString());
            } catch(java.text.ParseException e) {
                e.printStackTrace();
            }
            schedules.add(new Schedule(
                    document.get(DatabaseParameters.ID).toString(),
                    document.get(DatabaseParameters.PHOTO_ID).toString(),
                    tmpDate
            ));
        }

        return schedules;
    }

    public Schedule getScheduleByDate(String date){
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SCHEDULE);

        BasicDBObject query = new BasicDBObject();
        query.put(DatabaseParameters.SCHEDULE_DATE, date);

        MongoCursor<Document> cursor = collection.find(query).cursor();
        if (cursor.hasNext()){
            Document document = cursor.next();
            Date tmpDate = null;
            try {
                tmpDate = new SimpleDateFormat(DbConfig.DATE_FORMAT).parse(document.get(DatabaseParameters.SCHEDULE_DATE).toString());
            } catch(java.text.ParseException e) {
                e.printStackTrace();
            }
            return new Schedule(
                    document.get(DatabaseParameters.ID).toString(),
                    document.get(DatabaseParameters.PHOTO_ID).toString(),
                    tmpDate
            );
        }
        return  null;
    }

    public void setSchedulePhoto(String date, String photoId){
        setDocumentParameter(DbConfig.COLL_SCHEDULE, DatabaseParameters.SCHEDULE_DATE, date, DatabaseParameters.PHOTO_ID, photoId);
    }

    public void addScheduleDay(String date){
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SCHEDULE);
        Document day = new Document(DatabaseParameters.ID, new ObjectId());
        day.append(DatabaseParameters.SCHEDULE_DATE, date);
        day.append(DatabaseParameters.PHOTO_ID, DatabaseValues.DEFAULT_SCHEDULE);
        try {
            collection.insertOne(day);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSchedule(String date) {
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SCHEDULE);
        Bson filter = eq(DatabaseParameters.SCHEDULE_DATE, date);
        try {
            collection.deleteOne(filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllChatIds(){
        MongoCollection<Document> collection = database.getCollection(DbConfig.COLL_SESSION);
        List<String> ids = new ArrayList<>();

        MongoCursor<Document> cursor = collection.find().cursor();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            ids.add(document.get(DatabaseParameters.CHAT_ID).toString());
        }

        return ids;
    }
}

