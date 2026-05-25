package modules.item.services;

import java.util.List;
import modules.item.models.Item;
import modules.item.dao.ItemDAO;
import modules.item.dao.ItemDAOImpl;

public class ItemService {
    
    // Pastikan objek DAO-nya ada (misalnya itemDAO)
    private ItemDAO itemDAO = new ItemDAOImpl(); 

    // Untuk Edit (Update)
    public void ubahBarang(Item item) {
        itemDAO.ubahBarang(item);
    }

    // Untuk Tambah (Insert)
    public void simpanBarang(Item item) {
        itemDAO.simpanBarang(item);
    }
    
    // Constructor untuk menginisialisasi DAO
    public ItemService() {
        this.itemDAO = new ItemDAOImpl();
    }

    // Meneruskan perintah ke DAO
    public List<Item> tampilkanSemuaBarang() {
        return itemDAO.tampilkanSemuaBarang();
    }

    public void hapusBarang(int id) {
        itemDAO.hapusBarang(id);
    }
}