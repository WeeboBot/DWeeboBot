/*      It's a Twitch bot, because we can.
 *    Copyright (C) 2015  Timothy Chandler, James Wolff
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.weebobot.dweebobot.database;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.util.GOptions;
import io.github.weebobot.dweebobot.util.TType;
import io.github.weebobot.dweebobot.util.ULevel;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    private static Connection conn;

    private static final String URL = "jdbc:mysql://localhost:3306/dweebo?";

    private static final String DATABASE = "dweebo";

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    /**
     * Creates a connection to the database.
     *
     * @return - true if connection is successful
     */
    public static boolean initDBConnection(String pass) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            logger.error("Unable to find Driver in classpath!",e);
            WLogger.logError(e);
            return false;
        }
        try {
            conn = DriverManager.getConnection(String.format("%suser=bot&password=%s", URL, pass));
        } catch (SQLException e) {
            logger.error("Unable to connect to the database!! Shutting Down!!", e);
            WLogger.logError(e);
            return true;
        }
        return true;
    }

    /**
     * Creates the tables for the provided channel
     *
     * @param guildID - the guild we are connecting to.
     * @return - true if it has to create the tables
     */
    public static boolean getChannelTables(String guildID) {
        Statement stmt;
        Statement stmt1;
        Statement stmt2;
        Statement stmt3;
        try {
            stmt = conn.createStatement();
            stmt.closeOnCompletion();
            stmt.executeQuery(String.format("SELECT * FROM %s.%sOptions", DATABASE, guildID));
            return false;
        } catch (SQLException e) {
            try {
                stmt1 = conn.createStatement();
                stmt1.closeOnCompletion();
                stmt1.executeUpdate(String.format("CREATE TABLE %s.%sOptions(optionID varchar(50), value varchar(4000), PRIMARY KEY (optionID))", DATABASE, guildID));
            } catch (SQLException ex) {
                logger.error(String.format("Unable to create table %sOptions!", guildID), ex );
                WLogger.logError(e);
            }
            try{
                stmt2=conn.createStatement();
                stmt2.closeOnCompletion();
                stmt2.executeUpdate(String.format("CREATE TABLE %s.%sUsers(userID varchar(25), permissionLevel INTEGER, PRIMARY KEY (userID))", DATABASE, guildID));
            }catch(SQLException ex){
                logger.error("Unable to create table Users!", ex);
                WLogger.logError(e);
            }
            try{
                stmt3=conn.createStatement();
                stmt3.closeOnCompletion();
                stmt3.executeUpdate(String.format("CREATE TABLE %s.%sCommands(command varchar(25), parameters varchar(255), reply varchar(4000), permissionLevel INTEGER, default BOOLEAN, PRIMARY KEY (command))", DATABASE, guildID));
            }catch(SQLException ex){
                logger.error("Unable to create table Commands!", ex);
                WLogger.logError(e);
            }
            return true;
        }
    }

    /**
     * Sends an update to the database (eg. INSERT, DELETE, etc.)
     *
     * @param sqlCommand - the command to be executed
     * @return - true if it successfully executes the update
     */
    private static boolean executeUpdate(String sqlCommand) {
        Statement stmt;
        try {
            stmt = conn.createStatement();
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            logger.warn(String.format("Unable to create connection for SQLCommand: %s", sqlCommand), e);
            WLogger.logError(e);
            return false;
        }
        try {
            stmt.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            logger.error(String.format("Unable to execute statment: %s", sqlCommand), e);
            WLogger.logError(e);
            return false;
        }
        return true;
    }

    /**
     * Sends a query to the database (eg. SELECT, etc.)
     * @param sqlQuery - the query to be executed
     * @return - the results of the query
     */
    private static ResultSet executeQuery(String sqlQuery) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt=conn.createStatement();
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            logger.warn(String.format("Unable to create connection for SQLQuery: %s", sqlQuery), e);
            WLogger.logError(e);
        }
        try {
            if (stmt != null) {
                rs = stmt.executeQuery(sqlQuery);
            }
        } catch (SQLException e) {
            logger.error(String.format("Unable to execute query: %s", sqlQuery), e);
            WLogger.logError(e);
        }
        return rs;
    }

    /**
     * Sends an update to the database (eg. INSERT, DELETE, etc.)
     *
     * @param stmt - the PreparedStatement to execute
     * @return - true if it successfully executes the update
     */
    private static boolean executeUpdate(PreparedStatement stmt) {
        try {
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            logger.warn("Unable to create connection for SQLCommand", e);
            WLogger.logError(e);
            return false;
        }
        try {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Unable to execute statment", e);
            WLogger.logError(e);
            return false;
        }
        return true;
    }

    /**
     * Sends a query to the database (eg. SELECT, etc.)
     * @param stmt - the PreparedStatement to be executed
     * @return - results of the query
     */
    private static ResultSet executeQuery(PreparedStatement stmt) {
        ResultSet rs = null;
        try {
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            logger.warn("Unable to create connection for SQLQuery", e);
            WLogger.logError(e);
        }
        try {
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            logger.error("Unable to execute query", e);
            WLogger.logError(e);
        }
        return rs;
    }

    /**
     * @param channelNoHash - channel to get the option for without the leading #
     * @param option - Timeout Option
     * @return value if the option
     */
    public static String getOption(String channelNoHash, String option) {
        ResultSet rs=executeQuery(String.format("SELECT * FROM %s.%sOptions WHERE optionID=\'%s\'", DATABASE, channelNoHash, option));
        try {
            if(rs.next()) {
                return rs.getString(2);
            }
            return null;
        } catch (SQLException | NumberFormatException e) {
            logger.error(String.format("Unable to get welcome message for %s", channelNoHash), e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @param gID - ID of the guild
     * @return String[] of guild info
     */
    public static String[] getWelcomeInfo(String gID) {
        String[] welcomeInfo = new String[2];
        welcomeInfo[0] = getOption(gID, GOptions.welcomeChannel.getOptionID());
        welcomeInfo[1] = getOption(gID, GOptions.welcomeMessage.getOptionID());
        return welcomeInfo;
    }

    /**
     * @param channelNoHash - channel to set the option for, without the leading #
     * @param option - timeout option
     * @param value - value to set for the option
     * @return true if the message is set successfully
     */
    public static boolean setOption(String channelNoHash, String option, String value) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("UPDATE %s.%sOptions SET optionID=?,value=? WHERE optionID=?", DATABASE, channelNoHash));
            stmt.setString(1, option.toLowerCase());
            stmt.setString(2, value+"");
            stmt.setString(3, option.toLowerCase());
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - channel to add the option for, without the leading #
     * @param option - timeout option
     * @param value - value to set the option to
     * @return true if the option is added successfully
     */
    public static boolean addOption(String channelNoHash, String option, String value) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sOptions VALUES(? , ?)", DATABASE, channelNoHash));
            stmt.setString(1, option.toLowerCase());
            stmt.setString(2, value+"");
        } catch (SQLException e) {
            logger.error("Unable to add option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - channel to add the auto reply to
     * @param keywords - keywords to trigger the auto reply
     * @param reply - auto reply to be sent on trigger
     */
    public static void addAutoReply(String channelNoHash, String keywords, String reply) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sCommands VALUES(? , ?, ?)", DATABASE, channelNoHash));
            stmt.setString(1, keywords);
            stmt.setString(2, "");
            stmt.setString(3, reply);
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - channel to get the auto replies for, without the leading #
     * @return a result set of the auto replies
     */
    public static ResultSet getAutoReplies(String channelNoHash) {
        return executeQuery(String.format("SELECT * FROM %s.%sCommands WHERE command NOT LIKE \'!%%'", DATABASE, channelNoHash));
    }

    /**
     * @param channelNoHash - channel to add the command for, without the leading #
     * @param command - command to be added
     * @param parameters - parameters that should be passed
     * @param reply - reply to be sent on command
     */
    public static void addCommand(String channelNoHash, String command, String parameters, String reply) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sCommands VALUES(? , ?, ?)", DATABASE, channelNoHash));
            stmt.setString(1, command);
            stmt.setString(2, parameters);
            stmt.setString(3, reply);
        } catch (SQLException e) {
            logger.error("Unable to add command", e);
            WLogger.logError(e);
        }
        executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - channel to get spam for
     * @return result set of spam words
     */
    public static ResultSet getSpam(String channelNoHash) {
        return executeQuery(String.format("SELECT * FROM %s.%sSpam", DATABASE, channelNoHash));
    }

    /**
     * @param channelNoHash - channel to delete the auto reply from, without the leading #
     * @param keywords - keywords of the auto reply
     * @return true if the auto reply is removed
     */
    public static boolean delAutoReply(String channelNoHash, String keywords) {
        return delCommand(channelNoHash, keywords);
    }

    /**
     * @param channelNoHash - channel to get the custom commands for, without the leading #
     * @return result set of custom commands
     */
    public static ResultSet getCustomCommands(String channelNoHash) {
        return executeQuery(String.format("SELECT * FROM %s.%sCommands", DATABASE, channelNoHash));
    }

    /**
     * @param channelNoHash - channel to add spam to, without the leading #
     * @param word - word to add to the table
     * @return true if the word is added
     */
    public static boolean addSpam(String channelNoHash, boolean emote, String word) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sSpam VALUES(?,?)", DATABASE, channelNoHash));
            stmt.setBoolean(1, emote);
            stmt.setString(2, word);
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - channel to delete the spam from, without the leading #
     * @param word - word to delete
     * @return true if the word is deleted
     */
    public static boolean delSpam(String channelNoHash, String word) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("DELETE FROM %s.%sSpam WHERE word=?", DATABASE, channelNoHash));
            stmt.setString(1, word);
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    /**
     * @param nick - person to add points to
     * @param channelNoHash - channel the user is in, without the leading #
     * @param amount - the number of points to add
     */
    public static void addPoints(String nick, String channelNoHash, int amount) {
        ResultSet rs = Database.executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userID=\'%s\'", DATABASE, channelNoHash, nick));
        try {
            if(rs.next()){
                String userLevel = rs.getString(2);
                int points = rs.getInt(3) + amount;
                boolean visible = rs.getBoolean(4);
                boolean regular = rs.getBoolean(5);
                Database.executeUpdate(String.format("UPDATE %s.%sUsers SET userID=\'%s\', userLevel=\'%s\', points=%d, visibility=%b, regular=%b WHERE userID=\'%s\'", DATABASE, channelNoHash, nick, userLevel, points, visible, regular, nick));
            }
        } catch (SQLException e) {
            logger.error("An Error occured updating "+nick+"'s points!\n", e);
            WLogger.logError(e);
        }
    }

    /**
     * @param sender - person to get points for
     * @param channelNoHash - channel the user is in, without the leading #
     * @return number of points the user has
     */
    public static String getPoints(String sender, String channelNoHash) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userID=\'%s\'", DATABASE, channelNoHash, sender));
        try {
            if(rs.next()) {
                return rs.getInt(3)+"";
            }
        } catch (SQLException e) {
            logger.error("An error occurred getting a user's points.", e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @param amount - number of players to get
     * @param channelNoHash - channel the people are in
     * @return formatted string of top x players
     */
    public static String topPlayers(int amount, String channelNoHash) {
        StringBuilder output = new StringBuilder();
        output.append("The top ");
        output.append(amount);
        output.append(" points holder(s) are: ");
        ResultSet rs=executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE visibility = true ORDER BY points DESC", DATABASE, channelNoHash));
        try {
            while(rs.next()&&amount>1){
                if(rs.getBoolean(3)) {
                    output.append(rs.getString(1));
                    output.append(": ");
                    output.append(rs.getInt(3));
                    output.append(", ");
                    amount--;
                }
            }
            output.append(rs.getString(1));
            output.append(": ");
            output.append(rs.getInt(3));
        } catch (SQLException e) {
            logger.error("Error occurred creating Top list!", e);
            WLogger.logError(e);
        }
        return output.toString();
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param username - user to set exemption for
     * @param visible - whether the user should be exempt or not
     * @return - true if the operation is successful, false otherwise
     */
    public static boolean topExemption(String channelNoHash, String username, boolean visible){
        ResultSet rs = Database.executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userID=\'%s\'", DATABASE, channelNoHash, username));
        try {
            if(rs.next()){
                String userLevel = rs.getString(2);
                int points = rs.getInt(3);
                boolean regular = rs.getBoolean(5);
                return Database.executeUpdate(String.format("UPDATE %s.%sUsers SET userID=\'%s\', userLevel=\'%s\', points=%d, visibility=%b, regular=%b WHERE userID=\'%s\'", DATABASE, channelNoHash, username, userLevel, points, visible, regular, username));
            }
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param command - Command to be deleted
     * @return - ture if the operation is successful, false otherwise
     */
    public static boolean delCommand(String channelNoHash, String command) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("DELETE FROM %s.%sCommands WHERE command=?", DATABASE, channelNoHash));
            stmt.setString(1, command);
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @return - ResultSet of Users with the level "Moderator"
     */
    public static ResultSet getMods(String channelNoHash) {
        return executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userLevel=\'%s\'", DATABASE, channelNoHash, ULevel.Moderator.getName()));
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param level - User level to get immunities for
     * @return - Boolean array of immunities
     */
    public static boolean[] getImmunities(String channelNoHash, String level){
        String immunity = getOption(channelNoHash, level.toLowerCase()+"Immunities");
        if(immunity == null) {
            return new boolean[]{false,false,false,false,false,false};
        }
        boolean[] immunities = new boolean[6];
        immunities[0] = immunity.charAt(0) == '1';
        immunities[1] = immunity.charAt(1) == '1';
        immunities[2] = immunity.charAt(2) == '1';
        immunities[3] = immunity.charAt(3) == '1';
        immunities[4] = immunity.charAt(4) == '1';
        immunities[5] = immunity.charAt(5) == '1';
        return immunities;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @return - Emote list for the channel as well as the global emote list
     */
    public static ArrayList<String> getEmoteListAsArray(String channelNoHash) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSpam WHERE emote=true", DATABASE, channelNoHash));
        ArrayList<String> emotes = new ArrayList<>();
        try {
            while(rs.next()) {
                emotes.add(rs.getString(2));
            }
            ArrayList<String> globalEmotes = getGlobalEmoteListAsArray();
            emotes.addAll(globalEmotes == null ? new ArrayList<>() : globalEmotes);
            return emotes;
        } catch (SQLException e) {
            logger.error("Unable to get the emote list for: " + channelNoHash, e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @return - Array of Global Emotes
     */
    private static ArrayList<String> getGlobalEmoteListAsArray() {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.globalEmotes", DATABASE));
        ArrayList<String> emotes = new ArrayList<>();
        try {
            while(rs.next()) {
                emotes.add(rs.getString(1));
            }
            return emotes;
        } catch (SQLException e) {
            logger.error("Unable to get the global emote list", e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @param user - User to get the song tables for
     * @return true if both tables exist, false otherwise
     */
    public static boolean getUserSongTables(String user){
        boolean queue = false;
        boolean list = false;
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, String.format("%sSongQueue", user), null);
            if (tables.next()){
                queue=true;
            }else{
                executeUpdate(String.format("CREATE TABLE %s.%sSongQueue (queueID INTEGER AUTO_INCREMENT, songURL varchar(255), songTitle varchar(255), songID INTEGER, requester varchar(25), PRIMARY KEY(queueID))", DATABASE, user));
                System.out.println(String.format("Created song queue table for %s", user));
            }
            tables = dbm.getTables(null, null, String.format("%sSongList", user), null);
            if (tables.next()){
                list=true;
            }else{
                executeUpdate(String.format("CREATE TABLE %s.%sSongList (listID INTEGER AUTO_INCREMENT, songURL varchar(255), songTitle varchar(255), songID INTEGER, PRIMARY KEY(listID))", DATABASE, user));
                System.out.println(String.format("Created song list table for %s", user));
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue checking for %s's song tables", user), e);
            WLogger.logError(e);
        }
        return queue && list;
    }

    /**
     * @param songURL - URL of the song to be added
     * @param songTitle - title of the song to be added
     * @return - ID the song was given
     */
    public static int addSongToGlobalList(String songURL, String songTitle) {
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.globalSongList (songURL,songTitle) VALUES(?,?)", DATABASE));
            stmt.setString(1, songURL);
            stmt.setString(2, songTitle);
            executeUpdate(stmt);
            ResultSet rs = executeQuery(String.format("SELECT COUNT(*) FROM %s.globalSongList", DATABASE));
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param sender - The person who requested the song
     * @param videoInformation - Array of info on the song
     * @return - The status of the addition
     */
    public static String addSongToQueue(String channelNoHash, String sender, String[] videoInformation){
        if(isInQueue(channelNoHash, Integer.valueOf(videoInformation[2]))){
            return String.format("The song \"%s\" is already in the song queue!", videoInformation[1]);
        }
        if(executeUpdate(String.format("INSERT INTO %s.%sSongQueue (songURL,songTitle,songID,requester) VALUES (\'%s\',\'%s\',%d,\'%s\')", DATABASE, channelNoHash, videoInformation[0], videoInformation[1], Integer.valueOf(videoInformation[2]), sender))){
            return String.format("The song \"%s\" has been successfully added to the song queue!", videoInformation[1]);
        }
        return String.format("There was an error adding the song %s to the song queue!", videoInformation[1]);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoID - ID for the video in question
     * @return - true if the video is in the users list, false otherwise
     */
    public static boolean isInQueue(String channelNoHash, int videoID){
        try {
            ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongQueue WHERE songID=%d", DATABASE, channelNoHash, videoID));
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue checking if song %d is in %s's song queue", videoID, channelNoHash), e);
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoLink - Link for the video in question
     * @return - true if the video is in the users list, false otherwise
     */
    public static boolean isInQueue(String channelNoHash, String videoLink){
        try {
            ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongQueue WHERE songURL=\'%s\'", DATABASE, channelNoHash, videoLink));
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue checking if song %s is in %s's song queue", videoLink, channelNoHash), e);
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoInformation - Array of info on the video to delete
     * @return - The status of the deletion
     */
    public static String deleteSongFromQueue(String channelNoHash, String[] videoInformation){
        if(!isInQueue(channelNoHash, Integer.valueOf(videoInformation[2]))){
            return String.format("The song %s is not in the song queue!", videoInformation[1]);
        }
        if(executeUpdate(String.format("DELETE FROM %s.%sSongQueue WHERE songID=%d)", DATABASE, channelNoHash, Integer.valueOf(videoInformation[2])))){
            return String.format("The song %s has been successfully removed from the song queue!", videoInformation[1]);
        }
        return String.format("There was an error removing the song %s from the song queue!", videoInformation[1]);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param sender - the person who requested the song
     * @param videoInformation - Array of info on the video
     * @return - Status of the addition to the users list
     */
    public static String addSongToList(String channelNoHash, String sender, String[] videoInformation){
        if(isInQueue(channelNoHash, Integer.valueOf(videoInformation[3]))){
            return String.format("The song %s is already in your playlist!", videoInformation[1]);
        }
        if(executeUpdate(String.format("INSERT INTO %s.%sSongList VALUES (\'%s\',\'%s\',%d)", DATABASE, sender, videoInformation[0], videoInformation[1], Integer.valueOf(videoInformation[2])))){
            return String.format("The song %s has been successfully added to your personal playlist!", videoInformation[1]);
        }
        return String.format("There was an error adding the song %s to your playlist!", videoInformation[1]);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoID - Video to check for the existence of
     * @return - true if the video is in the list, false otherwise
     */
    public static boolean isInList(String channelNoHash, int videoID){
        try {
            ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongList WHERE songID=%d", DATABASE, channelNoHash, videoID));
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue checking if the song %s is in %s's playlist", videoID, channelNoHash), e);
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoLink - Video to check for the existence of
     * @return - true if the video is in the list, false otherwise
     */
    public static boolean isInList(String channelNoHash, String videoLink){
        try {
            ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongList WHERE songURL=\'%s\'", DATABASE, channelNoHash, videoLink));
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue checking if song %s is in %s's song queue", videoLink, channelNoHash), e);
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param channelNoHash - Channel without the leading Hash
     * @param videoInformation - Array of information for the video to be deleted
     * @return - Status on the deletion of the song
     */
    public static String deleteSongFromList(String channelNoHash, String[] videoInformation){
        if(!isInList(channelNoHash, Integer.valueOf(videoInformation[3]))){
            return String.format("The song %s is not in your playlist!", videoInformation[2]);
        }
        if(executeUpdate(String.format("DELETE FROM %s.%sSongList WHERE songID=%d)", DATABASE, channelNoHash, Integer.valueOf(videoInformation[3])))){
            return String.format("The song %s has been successfully removed from your playlist!", videoInformation[2]);
        }
        return String.format("There was an error removing the song %s from your playlist!", videoInformation[2]);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param videoLink - Video to get the info for
     * @return - Array of info for the video provided
     */
    public static String[] getSongInfoFromLink(String channelNoHash, String videoLink){
        try {
            ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongList WHERE songURL=\'%s\'", DATABASE, channelNoHash, videoLink));
            if(rs.next()){
                String[] videoInformation = new String[4];
                videoInformation[0] = "" + rs.getInt(1);
                videoInformation[1] = rs.getString(2);
                videoInformation[2] = rs.getString(3);
                videoInformation[3] = "" + rs.getInt(4);
                return videoInformation;
            }
        } catch (SQLException e) {
            logger.error(String.format("There was an issue getting the info for the song %s", videoLink), e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @param channelNoHash - Channel without the leading hash
     * @return The song at the top of the channel specified's queue.
     */
    public static String getCurrentSong(String channelNoHash) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sSongQueue ORDER BY queueID ASCENDING", DATABASE, channelNoHash));
        try {
            if(rs.next()) {
                return rs.getString("songURL");
            }
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        return null;
    }

//    public static String getNewSongID(String channelNoHash, String songUrl, String songTitle) {
//        PreparedStatement stmt = null;
//        try {
//            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sSongList (songURL,songTitle,songID) VALUES (?,?,?)", DATABASE, channelNoHash));
//            stmt.setString(1, songUrl);
//            stmt.setString(2, songTitle);
//            stmt.setString(3, );
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        executeUpdate(stmt);
//        String[] info = getSongInfoFromLink(channelNoHash, songUrl);
//        executeUpdate(String.format("UPDATE %s.%sSongList SET listID=%s, songURL=\'%s\', songTitle=\'%s\', songID=0", DATABASE, channelNoHash, info[0], info[1], info[2], info[0]));
//        return info[0];
//    }

    /**
     * @param emote - emote to check the database for
     * @return - true if the emote is in the database, false otherwise
     */
    public static boolean emoteExists(String emote) {
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement(String.format("SELECT * FROM %s.globalEmotes WHERE emote=?", DATABASE));
            stmt.setString(1, emote);

            return executeQuery(stmt).next();
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        return false;
    }

    /**
     * @param emote - emote to add to the database
     */
    public static void addEmote(String emote) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.globalEmotes VALUES (?)", DATABASE));
            stmt.setString(1, emote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        executeUpdate(stmt);
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param usage - What the message is used for
     * @param message - The message to be added
     */
    public static void addMessage(String channelNoHash, String usage, String message) {
        executeUpdate(String.format("INSERT INTO %s.customMessages VALUES(\'%s\', \'%s\', \'%s\')", DATABASE, channelNoHash, usage, message));
    }

    /**
     * @param channelNoHash - Channel without the leading #
     * @param usage - What the message is used for
     * @param message - message to be deleted from the database
     * @return true if it the message deleted, false otherwise
     */
    public static boolean delMessage(String channelNoHash, String usage, String message) {
        return executeUpdate(String.format("DELETE FROM %s.customMessages WHERE channel=\'%s\' AND regime=\'%s\' AND message=\'%s\'", DATABASE, channelNoHash, usage, message));
    }

    /**
     * @param channelNoHash - Channel without the leqading #
     * @param tType - the Timeout Type to get messages for
     * @return - An array of messages
     */
    public static ArrayList<String> getTimeoutMessages(String channelNoHash, TType tType) {
        ArrayList<String> messages = new ArrayList<>();
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement(String.format("SELECT * FROM %s.customMessages WHERE channel=? AND regime=?", DATABASE));
            stmt.setString(1, channelNoHash);
            stmt.setString(2, tType.getId());
            ResultSet rs = executeQuery(stmt);
            while(rs.next()) {
                messages.add(rs.getString(2));
            }
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        return messages;
    }

    public static int getUserPermissionLevel(IUser sender, IGuild guild) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userID=%s", DATABASE, guild.getID(), sender.getID()));
        try {
            if(rs.next()) {
                return rs.getInt("permissionLevel");
            }
            return 0;
        } catch (SQLException e) {
            logger.warn("An error occurred getting the user (" + sender.getID() + ") permission level for that guild (" + guild.getID() + ")", e);
            return 0;
        }
    }

    public static int getPermissionLevel(String commandText, IGuild guild) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sCommands WHERE command=%s", DATABASE, guild.getID(), commandText));
        try {
            if(rs.next()) {
                return rs.getInt("permissionLevel");
            }
            return Main.MAX_USER_LEVEL-1;
        } catch (SQLException e) {
            logger.warn("An error occurred getting the command (" + commandText + ") permission level for that guild (" + guild.getID() + ")", e);
            return Main.MAX_USER_LEVEL-1;
        }
    }

    public static void addWelcomedUser(String gID, String uID) {
        executeUpdate(String.format("INSERT INTO %s.welcomedUsers values(%s, %s)", DATABASE, gID, uID));
    }
}