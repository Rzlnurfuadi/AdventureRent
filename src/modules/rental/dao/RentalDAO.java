package modules.rental.dao;

import modules.rental.models.Rental;
import modules.rental.models.RentalDetail;
import java.util.List;

/**
 * Interface DAO (Data Access Object) untuk modul Rental.
 * Mendefinisikan kontrak CRUD yang harus diimplementasikan.
 *
 * @author Modul Rental
 */
public interface RentalDAO {

    // ======================= RENTAL CRUD =======================

    /**
     * Tambah transaksi rental baru ke database.
     * Otomatis insert ke tabel rentals DAN rental_details.
     * Otomatis kurangi stok barang yang dipinjam.
     *
     * @param rental objek Rental yang sudah diisi detail-nya
     * @return true jika berhasil, false jika gagal
     */
    boolean tambahRental(Rental rental);

    /**
     * Ambil semua data rental dengan nama customer (JOIN).
     *
     * @return list semua rental
     */
    List<Rental> getAllRental();

    /**
     * Ambil data rental berdasarkan ID.
     *
     * @param idRental ID rental yang dicari
     * @return objek Rental, atau null jika tidak ditemukan
     */
    Rental getRentalById(int idRental);

    /**
     * Update status rental (misal: "Dipinjam" → "Selesai").
     *
     * @param idRental ID rental yang akan diupdate
     * @param statusBaru status baru
     * @return true jika berhasil
     */
    boolean updateStatusRental(int idRental, String statusBaru);

    /**
     * Hapus rental dari database.
     * Karena ada ON DELETE CASCADE, rental_details ikut terhapus.
     * Stok barang dikembalikan sebelum dihapus.
     *
     * @param idRental ID rental yang akan dihapus
     * @return true jika berhasil
     */
    boolean hapusRental(int idRental);

    // ======================= RENTAL DETAIL =======================

    /**
     * Ambil semua detail barang untuk 1 rental.
     *
     * @param idRental ID rental
     * @return list RentalDetail
     */
    List<RentalDetail> getDetailByRentalId(int idRental);

    // ======================= HELPER (untuk ComboBox) =======================

    /**
     * Ambil semua customer dalam format [id, nama] untuk ComboBox.
     *
     * @return 2D array: [[id_customer, nama_lengkap], ...]
     */
    Object[][] getAllCustomers();

    /**
     * Ambil semua barang yang stoknya > 0 dalam format [id, nama, harga, stok].
     *
     * @return 2D array: [[id_barang, nama_barang, harga_sewa, stok], ...]
     */
    Object[][] getAvailableItems();
}
