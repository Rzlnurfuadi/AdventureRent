package modules.item.views;

/**
 * Callback untuk me-refresh tabel barang setelah tambah/edit/hapus.
 */
public interface ItemTableRefreshable {
    void loadDataToTable();
}
