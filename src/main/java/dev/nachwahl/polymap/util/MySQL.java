package dev.nachwahl.polymap.util;

import java.sql.*;
import java.util.UUID;

public class MySQL {
    public static Connection con;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void connect() {
        FileBuilder fb = new FileBuilder("plugins/PolyMap", "mysql.yml");
        String host = fb.getString("mysql.host");
        String port = fb.getString("mysql.port");
        String database = fb.getString("mysql.database");
        String user = fb.getString("mysql.user");
        String password = fb.getString("mysql.password");
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
                System.out.println("[PolyMap]" + ANSI_GREEN + " MySQL connection ok!" + ANSI_RESET);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("[PolyMap]" + ANSI_RED + " MySQL connection error" + ANSI_RESET);
            }
        }
    }


    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                System.out.println("[PolyMap] MySQL connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean isConnected() {
        try {
            return (con != null && (!con.isClosed()));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Connection getConnection() {
        if (!isConnected())
            connect();
        return con;
    }


    public static void createTables() {

        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `regions` (`uid` varchar(255) NOT NULL,`createdDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,`username` varchar(255) NOT NULL,`useruuid` varchar(255) NOT NULL, `data` text NOT NULL,`city` varchar(255) NOT NULL,`area` INT NOT NULL, KEY (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            ps.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }



    }

    public static void createRegion(Region region) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("INSERT INTO regions (createdDate, uid, username, useruuid, data, city, area) VALUES (?, ?, ?, ?, ?, ?, ?)");
        ps.setTimestamp(1, new Timestamp(region.getCreatedDate().getTime()));
        ps.setString(2, region.getUid().toString());
        ps.setString(3, region.getUsername());
        ps.setString(4, region.getUseruuid().toString());
        ps.setString(5, region.getData());
        ps.setString(6, region.getCity());
        ps.setInt(7, region.getArea());
        ps.executeUpdate();
    }

    public static void addArea(int area) throws SQLException {
        setTotalArea(getTotalArea()+area);
    }

    public static void setTotalArea(int amount) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("UPDATE stats SET value = ? WHERE `prop` = 'totalArea'");
        ps.setInt(1, amount);
        ps.executeUpdate();
    }


    public static Integer getTotalArea() throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM stats WHERE `prop` = 'totalArea'");
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next())
            return resultSet.getInt("value");
        return -1;
    }

    public static Boolean checkUser(UUID uuid) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM userLinks WHERE mcuuid = ?");
        ps.setString(1, uuid.toString());
        ResultSet resultSet = ps.executeQuery();
        return resultSet.next();
    }

    public static void createUser(UUID uuid, String mail) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("INSERT INTO userLinks (mcuuid, discordMail) VALUES (?, ?)");
        ps.setString(1, uuid.toString());
        ps.setString(2, mail);
        ps.executeUpdate();
    }
    public static void deleteUser(UUID uuid) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("DELETE FROM `userLinks` WHERE  `mcuuid`=?;");
        ps.setString(1, uuid.toString());
        ps.executeUpdate();
    }




}

