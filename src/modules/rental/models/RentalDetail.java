package modules.rental.models;

/**
 * Model class untuk detail barang dalam 1 transaksi Rental.
 * Merepresentasikan baris di tabel rental_details.
 *
 * @author Modul Rental
 */
public class RentalDetail {

    private int idDetail;
    private int idRental;
    private int idBarang;
    private String namaBarang;   // joined dari tabel items
    private int hargaSewa;       // joined dari tabel items
    private int jumlahPinjam;

    // ======================= CONSTRUCTOR =======================

    public RentalDetail() {}

    public RentalDetail(int idRental, int idBarang, int jumlahPinjam) {
        this.idRental = idRental;
        this.idBarang = idBarang;
        this.jumlahPinjam = jumlahPinjam;
    }

    public RentalDetail(int idDetail, int idRental, int idBarang,
                        String namaBarang, int hargaSewa, int jumlahPinjam) {
        this.idDetail = idDetail;
        this.idRental = idRental;
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.hargaSewa = hargaSewa;
        this.jumlahPinjam = jumlahPinjam;
    }

    // ======================= GETTERS & SETTERS =======================

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public int getIdRental() {
        return idRental;
    }

    public void setIdRental(int idRental) {
        this.idRental = idRental;
    }

    public int getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(int idBarang) {
        this.idBarang = idBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public int getHargaSewa() {
        return hargaSewa;
    }

    public void setHargaSewa(int hargaSewa) {
        this.hargaSewa = hargaSewa;
    }

    public int getJumlahPinjam() {
        return jumlahPinjam;
    }

    public void setJumlahPinjam(int jumlahPinjam) {
        this.jumlahPinjam = jumlahPinjam;
    }

    /**
     * Subtotal = harga_sewa * jumlah_pinjam
     */
    public int getSubtotal() {
        return hargaSewa * jumlahPinjam;
    }

    @Override
    public String toString() {
        return namaBarang + " x" + jumlahPinjam + " (@Rp" + hargaSewa + ")";
    }
}
