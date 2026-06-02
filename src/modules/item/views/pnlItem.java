package modules.item.views;

import java.awt.Window;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modules.item.models.Item;
import modules.item.services.ItemService;

/**
 * Panel Item untuk ditampilkan di mainMenu.
 */
public class pnlItem extends javax.swing.JPanel implements ItemTableRefreshable {

    public pnlItem() {
        initComponents();
        loadDataToTable();
        tblBarang.setSelectionBackground(new java.awt.Color(249, 237, 213));
        tblBarang.setSelectionForeground(new java.awt.Color(0, 0, 0));
    }

    private void styleTable(javax.swing.JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new java.awt.Color(230, 230, 230));
        table.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        table.setSelectionBackground(new java.awt.Color(204, 255, 204));
        table.setSelectionForeground(new java.awt.Color(0, 0, 0));

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(new java.awt.Color(21, 100, 60));
        header.setForeground(java.awt.Color.BLACK);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        header.setPreferredSize(new java.awt.Dimension(header.getWidth(), 35));
        ((javax.swing.table.DefaultTableCellRenderer) header.getDefaultRenderer())
                .setHorizontalAlignment(javax.swing.JLabel.CENTER);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        for (int x = 0; x < table.getColumnCount(); x++) {
            table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }
    }

    @Override
    public void loadDataToTable() {
        styleTable(tblBarang);
        DefaultTableModel model = (DefaultTableModel) tblBarang.getModel();
        model.setColumnIdentifiers(new String[]{"ID", "Nama Barang", "Kategori", "Harga", "Stok"});
        model.setRowCount(0);

        ItemService service = new ItemService();
        for (Item item : service.tampilkanSemuaBarang()) {
            model.addRow(new Object[]{
                item.getId(),
                item.getNama(),
                item.getKategori(),
                item.getHarga(),
                item.getStok()
            });
        }
    }

    private Window getOwnerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jPanel2.setBackground(new java.awt.Color(115, 147, 126));

        tblBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {},
            new String[] {"Title 1", "Title 2", "Title 3", "Title 4"}
        ));
        jScrollPane1.setViewportView(tblBarang);

        btnTambah.setBackground(new java.awt.Color(206, 185, 146));
        btnTambah.setText("Tambah Barang");
        btnTambah.addActionListener(this::btnTambahActionPerformed);

        btnEdit.setBackground(new java.awt.Color(206, 185, 146));
        btnEdit.setText("Edit Barang");
        btnEdit.addActionListener(this::btnEditActionPerformed);

        btnHapus.setBackground(new java.awt.Color(206, 185, 146));
        btnHapus.setText("Hapus Barang");
        btnHapus.addActionListener(this::btnHapusActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabel1.setForeground(new java.awt.Color(248, 255, 252));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Barang Barang Camping");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnTambah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnHapus)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        FormItemView popUp = new FormItemView(getOwnerWindow(), this);
        popUp.setVisible(true);
    }

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
        int barisTerpilih = tblBarang.getSelectedRow();
        if (barisTerpilih == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih barang yang ingin diedit terlebih dahulu!");
            return;
        }

        int id = Integer.parseInt(tblBarang.getValueAt(barisTerpilih, 0).toString());
        String nama = tblBarang.getValueAt(barisTerpilih, 1).toString();
        String kategori = tblBarang.getValueAt(barisTerpilih, 2).toString();
        int harga = Integer.parseInt(tblBarang.getValueAt(barisTerpilih, 3).toString());
        int stok = Integer.parseInt(tblBarang.getValueAt(barisTerpilih, 4).toString());

        FormItemView formEdit = new FormItemView(getOwnerWindow(), this, id, nama, kategori, harga, stok);
        formEdit.setVisible(true);
    }

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        int barisDipilih = tblBarang.getSelectedRow();
        if (barisDipilih == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih data di tabel terlebih dahulu yang ingin dihapus!");
            return;
        }

        int id = Integer.parseInt(tblBarang.getValueAt(barisDipilih, 0).toString());
        String nama = tblBarang.getValueAt(barisDipilih, 1).toString();

        int konfirmasi = javax.swing.JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus barang: " + nama + "?",
                "Konfirmasi Hapus",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);

        if (konfirmasi == javax.swing.JOptionPane.YES_OPTION) {
            new ItemService().hapusBarang(id);
            javax.swing.JOptionPane.showMessageDialog(this, "Data barang berhasil dihapus!");
            loadDataToTable();
        }
    }

    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnTambah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblBarang;
}
