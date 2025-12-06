/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pbo.proyekakhirpbo.db;

/**
 *
 * @author joan clarissa hal
 */


import java.awt.Component;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane; // buat error handling
import javax.xml.transform.Result;


public class Konektor {
    private static final String URL = "jdbc:mysql://localhost:3306/db_tokobangunan"; 
    private static final String USER = "root";
    private static final String PASSWORD = "mysql";
    
    public static Connection getConnection() {
        try {
            Connection newConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database Connected"); // buat testing ntar remove aja
            return newConnection;
        } catch (SQLException e) {
            // popup buat user klo connection fail
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage());
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
