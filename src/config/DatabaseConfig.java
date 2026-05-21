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
            // Cek jika koneksi belum dibuat atau sudah tertutup
            if (connection == null || connection.isClosed()) {
                // Memastikan driver MySQL dimuat (opsional untuk JDBC modern, tapi aman buat tugas kuliah)
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi ke Database Berhasil!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL tidak ditemukan! Periksa folder lib/ kalian.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke database! Pastikan MySQL/XAMPP sudah menyala.");
            e.printStackTrace();
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