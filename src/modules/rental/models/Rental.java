package modules.rental.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Model class untuk data Rental (Peminjaman).
 * Merepresentasikan 1 transaksi peminjaman alat camping.
 *
 * @author Modul Rental
 */
public class Rental {

    private int idRental;
    private int idCustomer;
    private String namaCustomer; // joined dari tabel customers
    private Date tglPinjam;
    private String statusRental;
    private int totalHarga;
    private List<RentalDetail> details;

    // ======================= CONSTRUCTOR =======================

    public Rental() {
        this.details = new ArrayList<>();
        this.statusRental = "Dipinjam";
        this.totalHarga = 0;
    }

    public Rental(int idCustomer, Date tglPinjam) {
        this();
        this.idCustomer = idCustomer;
        this.tglPinjam = tglPinjam;
    }

    public Rental(int idRental, int idCustomer, String namaCustomer,
                  Date tglPinjam, String statusRental, int totalHarga) {
        this.idRental = idRental;
        this.idCustomer = idCustomer;
        this.namaCustomer = namaCustomer;
        this.tglPinjam = tglPinjam;
        this.statusRental = statusRental;
        this.totalHarga = totalHarga;
        this.details = new ArrayList<>();
    }

    // ======================= GETTERS & SETTERS =======================

    public int getIdRental() {
        return idRental;
    }

    public void setIdRental(int idRental) {
        this.idRental = idRental;
    }

    public int getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(int idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getNamaCustomer() {
        return namaCustomer;
    }

    public void setNamaCustomer(String namaCustomer) {
        this.namaCustomer = namaCustomer;
    }

    public Date getTglPinjam() {
        return tglPinjam;
    }

    public void setTglPinjam(Date tglPinjam) {
        this.tglPinjam = tglPinjam;
    }

    public String getStatusRental() {
        return statusRental;
    }

    public void setStatusRental(String statusRental) {
        this.statusRental = statusRental;
    }

    public int getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(int totalHarga) {
        this.totalHarga = totalHarga;
    }

    public List<RentalDetail> getDetails() {
        return details;
    }

    public void setDetails(List<RentalDetail> details) {
        this.details = details;
    }

    public void addDetail(RentalDetail detail) {
        this.details.add(detail);
    }

    // ======================= UTILITY =======================

    /**
     * Hitung total harga dari semua detail barang.
     * totalHarga = SUM(harga_sewa * jumlah_pinjam) per detail
     */
    public void hitungTotalHarga() {
        int total = 0;
        for (RentalDetail d : details) {
            total += d.getSubtotal();
        }
        this.totalHarga = total;
    }

    @Override
    public String toString() {
        return "Rental{id=" + idRental + ", customer=" + namaCustomer
                + ", tgl=" + tglPinjam + ", status=" + statusRental
                + ", total=" + totalHarga + "}";
    }
}
