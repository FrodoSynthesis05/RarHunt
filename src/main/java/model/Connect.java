/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Julian
 */
public class Connect {

private Connection conexion;
private ResultSet rst;
private Statement sentencia;

private static final String driver = "org.sqlite.JDBC";
private static final String url = "jdbc:sqlite:rarbg_db.sqlite";

/**
 * Class constructor accessible from elsewhere.
 */
public Connect() {

}

/**
 * Establishes a connection with the SQLite database.
 *
 * @return Established connection.
 */
public Connection conectar() {
    this.conexion = null;

    try {
        Class.forName(driver);
        this.conexion = DriverManager.getConnection(url);
    } catch (ClassNotFoundException | SQLException e) {
        System.out.println("Mysql()  ERROR :: " + e.getMessage());
    }
    return this.conexion;
}

/**
 * Get current connection.
 *
 * @return Current connection.
 */
public Connection getConnection() {
    return this.conexion;
}

/**
 * Closes currently open connections through a call to Connection.close().
 *
 * @throws SQLException If connection cannot be closed.
 */
public void closeConnection() throws SQLException {
    conexion.close();
}

/**
 * Reads existing records off of database table.
 *
 * @param sql SQL statement to be used.
 * @return Output for the provided SQL statement after processing by the server.
 * @throws SQLException If SQL server encounters an error with provided
 * statement.
 */
public ResultSet leer(String sql) throws SQLException {
    this.sentencia = this.conexion.createStatement();
    this.rst = sentencia.executeQuery(sql);
    return rst;
}

/**
 * Executes an SQL statement for creating, updating, or deleting records.
 *
 * @param sql SQL statement to be executed.
 * @return The count of rows affected by the SQL statement.
 * @throws SQLException If SQL server encounters an error with provided
 * statement.
 */
public int ejecutar(String sql) throws SQLException {
    this.sentencia = this.conexion.createStatement();
    return sentencia.executeUpdate(sql);
}


}
