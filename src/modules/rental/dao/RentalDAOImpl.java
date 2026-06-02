package modules.rental.dao;

import config.DatabaseConfig;
import modules.rental.models.Rental;
import modules.rental.models.RentalDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi RentalDAO.
 * Berisi semua query SQL untuk operasi CRUD Rental ke database MySQL.
 *
 * @author Modul Rental
 */
public class RentalDAOImpl implements RentalDAO {

    private Connection getConn() {
        return DatabaseConfig.getConnection();
    }

    // ============================================================
    //  CREATE — Tambah Rental baru
    // ============================================================

    @Override
    public boolean tambahRental(Rental rental) {
        Connection conn = getConn();
        if (conn == null) return false;

        // Query insert ke tabel rentals
        String sqlRental = "INSERT INTO rentals (id_customer, tgl_pinjam, status_rental, total_harga) "
                         + "VALUES (?, ?, ?, ?)";
        // Query insert ke tabel rental_details
        String sqlDetail = "INSERT INTO rental_details (id_rental, id_barang, jumlah_pinjam) "
                         + "VALUES (?, ?, ?)";
        // Query kurangi stok barang
        String sqlKurangiStok = "UPDATE items SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";

        try {
            // Matikan auto-commit agar bisa rollback jika gagal
            conn.setAutoCommit(false);

            // 1. Hitung total harga dulu
            rental.hitungTotalHarga();

            // 2. Insert ke tabel rentals, ambil generated key (id_rental)
            PreparedStatement psRental = conn.prepareStatement(sqlRental, Statement.RETURN_GENERATED_KEYS);
            psRental.setInt(1, rental.getIdCustomer());
            psRental.setDate(2, new java.sql.Date(rental.getTglPinjam().getTime()));
            psRental.setString(3, rental.getStatusRental());
            psRental.setInt(4, rental.getTotalHarga());
            int rowsRental = psRental.executeUpdate();

            if (rowsRental == 0) {
                conn.rollback();
                return false;
            }

            // Ambil id_rental yang baru dibuat
            ResultSet generatedKeys = psRental.getGeneratedKeys();
            int idRentalBaru = 0;
            if (generatedKeys.next()) {
                idRentalBaru = generatedKeys.getInt(1);
                rental.setIdRental(idRentalBaru);
            }
            psRental.close();

            // 3. Insert setiap detail barang dan kurangi stok
            for (RentalDetail detail : rental.getDetails()) {
                // Cek dan kurangi stok
                PreparedStatement psStok = conn.prepareStatement(sqlKurangiStok);
                psStok.setInt(1, detail.getJumlahPinjam());
                psStok.setInt(2, detail.getIdBarang());
                psStok.setInt(3, detail.getJumlahPinjam());
                int rowsStok = psStok.executeUpdate();
                psStok.close();

                if (rowsStok == 0) {
                    // Stok tidak cukup — rollback semua!
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }

                // Insert detail rental
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                psDetail.setInt(1, idRentalBaru);
                psDetail.setInt(2, detail.getIdBarang());
                psDetail.setInt(3, detail.getJumlahPinjam());
                psDetail.executeUpdate();
                psDetail.close();
            }

            // 4. Commit semua perubahan
            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("Error tambahRental: " + e.getMessage());
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    // ============================================================
    //  READ — Ambil semua data Rental
    // ============================================================

    @Override
    public List<Rental> getAllRental() {
        List<Rental> list = new ArrayList<>();
        // JOIN dengan customers agar nama customer tampil
        String sql = "SELECT r.id_rental, r.id_customer, c.nama_lengkap, "
                   + "r.tgl_pinjam, r.status_rental, r.total_harga "
                   + "FROM rentals r "
                   + "JOIN customers c ON r.id_customer = c.id_customer "
                   + "ORDER BY r.id_rental DESC";

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Rental rental = new Rental(
                    rs.getInt("id_rental"),
                    rs.getInt("id_customer"),
                    rs.getString("nama_lengkap"),
                    rs.getDate("tgl_pinjam"),
                    rs.getString("status_rental"),
                    rs.getInt("total_harga")
                );
                list.add(rental);
            }

        } catch (SQLException e) {
            System.err.println("Error getAllRental: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    //  READ — Ambil Rental berdasarkan ID
    // ============================================================

    @Override
    public Rental getRentalById(int idRental) {
        String sql = "SELECT r.id_rental, r.id_customer, c.nama_lengkap, "
                   + "r.tgl_pinjam, r.status_rental, r.total_harga "
                   + "FROM rentals r "
                   + "JOIN customers c ON r.id_customer = c.id_customer "
                   + "WHERE r.id_rental = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idRental);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Rental rental = new Rental(
                    rs.getInt("id_rental"),
                    rs.getInt("id_customer"),
                    rs.getString("nama_lengkap"),
                    rs.getDate("tgl_pinjam"),
                    rs.getString("status_rental"),
                    rs.getInt("total_harga")
                );
                // Ambil juga detailnya
                rental.setDetails(getDetailByRentalId(idRental));
                return rental;
            }

        } catch (SQLException e) {
            System.err.println("Error getRentalById: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    //  UPDATE — Ubah status rental
    // ============================================================

    @Override
    public boolean updateStatusRental(int idRental, String statusBaru) {
        String sql = "UPDATE rentals SET status_rental = ? WHERE id_rental = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statusBaru);
            ps.setInt(2, idRental);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updateStatusRental: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    //  DELETE — Hapus rental (stok dikembalikan dulu)
    // ============================================================

    @Override
    public boolean hapusRental(int idRental) {
        Connection conn = getConn();
        if (conn == null) return false;

        // Kembalikan stok barang sebelum hapus
        String sqlKembalikanStok = "UPDATE items SET stok = stok + rd.jumlah_pinjam "
                                 + "FROM rental_details rd "
                                 + "WHERE items.id_barang = rd.id_barang AND rd.id_rental = ?";
        // Versi MySQL:
        String sqlKembalikanStokMySQL = "UPDATE items i "
                                      + "JOIN rental_details rd ON i.id_barang = rd.id_barang "
                                      + "SET i.stok = i.stok + rd.jumlah_pinjam "
                                      + "WHERE rd.id_rental = ?";
        String sqlHapus = "DELETE FROM rentals WHERE id_rental = ?";

        try {
            conn.setAutoCommit(false);

            // 1. Kembalikan stok barang
            PreparedStatement psStok = conn.prepareStatement(sqlKembalikanStokMySQL);
            psStok.setInt(1, idRental);
            psStok.executeUpdate();
            psStok.close();

            // 2. Hapus rental (rental_details ikut terhapus via CASCADE)
            PreparedStatement psHapus = conn.prepareStatement(sqlHapus);
            psHapus.setInt(1, idRental);
            int rows = psHapus.executeUpdate();
            psHapus.close();

            conn.commit();
            conn.setAutoCommit(true);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error hapusRental: " + e.getMessage());
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    // ============================================================
    //  READ DETAIL — Ambil detail barang untuk 1 rental
    // ============================================================

    @Override
    public List<RentalDetail> getDetailByRentalId(int idRental) {
        List<RentalDetail> list = new ArrayList<>();
        String sql = "SELECT rd.id_detail, rd.id_rental, rd.id_barang, "
                   + "i.nama_barang, i.harga_sewa, rd.jumlah_pinjam "
                   + "FROM rental_details rd "
                   + "JOIN items i ON rd.id_barang = i.id_barang "
                   + "WHERE rd.id_rental = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idRental);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RentalDetail detail = new RentalDetail(
                    rs.getInt("id_detail"),
                    rs.getInt("id_rental"),
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getInt("harga_sewa"),
                    rs.getInt("jumlah_pinjam")
                );
                list.add(detail);
            }

        } catch (SQLException e) {
            System.err.println("Error getDetailByRentalId: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    //  HELPER — Data untuk ComboBox
    // ============================================================

    @Override
    public Object[][] getAllCustomers() {
        String sql = "SELECT id_customer, nama_lengkap FROM customers ORDER BY nama_lengkap";
        List<Object[]> rows = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{rs.getInt("id_customer"), rs.getString("nama_lengkap")});
            }

        } catch (SQLException e) {
            System.err.println("Error getAllCustomers: " + e.getMessage());
        }
        return rows.toArray(new Object[0][]);
    }

    @Override
    public Object[][] getAvailableItems() {
        String sql = "SELECT id_barang, nama_barang, harga_sewa, stok "
                   + "FROM items WHERE stok > 0 ORDER BY nama_barang";
        List<Object[]> rows = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getInt("harga_sewa"),
                    rs.getInt("stok")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error getAvailableItems: " + e.getMessage());
        }
        return rows.toArray(new Object[0][]);
    }
}
