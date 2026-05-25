package modules.item.dao;

import java.util.List;
import modules.item.models.Item;

public interface ItemDAO {
    List<Item> tampilkanSemuaBarang();
    void simpanBarang(Item item);
    void ubahBarang(Item item);
    void hapusBarang(int id);
}