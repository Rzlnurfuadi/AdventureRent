package modules.item.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modules.item.models.Item;
import config.DatabaseConfig;

public class ItemDAOImpl implements ItemDAO {

    @Override
    public List<Item> tampilkanSemuaBarang() {
        List<Item> listBarang = new ArrayList<>();
        String sql = "SELECT * FROM items"; 

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id_barang")); 
                item.setNama(rs.getString("nama_barang")); 
                item.setKategori(rs.getString("kategori_barang")); 
                item.setHarga(rs.getInt("harga_sewa")); 
                item.setStok(rs.getInt("stok")); 
                listBarang.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error tampil data: " + e.getMessage());
        }
        return listBarang;
    }

    @Override
    public void simpanBarang(Item item) {
        // MENGGUNAKAN INSERT INTO UNTUK MENAMBAH BARU
        // Nama tabel dan kolom disesuaikan dengan database kamu
        String sql = "INSERT INTO items (nama_barang, kategori_barang, harga_sewa, stok) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getNama());
            ps.setString(2, item.getKategori());
            ps.setInt(3, item.getHarga());
            ps.setInt(4, item.getStok());
            
            ps.executeUpdate(); 
        } catch (SQLException e) {
            System.err.println("Error simpan data: " + e.getMessage());
        }
    }

    @Override
    public void ubahBarang(Item item) {
        String sql = "UPDATE items SET nama_barang = ?, kategori_barang = ?, harga_sewa = ?, stok = ? WHERE id_barang = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getNama());
            ps.setString(2, item.getKategori());
            ps.setInt(3, item.getHarga());
            ps.setInt(4, item.getStok());
            ps.setInt(5, item.getId()); 
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error ubah data: " + e.getMessage());
        }
    }

    @Override
    public void hapusBarang(int id) {
        String sql = "DELETE FROM items WHERE id_barang = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error hapus data: " + e.getMessage());
        }
    }
}