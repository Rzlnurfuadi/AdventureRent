package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    
    // Sesuaikan dengan settingan MySQL di laptop masing-masing
    private static final String URL = "jdbc:mysql://localhost:3306/db_rental_camping";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Kosongkan jika menggunakan XAMPP default

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi ke Database Berhasil!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL tidak ditemukan! Pastikan file lib/mysql-connector-j-9.6.0.jar ada di project.");
            e.printStackTrace();
            connection = null;
        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke database! Pastikan MySQL/XAMPP sudah menyala dan database db_rental_camping sudah dibuat.");
            e.printStackTrace();
            connection = null;
        }
        return connection;
    }

    // Fungsi untuk menutup koneksi jika aplikasi selesai digunakan
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Koneksi database berhasil ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}