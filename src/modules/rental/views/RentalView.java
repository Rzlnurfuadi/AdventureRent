package modules.rental.views;

import modules.rental.models.Rental;
import modules.rental.models.RentalDetail;
import modules.rental.services.RentalService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * View GUI untuk Modul Rental (Peminjaman Alat Camping).
 * Menggunakan Java Swing dengan JTabbedPane untuk 4 operasi CRUD:
 *   - Tab 1: Lihat Semua Rental
 *   - Tab 2: Tambah Rental
 *   - Tab 3: Update Status Rental
 *   - Tab 4: Hapus Rental
 *
 * @author Modul Rental
 */
public class RentalView extends JFrame {

    // ======================= SERVICE =======================
    private final RentalService service = new RentalService();

    // ======================= WARNA & FONT (Tema) =======================
    private static final Color CLR_PRIMARY    = new Color(34, 139, 87);   // hijau camping
    private static final Color CLR_PRIMARY_DK = new Color(21, 100, 60);   // hijau gelap
    private static final Color CLR_ACCENT     = new Color(255, 165, 0);   // oranye aksen
    private static final Color CLR_BG         = new Color(245, 248, 245); // putih kehijauan
    private static final Color CLR_CARD       = Color.WHITE;
    private static final Color CLR_TEXT       = new Color(33, 37, 41);
    private static final Color CLR_DANGER     = new Color(220, 53, 69);
    private static final Color CLR_SUCCESS    = new Color(25, 135, 84);
    private static final Color CLR_WARNING    = new Color(255, 193, 7);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);

    // ======================= KOMPONEN UTAMA =======================
    private JTabbedPane tabbedPane;

    // --- Tab 1: Lihat Rental ---
    private JTable tblRental;
    private DefaultTableModel modelRental;
    private JTable tblDetail;
    private DefaultTableModel modelDetail;
    private JLabel lblInfoRental;

    // --- Tab 2: Tambah Rental ---
    private JComboBox<String> cmbCustomer;
    private JSpinner spnTanggal;
    private JTable tblPilihBarang;
    private DefaultTableModel modelPilihBarang;
    private JTable tblKeranjang;
    private DefaultTableModel modelKeranjang;
    private JLabel lblTotalHarga;
    private JButton btnTambahKeKeranjang;
    private JButton btnHapusDariKeranjang;
    private JButton btnSimpanRental;

    // --- Tab 3: Update Status ---
    private JTable tblUpdateRental;
    private DefaultTableModel modelUpdateRental;
    private JComboBox<String> cmbStatusBaru;
    private JButton btnUpdateStatus;
    private JLabel lblStatusInfo;

    // --- Tab 4: Hapus Rental ---
    private JTable tblHapusRental;
    private DefaultTableModel modelHapusRental;
    private JButton btnHapus;
    private JLabel lblHapusInfo;

    // Data customers & items untuk dropdown
    private Object[][] dataCustomers;
    private Object[][] dataItems;
    // Total harga sementara di keranjang
    private int totalHargaKeranjang = 0;

    // ===================================================================
    //  CONSTRUCTOR
    // ===================================================================

    public RentalView() {
        setTitle("🏕️ AdventureRent — Modul Peminjaman (Rental)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setBackground(CLR_BG);

        initComponents();
        loadData();
    }

    // ===================================================================
    //  INIT KOMPONEN
    // ===================================================================

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Header Bar ---
        add(buatHeader(), BorderLayout.NORTH);

        // --- Tabbed Pane ---
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(FONT_BOLD);
        tabbedPane.setBackground(CLR_BG);

        tabbedPane.addTab("📋  Lihat Rental",   buatTabLihat());
        tabbedPane.addTab("➕  Tambah Rental",  buatTabTambah());
        tabbedPane.addTab("✏️  Update Status",  buatTabUpdate());
        tabbedPane.addTab("🗑️  Hapus Rental",   buatTabHapus());

        // Refresh data saat ganti tab
        tabbedPane.addChangeListener(e -> onTabChanged());

        add(tabbedPane, BorderLayout.CENTER);

        // --- Status Bar ---
        JLabel statusBar = new JLabel("  AdventureRent © 2026 — Modul Peminjaman");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.setPreferredSize(new Dimension(0, 22));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ===================================================================
    //  HEADER
    // ===================================================================

    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_PRIMARY_DK);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("🏕️  Rental Alat Camping — Modul Peminjaman");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("AdventureRent v1.0");
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(new Color(180, 220, 180));
        header.add(lblSub, BorderLayout.EAST);

        return header;
    }

    // ===================================================================
    //  TAB 1: LIHAT RENTAL
    // ===================================================================

    private JPanel buatTabLihat() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ---- Tabel utama rental ----
        String[] colRental = {"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"};
        modelRental = new DefaultTableModel(colRental, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRental = buatTable(modelRental);
        tblRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Saat baris dipilih, tampilkan detail di bawah
        tblRental.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) tampilkanDetailRental();
        });

        // Lebar kolom
        tblRental.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblRental.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblRental.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblRental.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblRental.getColumnModel().getColumn(4).setPreferredWidth(110);

        JScrollPane spRental = new JScrollPane(tblRental);
        spRental.setBorder(buatCardBorder("Daftar Semua Rental"));
        spRental.setPreferredSize(new Dimension(0, 280));

        // ---- Tabel detail barang ----
        String[] colDetail = {"ID Barang", "Nama Barang", "Harga Sewa", "Jumlah", "Subtotal"};
        modelDetail = new DefaultTableModel(colDetail, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetail = buatTable(modelDetail);
        JScrollPane spDetail = new JScrollPane(tblDetail);
        spDetail.setBorder(buatCardBorder("Detail Barang yang Dipinjam"));

        lblInfoRental = new JLabel("  Klik baris rental di atas untuk melihat detail barang.");
        lblInfoRental.setFont(FONT_BODY);
        lblInfoRental.setForeground(Color.GRAY);

        JPanel panelDetail = new JPanel(new BorderLayout(5, 5));
        panelDetail.setOpaque(false);
        panelDetail.add(spDetail, BorderLayout.CENTER);
        panelDetail.add(lblInfoRental, BorderLayout.SOUTH);

        // Tombol refresh
        JButton btnRefresh = buatTombol("🔄 Refresh Data", CLR_PRIMARY);
        btnRefresh.addActionListener(e -> loadDataTabelRental());
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBtn.setOpaque(false);
        panelBtn.add(btnRefresh);

        panel.add(panelBtn, BorderLayout.NORTH);
        panel.add(spRental, BorderLayout.CENTER);
        panel.add(panelDetail, BorderLayout.SOUTH);

        return panel;
    }

    private void tampilkanDetailRental() {
        modelDetail.setRowCount(0);
        int row = tblRental.getSelectedRow();
        if (row < 0) return;

        int idRental = (int) modelRental.getValueAt(row, 0);
        List<RentalDetail> details = service.getDetailByRentalId(idRental);

        for (RentalDetail d : details) {
            modelDetail.addRow(new Object[]{
                d.getIdBarang(),
                d.getNamaBarang(),
                RentalService.formatRupiah(d.getHargaSewa()),
                d.getJumlahPinjam(),
                RentalService.formatRupiah(d.getSubtotal())
            });
        }
        lblInfoRental.setText("  Menampilkan " + details.size() + " barang untuk Rental #" + idRental);
    }

    // ===================================================================
    //  TAB 2: TAMBAH RENTAL
    // ===================================================================

    private JPanel buatTabTambah() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ---- Panel Kiri: Form + Pilih Barang ----
        JPanel panelKiri = new JPanel(new BorderLayout(0, 10));
        panelKiri.setOpaque(false);

        // Form input customer & tanggal
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CLR_CARD);
        formPanel.setBorder(buatCardBorder("Data Rental"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Customer
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(buatLabel("Customer:"), gbc);
        cmbCustomer = new JComboBox<>();
        cmbCustomer.setFont(FONT_BODY);
        cmbCustomer.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        formPanel.add(cmbCustomer, gbc);

        // Tanggal Pinjam
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(buatLabel("Tanggal Pinjam:"), gbc);
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spnTanggal = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnTanggal, "dd/MM/yyyy");
        spnTanggal.setEditor(dateEditor);
        spnTanggal.setFont(FONT_BODY);
        spnTanggal.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        formPanel.add(spnTanggal, gbc);

        // Tabel pilih barang
        String[] colBarang = {"ID", "Nama Barang", "Harga Sewa", "Stok"};
        modelPilihBarang = new DefaultTableModel(colBarang, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPilihBarang = buatTable(modelPilihBarang);
        tblPilihBarang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spBarang = new JScrollPane(tblPilihBarang);
        spBarang.setBorder(buatCardBorder("Pilih Barang (klik baris lalu klik Tambah ke Keranjang)"));
        spBarang.setPreferredSize(new Dimension(0, 160));

        btnTambahKeKeranjang = buatTombol("➕ Tambah ke Keranjang", CLR_PRIMARY);
        btnTambahKeKeranjang.addActionListener(e -> tambahKeKeranjang());
        JPanel panelBtnBarang = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBtnBarang.setOpaque(false);
        panelBtnBarang.add(btnTambahKeKeranjang);

        panelKiri.add(formPanel, BorderLayout.NORTH);
        panelKiri.add(spBarang, BorderLayout.CENTER);
        panelKiri.add(panelBtnBarang, BorderLayout.SOUTH);

        // ---- Panel Kanan: Keranjang ----
        JPanel panelKanan = new JPanel(new BorderLayout(0, 10));
        panelKanan.setOpaque(false);
        panelKanan.setPreferredSize(new Dimension(320, 0));

        String[] colKeranjang = {"Nama Barang", "Harga", "Jml", "Subtotal"};
        modelKeranjang = new DefaultTableModel(colKeranjang, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKeranjang = buatTable(modelKeranjang);
        JScrollPane spKeranjang = new JScrollPane(tblKeranjang);
        spKeranjang.setBorder(buatCardBorder("🛒 Keranjang Rental"));

        lblTotalHarga = new JLabel("Total: Rp 0");
        lblTotalHarga.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalHarga.setForeground(CLR_PRIMARY_DK);
        lblTotalHarga.setBorder(new EmptyBorder(5, 10, 5, 10));

        btnHapusDariKeranjang = buatTombol("🗑 Hapus dari Keranjang", CLR_DANGER);
        btnHapusDariKeranjang.addActionListener(e -> hapusDariKeranjang());

        btnSimpanRental = buatTombol("💾 Simpan Rental", CLR_SUCCESS);
        btnSimpanRental.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpanRental.addActionListener(e -> simpanRental());

        JPanel panelKananBtn = new JPanel(new GridLayout(3, 1, 5, 5));
        panelKananBtn.setOpaque(false);
        panelKananBtn.setBorder(new EmptyBorder(5, 0, 0, 0));
        panelKananBtn.add(lblTotalHarga);
        panelKananBtn.add(btnHapusDariKeranjang);
        panelKananBtn.add(btnSimpanRental);

        panelKanan.add(spKeranjang, BorderLayout.CENTER);
        panelKanan.add(panelKananBtn, BorderLayout.SOUTH);

        panel.add(panelKiri, BorderLayout.CENTER);
        panel.add(panelKanan, BorderLayout.EAST);

        return panel;
    }

    private void tambahKeKeranjang() {
        int row = tblPilihBarang.getSelectedRow();
        if (row < 0) {
            showWarning("Pilih barang dari tabel terlebih dahulu!");
            return;
        }

        int idBarang = (int) modelPilihBarang.getValueAt(row, 0);
        String namaBarang = (String) modelPilihBarang.getValueAt(row, 1);
        int hargaSewa = dataItems[row][2] instanceof Integer
                ? (int) dataItems[row][2]
                : Integer.parseInt(dataItems[row][2].toString());
        int stok = dataItems[row][3] instanceof Integer
                ? (int) dataItems[row][3]
                : Integer.parseInt(dataItems[row][3].toString());

        // Cek apakah sudah ada di keranjang
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            if (modelKeranjang.getValueAt(i, 0).toString().equals(namaBarang)) {
                showWarning("Barang ini sudah ada di keranjang!");
                return;
            }
        }

        // Input jumlah
        String inputJml = JOptionPane.showInputDialog(this,
            "Masukkan jumlah pinjam untuk:\n" + namaBarang + "\n(Stok tersedia: " + stok + ")",
            "Jumlah Pinjam", JOptionPane.QUESTION_MESSAGE);
        if (inputJml == null || inputJml.trim().isEmpty()) return;

        int jumlah;
        try {
            jumlah = Integer.parseInt(inputJml.trim());
        } catch (NumberFormatException ex) {
            showWarning("Jumlah harus berupa angka!");
            return;
        }
        if (jumlah <= 0 || jumlah > stok) {
            showWarning("Jumlah tidak valid! Stok tersedia: " + stok);
            return;
        }

        int subtotal = hargaSewa * jumlah;
        modelKeranjang.addRow(new Object[]{
            namaBarang,
            RentalService.formatRupiah(hargaSewa),
            jumlah,
            RentalService.formatRupiah(subtotal)
        });

        // Simpan referensi id_barang dan harga asli (tersembunyi, simpan di Map)
        // Cara sederhana: encode di nama kolom tersembunyi — kita pakai List terpisah
        keranjangIds.add(idBarang);
        keranjangHarga.add(hargaSewa);

        totalHargaKeranjang += subtotal;
        lblTotalHarga.setText("Total: " + RentalService.formatRupiah(totalHargaKeranjang));
    }

    // List untuk menyimpan data tersembunyi keranjang (id & harga asli)
    private final List<Integer> keranjangIds   = new ArrayList<>();
    private final List<Integer> keranjangHarga = new ArrayList<>();

    private void hapusDariKeranjang() {
        int row = tblKeranjang.getSelectedRow();
        if (row < 0) {
            showWarning("Pilih barang yang ingin dihapus dari keranjang!");
            return;
        }
        int jumlah  = (int) modelKeranjang.getValueAt(row, 2);
        int harga   = keranjangHarga.get(row);
        totalHargaKeranjang -= harga * jumlah;

        modelKeranjang.removeRow(row);
        keranjangIds.remove(row);
        keranjangHarga.remove(row);
        lblTotalHarga.setText("Total: " + RentalService.formatRupiah(totalHargaKeranjang));
    }

    private void simpanRental() {
        // Validasi customer
        if (cmbCustomer.getSelectedIndex() < 0) {
            showWarning("Pilih customer terlebih dahulu!");
            return;
        }
        if (modelKeranjang.getRowCount() == 0) {
            showWarning("Keranjang masih kosong! Tambah barang terlebih dahulu.");
            return;
        }

        int idCustomer = (int) dataCustomers[cmbCustomer.getSelectedIndex()][0];
        Date tglPinjam = (Date) spnTanggal.getValue();

        // Buat list detail dari keranjang
        List<RentalDetail> details = new ArrayList<>();
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            RentalDetail d = new RentalDetail();
            d.setIdBarang(keranjangIds.get(i));
            d.setNamaBarang(modelKeranjang.getValueAt(i, 0).toString());
            d.setHargaSewa(keranjangHarga.get(i));
            d.setJumlahPinjam((int) modelKeranjang.getValueAt(i, 2));
            details.add(d);
        }

        // Konfirmasi
        int confirm = JOptionPane.showConfirmDialog(this,
            "Simpan rental ini?\n"
            + "Customer: " + cmbCustomer.getSelectedItem() + "\n"
            + "Jumlah barang: " + details.size() + " item\n"
            + "Total: " + RentalService.formatRupiah(totalHargaKeranjang),
            "Konfirmasi Simpan Rental", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String result = service.tambahRental(idCustomer, tglPinjam, details);
        if ("SUCCESS".equals(result)) {
            showSuccess("Rental berhasil disimpan!\nStok barang telah diperbarui.");
            resetFormTambah();
            loadDataTabelRental();
        } else {
            showError(result);
        }
    }

    private void resetFormTambah() {
        cmbCustomer.setSelectedIndex(0);
        spnTanggal.setValue(new Date());
        modelKeranjang.setRowCount(0);
        keranjangIds.clear();
        keranjangHarga.clear();
        totalHargaKeranjang = 0;
        lblTotalHarga.setText("Total: Rp 0");
    }

    // ===================================================================
    //  TAB 3: UPDATE STATUS
    // ===================================================================

    private JPanel buatTabUpdate() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Tabel rental
        String[] cols = {"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"};
        modelUpdateRental = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUpdateRental = buatTable(modelUpdateRental);
        tblUpdateRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(tblUpdateRental);
        sp.setBorder(buatCardBorder("Pilih Rental yang Akan Diupdate Statusnya"));

        // Panel form update
        JPanel formUpdate = new JPanel(new GridBagLayout());
        formUpdate.setBackground(CLR_CARD);
        formUpdate.setBorder(buatCardBorder("Ubah Status Rental"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formUpdate.add(buatLabel("Status Baru:"), gbc);

        cmbStatusBaru = new JComboBox<>(new String[]{
            RentalService.STATUS_DIPINJAM,
            RentalService.STATUS_SELESAI,
            RentalService.STATUS_DIBATALKAN
        });
        cmbStatusBaru.setFont(FONT_BODY);
        cmbStatusBaru.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        formUpdate.add(cmbStatusBaru, gbc);

        btnUpdateStatus = buatTombol("✏️ Update Status", CLR_ACCENT);
        btnUpdateStatus.setForeground(CLR_TEXT);
        btnUpdateStatus.addActionListener(e -> doUpdateStatus());
        gbc.gridx = 2;
        formUpdate.add(btnUpdateStatus, gbc);

        lblStatusInfo = new JLabel(" ");
        lblStatusInfo.setFont(FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        formUpdate.add(lblStatusInfo, gbc);

        panel.add(sp, BorderLayout.CENTER);
        panel.add(formUpdate, BorderLayout.SOUTH);

        return panel;
    }

    private void doUpdateStatus() {
        int row = tblUpdateRental.getSelectedRow();
        if (row < 0) {
            showWarning("Pilih rental dari tabel terlebih dahulu!");
            return;
        }
        int idRental = (int) modelUpdateRental.getValueAt(row, 0);
        String namaCustomer = modelUpdateRental.getValueAt(row, 1).toString();
        String statusBaru = (String) cmbStatusBaru.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Update status Rental #" + idRental + " (" + namaCustomer + ")\nmenjadi: " + statusBaru + "?",
            "Konfirmasi Update", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String result = service.updateStatusRental(idRental, statusBaru);
        if ("SUCCESS".equals(result)) {
            lblStatusInfo.setText("✅ Rental #" + idRental + " berhasil diupdate ke status: " + statusBaru);
            lblStatusInfo.setForeground(CLR_SUCCESS);
            loadDataTabelUpdate();
            loadDataTabelRental();
        } else {
            lblStatusInfo.setText("❌ " + result);
            lblStatusInfo.setForeground(CLR_DANGER);
        }
    }

    // ===================================================================
    //  TAB 4: HAPUS RENTAL
    // ===================================================================

    private JPanel buatTabHapus() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Tabel rental
        String[] cols = {"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"};
        modelHapusRental = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHapusRental = buatTable(modelHapusRental);
        tblHapusRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(tblHapusRental);
        sp.setBorder(buatCardBorder("Pilih Rental yang Akan Dihapus"));

        // Panel bawah
        JPanel panelBawah = new JPanel(new BorderLayout(10, 5));
        panelBawah.setBackground(CLR_CARD);
        panelBawah.setBorder(buatCardBorder("Hapus Rental"));
        panelBawah.setPreferredSize(new Dimension(0, 110));

        JLabel lblPeringatan = new JLabel("⚠️ Menghapus rental akan mengembalikan stok barang secara otomatis!");
        lblPeringatan.setFont(FONT_BOLD);
        lblPeringatan.setForeground(new Color(130, 80, 0));
        lblPeringatan.setBorder(new EmptyBorder(10, 15, 5, 15));

        btnHapus = buatTombol("🗑️  HAPUS RENTAL", CLR_DANGER);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHapus.addActionListener(e -> doHapusRental());

        lblHapusInfo = new JLabel(" ");
        lblHapusInfo.setFont(FONT_BODY);
        lblHapusInfo.setBorder(new EmptyBorder(0, 15, 0, 0));

        JPanel rowBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        rowBtn.setOpaque(false);
        rowBtn.add(btnHapus);
        rowBtn.add(lblHapusInfo);

        panelBawah.add(lblPeringatan, BorderLayout.NORTH);
        panelBawah.add(rowBtn, BorderLayout.CENTER);

        panel.add(sp, BorderLayout.CENTER);
        panel.add(panelBawah, BorderLayout.SOUTH);

        return panel;
    }

    private void doHapusRental() {
        int row = tblHapusRental.getSelectedRow();
        if (row < 0) {
            showWarning("Pilih rental dari tabel terlebih dahulu!");
            return;
        }
        int idRental = (int) modelHapusRental.getValueAt(row, 0);
        String namaCustomer = modelHapusRental.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin MENGHAPUS Rental #" + idRental + " (" + namaCustomer + ")?\n"
            + "Stok barang akan dikembalikan!\n"
            + "Aksi ini tidak dapat dibatalkan.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String result = service.hapusRental(idRental);
        if ("SUCCESS".equals(result)) {
            lblHapusInfo.setText("✅ Rental #" + idRental + " berhasil dihapus.");
            lblHapusInfo.setForeground(CLR_SUCCESS);
            loadDataTabelHapus();
            loadDataTabelRental();
        } else {
            lblHapusInfo.setText("❌ " + result);
            lblHapusInfo.setForeground(CLR_DANGER);
        }
    }

    // ===================================================================
    //  LOAD DATA
    // ===================================================================

    private void loadData() {
        loadDataCustomers();
        loadDataItems();
        loadDataTabelRental();
    }

    private void loadDataCustomers() {
        dataCustomers = service.getAllCustomers();
        cmbCustomer.removeAllItems();
        for (Object[] row : dataCustomers) {
            cmbCustomer.addItem(row[1].toString());
        }
    }

    private void loadDataItems() {
        dataItems = service.getAvailableItems();
        modelPilihBarang.setRowCount(0);
        for (Object[] row : dataItems) {
            modelPilihBarang.addRow(new Object[]{
                row[0],
                row[1],
                RentalService.formatRupiah((int) row[2]),
                row[3]
            });
        }
    }

    private void loadDataTabelRental() {
        modelRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            modelRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
        // Warnai baris berdasarkan status
        tblRental.setDefaultRenderer(Object.class, new StatusRowRenderer());
    }

    private void loadDataTabelUpdate() {
        modelUpdateRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            modelUpdateRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
        tblUpdateRental.setDefaultRenderer(Object.class, new StatusRowRenderer());
    }

    private void loadDataTabelHapus() {
        modelHapusRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            modelHapusRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
        tblHapusRental.setDefaultRenderer(Object.class, new StatusRowRenderer());
    }

    private void onTabChanged() {
        int idx = tabbedPane.getSelectedIndex();
        switch (idx) {
            case 0 -> loadDataTabelRental();
            case 1 -> { loadDataCustomers(); loadDataItems(); }
            case 2 -> loadDataTabelUpdate();
            case 3 -> loadDataTabelHapus();
        }
    }

    // ===================================================================
    //  HELPER KOMPONEN UI
    // ===================================================================

    private JTable buatTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setRowHeight(26);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(198, 239, 206));
        table.setSelectionForeground(CLR_TEXT);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(CLR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        return table;
    }

    private JButton buatTombol(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));

        // Hover effect
        Color hover = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private JLabel buatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(CLR_TEXT);
        return lbl;
    }

    private Border buatCardBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200), 1, true),
            " " + title + " ",
            TitledBorder.LEFT, TitledBorder.TOP,
            FONT_BOLD, CLR_PRIMARY_DK
        );
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Berhasil ✅", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Gagal ❌", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan ⚠️", JOptionPane.WARNING_MESSAGE);
    }

    // ===================================================================
    //  STATUS ROW RENDERER — Warnai baris berdasarkan status
    // ===================================================================

    class StatusRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Ambil kolom status (index 3)
                Object statusVal = table.getModel().getValueAt(row, 3);
                String status = statusVal != null ? statusVal.toString() : "";
                switch (status) {
                    case "Dipinjam"   -> c.setBackground(new Color(255, 249, 219)); // kuning muda
                    case "Selesai"    -> c.setBackground(new Color(214, 245, 220)); // hijau muda
                    case "Dibatalkan" -> c.setBackground(new Color(252, 228, 228)); // merah muda
                    default           -> c.setBackground(Color.WHITE);
                }
            }
            ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
            return c;
        }
    }

    // ===================================================================
    //  MAIN — Untuk run standalone
    // ===================================================================

    public static void main(String[] args) {
        // Set Look and Feel Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Gunakan default L&F jika Nimbus tidak tersedia
        }

        SwingUtilities.invokeLater(() -> new RentalView().setVisible(true));
    }
}
