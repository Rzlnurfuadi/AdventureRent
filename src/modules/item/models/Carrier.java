package modules.item.models;

public class Carrier extends Item {
    public Carrier(int id, String nama, int harga, int stok) {
        super(id, nama, "Carrier", harga, stok);
    }
}