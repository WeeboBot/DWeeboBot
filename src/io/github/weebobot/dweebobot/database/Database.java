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
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.*;

public class Database {

    private static Connection conn;

    private static final String URL = "jdbc:mysql://localhost:3306/dweebo?";

    private static final String DATABASE = "dweebo";

    private static String DBPASSWORD;

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    /**
     * Creates a connection to the database.
     *
     * @return - true if connection is successful
     */
    public static boolean initDBConnection(String pass) {
        DBPASSWORD = pass;
        return initDBConnection();
    }

    private static boolean initDBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            logger.error("Unable to find Driver in classpath!",e);
            WLogger.logError(e);
            return false;
        }
        try {
            conn = DriverManager.getConnection(String.format("%suser=bot&password=%s", URL, DBPASSWORD));
        } catch (SQLException e) {
            logger.error("Unable to connect to the database!! Shutting Down!!", e);
            WLogger.logError(e);
            return false;
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
        Statement stmt4;
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
                stmt3.executeUpdate(String.format("CREATE TABLE %s.%sCommands LIKE %s.defaultCommands", DATABASE, guildID, DATABASE));
                stmt4=conn.createStatement();
                stmt4.closeOnCompletion();
                stmt4.executeUpdate(String.format(("INSERT %s.%sCommands SELECT * FROM %s.defaultCommands"), DATABASE, guildID, DATABASE));
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
        while(!initDBConnection());
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
            logger.error(String.format("Unable to execute statment: %s", sqlCommand));
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
        while(!initDBConnection());
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
        while(!initDBConnection());
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
        while(!initDBConnection());
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
     * @param gID - channel to get the option for without the leading #
     * @param option - Timeout Option
     * @return value if the option
     */
    public static String getOption(String gID, String option) {
        ResultSet rs=executeQuery(String.format("SELECT * FROM %s.%sOptions WHERE optionID=\'%s\'", DATABASE, gID, option));
        try {
            if(rs.next()) {
                return rs.getString(2);
            }
            return null;
        } catch (SQLException | NumberFormatException e) {
            logger.error(String.format("Unable to get option %s for %s", option, gID), e);
            WLogger.logError(e);
        }
        return null;
    }

    /**
     * @param gID - ID of the guild
     * @return String[] of guild info
     */
    public static String[] getWelcomeInfo(String gID) {
        String[] welcomeInfo = new String[3];
        welcomeInfo[0] = getOption(gID, GOptions.welcomeChannel.getOptionID());
        welcomeInfo[1] = getOption(gID, GOptions.welcomeMessage.getOptionID());
        welcomeInfo[2] = getOption(gID, GOptions.deleteWelcome.getOptionID());
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
     * @param gID - channel to add the command for, without the leading #
     * @param command - command to be added
     * @param parameters - parameters that should be passed
     * @param reply - reply to be sent on command
     * @param level
     */
    public static void addCommand(String gID, String command, String parameters, String reply, int level) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s.%sCommands VALUES(? , ?, ?, ?, ?)", DATABASE, gID));
            stmt.setString(1, command);
            stmt.setString(2, parameters);
            stmt.setString(3, reply);
            stmt.setInt(4, level);
            stmt.setBoolean(5, false);
        } catch (SQLException e) {
            logger.error("Unable to add command", e);
            WLogger.logError(e);
        }
        executeUpdate(stmt);
    }

    /**
     * @param gID - channel to get the custom commands for, without the leading #
     * @return result set of custom commands
     */
    public static ResultSet getCustomCommands(String gID) {
        return executeQuery(String.format("SELECT * FROM %s.%sCommands", DATABASE, gID));
    }

    /**
     * @param gID - Channel without the leading #
     * @param command - Command to be deleted
     * @return - true if the operation is successful, false otherwise
     */
    public static boolean delCommand(String gID, String command) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("DELETE FROM %s.%sCommands WHERE command=?", DATABASE, gID));
            stmt.setString(1, command);
        } catch (SQLException e) {
            logger.error("Unable to set option", e);
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    public static boolean updateCommand(String gID, String command, String parameters, String reply, int level, boolean isDefault) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("UPDATE %s.%sCommands SET command=?,parameters=?,reply=?,permissionLevel=?,isDefault=? WHERE command = ?", DATABASE, gID));
            stmt.setString(1, command);
            stmt.setString(2, parameters);
            stmt.setString(3, reply);
            stmt.setInt(4, level);
            stmt.setBoolean(5, isDefault);
            stmt.setString(6, command);
        } catch (SQLException e) {
            logger.error("Unable to update command %c%".replace("%c%", command));
            WLogger.logError(e);
        }
        return executeUpdate(stmt);
    }

    public static boolean updateCommandPermissions(String gID, String command, int level) {
        ResultSet rs = getCommand(gID, command);
        try {
            if(rs.next()) {
                return updateCommand(gID, command, rs.getString("parameters"), rs.getString("reply"), level, rs.getBoolean("isDefault"));
            }
        } catch (SQLException e) {
            logger.error("Unable to update permissions for %c%".replace("%c%", command));
            WLogger.logError(e);
        }
        return false;
    }

    public static ResultSet getCommand(String gID, String command) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(String.format("SELECT * FROM %s.%sCommands WHERE command=?", DATABASE, gID));
            stmt.setString(1, command);
        } catch (SQLException e) {
            logger.error("Unable to get command %c%".replace("%c%", command));
        }
        return executeQuery(stmt);
    }

    public static int getUserPermissionLevel(String uID, String gID) {
        ResultSet rs = executeQuery(String.format("SELECT * FROM %s.%sUsers WHERE userID=%s", DATABASE, gID, uID));
        try {
            if(rs.next()) {
                return rs.getInt("permissionLevel");
            }
            return -1;
        } catch (SQLException e) {
            logger.warn("An error occurred getting the user (" + uID + ") permission level for that guild (" + gID + ")", e);
            return -1;
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

    public static void setUserPermissionLevel(String uID, String gID, int level) {
        executeUpdate(String.format("UPDATE %s.%sUsers SET userID=%s,permissionLevel=%s WHERE userID=%s", DATABASE, gID, uID, level, uID));
    }

    public static void addUser(String uID, String gID, int level) {
        executeUpdate(String.format("INSERT INTO %s.%sUsers values(%s, %d)", DATABASE, gID, uID, level));
    }

    public static void addUser(String uID, String gID) {
        addUser(uID, gID, 0);
    }
}
