package modules.rental.services;

import modules.rental.dao.RentalDAO;
import modules.rental.dao.RentalDAOImpl;
import modules.rental.models.Rental;
import modules.rental.models.RentalDetail;

import java.util.Date;
import java.util.List;

/**
 * Service layer untuk modul Rental.
 * Berisi business logic dan validasi sebelum operasi ke database.
 * View hanya boleh memanggil Service, tidak boleh langsung ke DAO.
 *
 * @author Modul Rental
 */
public class RentalService {

    private final RentalDAO rentalDAO;

    // Status yang valid untuk rental
    public static final String STATUS_DIPINJAM = "Dipinjam";
    public static final String STATUS_SELESAI  = "Selesai";
    public static final String STATUS_DIBATALKAN = "Dibatalkan";

    public RentalService() {
        this.rentalDAO = new RentalDAOImpl();
    }

    // ============================================================
    //  CREATE
    // ============================================================

    /**
     * Buat transaksi rental baru.
     *
     * @param idCustomer ID customer yang menyewa
     * @param tglPinjam tanggal pinjam
     * @param details list detail barang yang dipinjam
     * @return pesan hasil: "SUCCESS" atau pesan error
     */
    public String tambahRental(int idCustomer, Date tglPinjam, List<RentalDetail> details) {
        // Validasi
        if (idCustomer <= 0) {
            return "Pilih customer terlebih dahulu!";
        }
        if (tglPinjam == null) {
            return "Tanggal pinjam tidak boleh kosong!";
        }
        if (details == null || details.isEmpty()) {
            return "Pilih minimal 1 barang untuk dipinjam!";
        }
        for (RentalDetail d : details) {
            if (d.getJumlahPinjam() <= 0) {
                return "Jumlah pinjam untuk " + d.getNamaBarang() + " harus lebih dari 0!";
            }
        }

        // Buat objek Rental
        Rental rental = new Rental(idCustomer, tglPinjam);
        rental.setStatusRental(STATUS_DIPINJAM);
        for (RentalDetail d : details) {
            rental.addDetail(d);
        }
        rental.hitungTotalHarga();

        // Simpan ke database
        boolean berhasil = rentalDAO.tambahRental(rental);
        if (berhasil) {
            return "SUCCESS";
        } else {
            return "Gagal menambah rental! Cek stok barang atau koneksi database.";
        }
    }

    // ============================================================
    //  READ
    // ============================================================

    /**
     * Ambil semua data rental.
     */
    public List<Rental> getAllRental() {
        return rentalDAO.getAllRental();
    }

    /**
     * Ambil data rental beserta detailnya berdasarkan ID.
     */
    public Rental getRentalById(int idRental) {
        if (idRental <= 0) return null;
        return rentalDAO.getRentalById(idRental);
    }

    /**
     * Ambil detail barang untuk 1 rental.
     */
    public List<RentalDetail> getDetailByRentalId(int idRental) {
        if (idRental <= 0) return List.of();
        return rentalDAO.getDetailByRentalId(idRental);
    }

    // ============================================================
    //  UPDATE
    // ============================================================

    /**
     * Update status rental.
     *
     * @param idRental ID rental yang diupdate
     * @param statusBaru status baru ("Dipinjam", "Selesai", "Dibatalkan")
     * @return pesan hasil: "SUCCESS" atau pesan error
     */
    public String updateStatusRental(int idRental, String statusBaru) {
        if (idRental <= 0) {
            return "Pilih rental yang akan diupdate!";
        }
        if (statusBaru == null || statusBaru.trim().isEmpty()) {
            return "Status tidak boleh kosong!";
        }
        if (!statusBaru.equals(STATUS_DIPINJAM)
                && !statusBaru.equals(STATUS_SELESAI)
                && !statusBaru.equals(STATUS_DIBATALKAN)) {
            return "Status tidak valid! Pilih: Dipinjam, Selesai, atau Dibatalkan.";
        }

        // Cek rental ada
        Rental existing = rentalDAO.getRentalById(idRental);
        if (existing == null) {
            return "Rental dengan ID " + idRental + " tidak ditemukan!";
        }
        if (existing.getStatusRental().equals(statusBaru)) {
            return "Status sudah " + statusBaru + ", tidak ada perubahan.";
        }

        boolean berhasil = rentalDAO.updateStatusRental(idRental, statusBaru);
        return berhasil ? "SUCCESS" : "Gagal mengupdate status rental!";
    }

    // ============================================================
    //  DELETE
    // ============================================================

    /**
     * Hapus rental (stok barang otomatis dikembalikan).
     *
     * @param idRental ID rental yang akan dihapus
     * @return pesan hasil: "SUCCESS" atau pesan error
     */
    public String hapusRental(int idRental) {
        if (idRental <= 0) {
            return "Pilih rental yang akan dihapus!";
        }

        Rental existing = rentalDAO.getRentalById(idRental);
        if (existing == null) {
            return "Rental dengan ID " + idRental + " tidak ditemukan!";
        }

        boolean berhasil = rentalDAO.hapusRental(idRental);
        return berhasil ? "SUCCESS" : "Gagal menghapus rental!";
    }

    // ============================================================
    //  HELPER
    // ============================================================

    /**
     * Ambil data customers untuk ComboBox.
     * Format: [[id_customer, nama_lengkap], ...]
     */
    public Object[][] getAllCustomers() {
        return rentalDAO.getAllCustomers();
    }

    /**
     * Ambil data barang tersedia untuk ComboBox.
     * Format: [[id_barang, nama_barang, harga_sewa, stok], ...]
     */
    public Object[][] getAvailableItems() {
        return rentalDAO.getAvailableItems();
    }

    /**
     * Format angka ke format Rupiah.
     * Contoh: 65000 → "Rp 65.000"
     */
    public static String formatRupiah(int nominal) {
        return String.format("Rp %,d", nominal).replace(",", ".");
    }
}
