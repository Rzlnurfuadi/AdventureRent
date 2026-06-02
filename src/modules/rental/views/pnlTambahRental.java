/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package modules.rental.views;

// Importan
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ASUS
 */
public class pnlTambahRental extends javax.swing.JPanel {
    
    // Variabel Global
    // --- VARIABEL GLOBAL UNTUK PENAMPUNG ID ---
    private DefaultTableModel modelPilihBarang;
    private DefaultTableModel modelKeranjang;
    private ArrayList<Integer> listIdCustomer = new ArrayList<>();
    private ArrayList<Integer> listIdBarangKeranjang = new ArrayList<>();
    private int grandTotal = 0;

    /**
     * Creates new form pnlTambahRental
     */
    public pnlTambahRental() {
        initComponents();
        
        loadCustomer();
        loadBarang();
        setupKeranjang();
    }
    
    private void styleTable(javax.swing.JTable table) {
    // 1. Styling Baris dan Grid (Tampilan Clean/Flat)
    table.setRowHeight(35); // Bikin baris lebih tinggi agar tidak sempit
    table.setShowVerticalLines(false); // Hilangkan garis vertikal
    table.setShowHorizontalLines(true); // Biarkan garis horizontal sebagai pemisah
    table.setGridColor(new java.awt.Color(230, 230, 230)); // Warna garis horizontal abu-abu tipis
    table.setBorder(javax.swing.BorderFactory.createEmptyBorder());
    
    // 2. Warna saat baris diklik (Hijau Muda)
    table.setSelectionBackground(new java.awt.Color(204, 255, 204)); 
    table.setSelectionForeground(new java.awt.Color(0, 0, 0)); // Teks tetap hitam saat dipilih

    // 3. Styling Header (Warna hijau gelap sesuai tema UI-mu)
    javax.swing.table.JTableHeader header = table.getTableHeader();
    header.setBackground(new java.awt.Color(21, 100, 60)); // Background hijau
    header.setForeground(java.awt.Color.BLACK); // Teks putih
    header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13)); // Font modern
    
    // Bikin header lebih tinggi sedikit
    header.setPreferredSize(new java.awt.Dimension(header.getWidth(), 35));
    
    // Rata tengah untuk teks header
    ((javax.swing.table.DefaultTableCellRenderer)header.getDefaultRenderer())
            .setHorizontalAlignment(javax.swing.JLabel.CENTER);
            
    // Opsional: Membuat isi sel tabel rata tengah
    javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment( javax.swing.JLabel.CENTER );
    for(int x=0; x < table.getColumnCount(); x++){
         table.getColumnModel().getColumn(x).setCellRenderer( centerRenderer );
    }
}
    
    // --- INNER CLASS UNTUK MERENDER TOMBOL DI TABEL ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Edit QTY" : value.toString());
            return this;
        }
    }

    // --- INNER CLASS UNTUK MENANGANI KLIK TOMBOL DI TABEL ---
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            label = (value == null) ? "Edit QTY" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                editQty(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    // --- METHOD KONEKSI ---
    private Connection getKoneksi() {
        try {
            String url = "jdbc:mysql://localhost:3306/db_rental_camping"; 
            String user = "root";
            String pass = "";
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi Database Gagal: " + e.getMessage());
            return null;
        }
    }

    // --- METHOD LOAD CUSTOMER ---
    private void loadCustomer() {
        cmbCustomer.removeAllItems();
        listIdCustomer.clear();
        try {
            Connection conn = getKoneksi();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_customer, nama_lengkap FROM customers");
            while (rs.next()) {
                cmbCustomer.addItem(rs.getString("nama_lengkap"));
                listIdCustomer.add(rs.getInt("id_customer")); 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load Customer: " + e.getMessage());
        }
    }

    // --- METHOD LOAD BARANG ---
    private void loadBarang() {
        modelPilihBarang = new DefaultTableModel();
        modelPilihBarang.addColumn("ID Barang");
        modelPilihBarang.addColumn("Nama Barang");
        modelPilihBarang.addColumn("Harga Sewa (satuan)");
        modelPilihBarang.addColumn("Stok Gudang");
        tblPilihBarang.setModel(modelPilihBarang);
        styleTable(tblPilihBarang);

        try {
            Connection conn = getKoneksi();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM items WHERE stok > 0"); 
            while (rs.next()) {
                modelPilihBarang.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("harga_sewa"),
                    rs.getString("stok")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load Barang: " + e.getMessage());
        }
    }

    // --- METHOD SETUP KERANJANG ---
    private void setupKeranjang() {
        modelKeranjang = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };
        
        modelKeranjang.addColumn("Nama Barang");
        modelKeranjang.addColumn("Harga");
        modelKeranjang.addColumn("Qty");
        modelKeranjang.addColumn("Subtotal");
        modelKeranjang.addColumn("Action"); 
        
        tblKeranjang.setModel(modelKeranjang);
        styleTable(tblKeranjang);
        
        tblKeranjang.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tblKeranjang.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    // --- METHOD HITUNG TOTAL ---
    private void hitungGrandTotal() {
        grandTotal = 0;
        for (int i = 0; i < tblKeranjang.getRowCount(); i++) {
            grandTotal += Integer.parseInt(tblKeranjang.getValueAt(i, 3).toString());
        }
        lblTotalHarga.setText(String.valueOf(grandTotal));
    }
    
    // --- METHOD EDIT QTY ---
    private void editQty(int row) {
        if (row < 0 || row >= tblKeranjang.getRowCount()) return;

        int idBarang = listIdBarangKeranjang.get(row);
        String namaBarang = tblKeranjang.getValueAt(row, 0).toString();
        int harga = Integer.parseInt(tblKeranjang.getValueAt(row, 1).toString());
        int qtyLama = Integer.parseInt(tblKeranjang.getValueAt(row, 2).toString());

        int maxStok = 0;
        for (int i = 0; i < tblPilihBarang.getRowCount(); i++) {
            if (Integer.parseInt(tblPilihBarang.getValueAt(i, 0).toString()) == idBarang) {
                maxStok = Integer.parseInt(tblPilihBarang.getValueAt(i, 3).toString());
                break;
            }
        }

        String qtyStr = JOptionPane.showInputDialog(this, "Ubah Qty untuk " + namaBarang + " (Maks: " + maxStok + "):", qtyLama);

        if (qtyStr != null && !qtyStr.isEmpty()) {
            try {
                int qtyBaru = Integer.parseInt(qtyStr);
                
                if (qtyBaru <= 0) {
                    JOptionPane.showMessageDialog(this, "Qty tidak valid! Gunakan tombol 'Hapus dari Keranjang' jika ingin membatalkan item.");
                } else if (qtyBaru > maxStok) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Sisa stok di gudang: " + maxStok);
                } else {
                    int subtotalBaru = harga * qtyBaru;
                    tblKeranjang.setValueAt(qtyBaru, row, 2);
                    tblKeranjang.setValueAt(subtotalBaru, row, 3);
                    hitungGrandTotal();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Input harus berupa angka!");
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        tableContainer = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbCustomer = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        spnTanggal = new javax.swing.JSpinner();
        scrPilihBarang = new javax.swing.JScrollPane();
        tblPilihBarang = new javax.swing.JTable();
        btnTambahKeranjang = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        scrKeranjang = new javax.swing.JScrollPane();
        tblKeranjang = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblTotalHarga = new javax.swing.JLabel();
        btnHapusKeranjang = new javax.swing.JButton();
        btnSimpanRental = new javax.swing.JButton();

        tableContainer.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBackground(new java.awt.Color(248, 255, 252));

        jLabel2.setBackground(new java.awt.Color(0, 153, 0));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 0));
        jLabel2.setText("Tambah Transaksi Rental");

        jLabel3.setBackground(new java.awt.Color(0, 153, 0));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 51, 51));
        jLabel3.setText("Nama Customer:");

        cmbCustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setBackground(new java.awt.Color(0, 153, 0));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("Tgl. Rental (mulai)");

        spnTanggal.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.DAY_OF_MONTH));

        tblPilihBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID Barang", "Nama Barang", "Haga Sewa (satuan)", "Stok Gudang"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrPilihBarang.setViewportView(tblPilihBarang);

        btnTambahKeranjang.setBackground(new java.awt.Color(0, 153, 0));
        btnTambahKeranjang.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahKeranjang.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahKeranjang.setText("Tambah ke Keranjang");
        btnTambahKeranjang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnTambahKeranjangMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTambahKeranjangMouseEntered(evt);
            }
        });
        btnTambahKeranjang.addActionListener(this::btnTambahKeranjangActionPerformed);

        jLabel5.setBackground(new java.awt.Color(0, 153, 0));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 153, 0));
        jLabel5.setText("List Barang Tersedia");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnTambahKeranjang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(scrPilihBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel5))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(cmbCustomer, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(spnTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spnTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnTambahKeranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrPilihBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBackground(new java.awt.Color(248, 255, 252));

        jLabel6.setBackground(new java.awt.Color(0, 153, 0));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 153, 0));
        jLabel6.setText("Keranjang Rental");

        tblKeranjang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama Barang", "Harga", "Qty", "Subtotal", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKeranjang.setOpaque(false);
        scrKeranjang.setViewportView(tblKeranjang);

        jLabel7.setBackground(new java.awt.Color(0, 153, 0));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 153, 0));
        jLabel7.setText("Grand Total:");

        jLabel8.setBackground(new java.awt.Color(0, 153, 0));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 153, 0));
        jLabel8.setText("Rp");

        lblTotalHarga.setBackground(new java.awt.Color(0, 153, 0));
        lblTotalHarga.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalHarga.setForeground(new java.awt.Color(0, 153, 0));
        lblTotalHarga.setText("0");

        btnHapusKeranjang.setBackground(new java.awt.Color(255, 51, 51));
        btnHapusKeranjang.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapusKeranjang.setForeground(new java.awt.Color(255, 255, 255));
        btnHapusKeranjang.setText("Hapus dari Keranjang");
        btnHapusKeranjang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnHapusKeranjangMouseClicked(evt);
            }
        });
        btnHapusKeranjang.addActionListener(this::btnHapusKeranjangActionPerformed);

        btnSimpanRental.setBackground(new java.awt.Color(0, 153, 0));
        btnSimpanRental.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanRental.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanRental.setText("SIMPAN TRANSAKSI");
        btnSimpanRental.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSimpanRentalMouseClicked(evt);
            }
        });
        btnSimpanRental.addActionListener(this::btnSimpanRentalActionPerformed);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrKeranjang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnHapusKeranjang, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(29, 29, 29)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSimpanRental, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTotalHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrKeranjang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(lblTotalHarga))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHapusKeranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSimpanRental, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );

        javax.swing.GroupLayout tableContainerLayout = new javax.swing.GroupLayout(tableContainer);
        tableContainer.setLayout(tableContainerLayout);
        tableContainerLayout.setHorizontalGroup(
            tableContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableContainerLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        tableContainerLayout.setVerticalGroup(
            tableContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableContainerLayout.createSequentialGroup()
                .addGroup(tableContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahKeranjangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTambahKeranjangMouseClicked
        int row = tblPilihBarang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu dari list barang tersedia!");
            return;
        }

        // Ambil data dari tabel kiri (Barang Tersedia)
        int idBarang = Integer.parseInt(tblPilihBarang.getValueAt(row, 0).toString());
        String namaBarang = tblPilihBarang.getValueAt(row, 1).toString();
        int harga = Integer.parseInt(tblPilihBarang.getValueAt(row, 2).toString());
        int stok = Integer.parseInt(tblPilihBarang.getValueAt(row, 3).toString());

        // Minta user input jumlah barang
        String qtyStr = JOptionPane.showInputDialog(this, "Masukkan Qty untuk " + namaBarang + ":");

        if (qtyStr != null && !qtyStr.isEmpty()) {
            try {
                int qtyInput = Integer.parseInt(qtyStr);

                // Validasi 1: Qty tidak boleh minus atau nol
                if (qtyInput <= 0) {
                    JOptionPane.showMessageDialog(this, "Qty harus lebih dari 0!");
                    return;
                }

                // Cek apakah idBarang sudah ada di dalam list keranjang
                int indexKeranjang = listIdBarangKeranjang.indexOf(idBarang);

                if (indexKeranjang != -1) {
                    // --- JIKA BARANG SUDAH ADA DI KERANJANG ---

                    // Ambil Qty yang sudah ada di keranjang sebelumnya
                    int qtyLama = Integer.parseInt(tblKeranjang.getValueAt(indexKeranjang, 2).toString());
                    int qtyBaru = qtyLama + qtyInput;

                    // Validasi 2: Akumulasi Qty tidak boleh melebihi stok
                    if (qtyBaru > stok) {
                        int sisaBisaDitambah = stok - qtyLama;
                        JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!\n"
                            + "Barang ini sudah ada di keranjang sebanyak: " + qtyLama + "\n"
                            + "Sisa stok yang bisa ditambah: " + sisaBisaDitambah);
                    } else {
                        // Update Qty dan Subtotal di baris yang sudah ada
                        int subtotalBaru = harga * qtyBaru;
                        tblKeranjang.setValueAt(qtyBaru, indexKeranjang, 2); // Update kolom Qty
                        tblKeranjang.setValueAt(subtotalBaru, indexKeranjang, 3); // Update kolom Subtotal

                        hitungGrandTotal();
                    }

                } else {
                    // --- JIKA BARANG BELUM ADA DI KERANJANG (BARANG BARU) ---

                    // Validasi 3: Qty input tidak boleh melebihi stok
                    if (qtyInput > stok) {
                        JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Sisa stok: " + stok);
                    } else {
                        // Tambahkan sebagai baris baru ke tabel keranjang
                        int subtotal = harga * qtyInput;
                        // Sebelumnya: modelKeranjang.addRow(new Object[]{namaBarang, harga, qtyInput, subtotal});

                        modelKeranjang.addRow(new Object[]{namaBarang, harga, qtyInput, subtotal, "Edit QTY"});
                        listIdBarangKeranjang.add(idBarang); // Simpan ID barang ke penampung

                        hitungGrandTotal();
                    }
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Qty harus berupa angka bulat!");
            }
        }
    }//GEN-LAST:event_btnTambahKeranjangMouseClicked

    private void btnTambahKeranjangMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTambahKeranjangMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTambahKeranjangMouseEntered

    private void btnTambahKeranjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKeranjangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTambahKeranjangActionPerformed

    private void btnHapusKeranjangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnHapusKeranjangMouseClicked
        int row = tblKeranjang.getSelectedRow();

        // Cek apakah ada baris yang dipilih
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang di keranjang yang ingin dihapus terlebih dahulu!");
            return;
        }

        // Ambil nama barang untuk ditampilkan di pesan agar lebih jelas
        String namaBarang = tblKeranjang.getValueAt(row, 0).toString();

        // Memunculkan OptionPane Konfirmasi (Yes / No)
        int pilihan = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus '" + namaBarang + "' dari keranjang?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        // Jika user menekan tombol YES (Ya)
        if (pilihan == JOptionPane.YES_OPTION) {
            modelKeranjang.removeRow(row);         // Hapus dari tampilan tabel
            listIdBarangKeranjang.remove(row);     // Hapus ID dari list di belakang layar
            hitungGrandTotal();                    // Update harga total

        }
    }//GEN-LAST:event_btnHapusKeranjangMouseClicked

    private void btnHapusKeranjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusKeranjangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnHapusKeranjangActionPerformed

    private void btnSimpanRentalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSimpanRentalMouseClicked
        if (tblKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }

        try {
            Connection conn = getKoneksi();
            // Mulai transaksi database
            conn.setAutoCommit(false);

            // 1. Ambil Data Header
            int indexCustomer = cmbCustomer.getSelectedIndex();
            int idCustomer = listIdCustomer.get(indexCustomer);

            // Format tanggal dari JSpinner ke format MySQL (YYYY-MM-DD)
            java.util.Date dateFromSpinner = (java.util.Date) spnTanggal.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tglPinjam = sdf.format(dateFromSpinner);

            // 2. Insert ke tabel rentals
            String sqlRental = "INSERT INTO rentals (id_customer, tgl_pinjam, status_rental, total_harga) VALUES (?, ?, 'Dipinjam', ?)";
            PreparedStatement pstRental = conn.prepareStatement(sqlRental, Statement.RETURN_GENERATED_KEYS);
            pstRental.setInt(1, idCustomer);
            pstRental.setString(2, tglPinjam);
            pstRental.setInt(3, grandTotal);
            pstRental.executeUpdate();

            // Dapatkan ID Rental yang baru saja di-generate (Auto Increment)
            ResultSet rsKeys = pstRental.getGeneratedKeys();
            int idRentalBaru = 0;
            if (rsKeys.next()) {
                idRentalBaru = rsKeys.getInt(1);
            }

            // 3. Insert ke tabel rental_details & Update Stok Items
            String sqlDetail = "INSERT INTO rental_details (id_rental, id_barang, jumlah_pinjam) VALUES (?, ?, ?)";
            String sqlUpdateStok = "UPDATE items SET stok = stok - ? WHERE id_barang = ?";

            PreparedStatement pstDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement pstStok = conn.prepareStatement(sqlUpdateStok);

            for (int i = 0; i < tblKeranjang.getRowCount(); i++) {
                int idBarang = listIdBarangKeranjang.get(i);
                int qty = Integer.parseInt(tblKeranjang.getValueAt(i, 2).toString());

                // Masukkan ke detail
                pstDetail.setInt(1, idRentalBaru);
                pstDetail.setInt(2, idBarang);
                pstDetail.setInt(3, qty);
                pstDetail.executeUpdate();

                // Potong stok gudang
                pstStok.setInt(1, qty);
                pstStok.setInt(2, idBarang);
                pstStok.executeUpdate();
            }

            // 4. Commit (Simpan permanen) jika semua proses di atas berhasil
            conn.commit();
            JOptionPane.showMessageDialog(this, "Transaksi Rental Berhasil Disimpan!");

            // 5. Bersihkan form untuk transaksi selanjutnya
            modelKeranjang.setRowCount(0);
            listIdBarangKeranjang.clear();
            hitungGrandTotal();
            loadBarang(); // Refresh tabel barang untuk melihat sisa stok terbaru

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Transaksi Gagal: " + e.getMessage());
            // Jika terjadi error, kita bisa melakukan Rollback (opsional tapi disarankan)
        }

    }//GEN-LAST:event_btnSimpanRentalMouseClicked

    private void btnSimpanRentalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanRentalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSimpanRentalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapusKeranjang;
    private javax.swing.JButton btnSimpanRental;
    private javax.swing.JButton btnTambahKeranjang;
    private javax.swing.JComboBox<String> cmbCustomer;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel lblTotalHarga;
    private javax.swing.JScrollPane scrKeranjang;
    private javax.swing.JScrollPane scrPilihBarang;
    private javax.swing.JSpinner spnTanggal;
    private javax.swing.JPanel tableContainer;
    private javax.swing.JTable tblKeranjang;
    private javax.swing.JTable tblPilihBarang;
    // End of variables declaration//GEN-END:variables
}
