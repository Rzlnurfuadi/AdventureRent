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
 * RentalView — Manual JFrame Version (tanpa .form file)
 * Semua komponen dideklarasikan manual, action pakai mouseClicked.
 *
 * STRUKTUR FILE:
 *   1. Fields (Service + Data Helper)
 *   2. Constructor + initComponents()
 *   3. Panel builder per tab
 *   4. ===== ACTION MOUSE CLICKED =====
 *      - btnRefreshLihat_mouseClicked
 *      - btnTambahKeranjang_mouseClicked
 *      - btnHapusKeranjang_mouseClicked
 *      - btnSimpanRental_mouseClicked
 *      - btnUpdateStatus_mouseClicked
 *      - btnHapusRental_mouseClicked
 *      - tblRental_mouseClicked  (lihat detail)
 *   5. Load Data helpers
 *   6. Variable Declarations
 */
public class Manual extends JFrame {

    // ================================================================
    //  SERVICE & DATA HELPERS
    // ================================================================

    private final RentalService service = new RentalService();

    // Menyimpan id_barang dan harga_sewa tiap baris keranjang
    private final List<Integer> keranjangIdBarang  = new ArrayList<>();
    private final List<Integer> keranjangHargaSewa = new ArrayList<>();
    private int totalHargaKeranjang = 0;

    // Cache data dari DB untuk ComboBox / tabel pilih barang
    private Object[][] dataCustomers; // [id_customer, nama_lengkap]
    private Object[][] dataItems;     // [id_barang, nama_barang, harga_sewa, stok]


    // ================================================================
    //  CONSTRUCTOR
    // ================================================================

    public Manual() {
        initComponents();
        loadAllData();
    }


    // ================================================================
    //  INIT COMPONENTS
    // ================================================================

    private void initComponents() {
        // --- JFrame setup ---
        setTitle("Rental Alat Camping");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ---- HEADER ----
        pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(21, 100, 60));
        pnlHeader.setPreferredSize(new Dimension(0, 60));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblHeaderJudul = new JLabel("Rental Alat Camping — Modul Peminjaman");
        lblHeaderJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderJudul.setForeground(Color.WHITE);

        lblHeaderSub = new JLabel("AdventureRent v1.0");
        lblHeaderSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHeaderSub.setForeground(new Color(180, 220, 180));

        pnlHeader.add(lblHeaderJudul, BorderLayout.WEST);
        pnlHeader.add(lblHeaderSub,  BorderLayout.EAST);

        // ---- TABBED PANE ----
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlTabLihat  = buildTabLihat();
        pnlTabTambah = buildTabTambah();
        pnlTabUpdate = buildTabUpdate();
        pnlTabHapus  = buildTabHapus();

        tabbedPane.addTab("Lihat Rental",   pnlTabLihat);
        tabbedPane.addTab("Tambah Rental",  pnlTabTambah);
        tabbedPane.addTab("Update Status",  pnlTabUpdate);
        tabbedPane.addTab("Hapus Rental",   pnlTabHapus);

        // Refresh data saat ganti tab
        tabbedPane.addChangeListener(e -> {
            switch (tabbedPane.getSelectedIndex()) {
                case 0 -> loadTabelLihat();
                case 1 -> { loadComboCustomer(); loadTabelPilihBarang(); }
                case 2 -> loadTabelUpdate();
                case 3 -> loadTabelHapus();
            }
        });

        // ---- STATUS BAR ----
        lblStatusBar = new JLabel("  AdventureRent © 2026");
        lblStatusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatusBar.setForeground(Color.GRAY);
        lblStatusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        lblStatusBar.setPreferredSize(new Dimension(0, 22));

        add(pnlHeader,   BorderLayout.NORTH);
        add(tabbedPane,  BorderLayout.CENTER);
        add(lblStatusBar, BorderLayout.SOUTH);
    }


    // ================================================================
    //  BUILD TAB 1 — LIHAT RENTAL
    // ================================================================

    private JPanel buildTabLihat() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(245, 248, 245));

        // Tombol refresh
        btnRefreshLihat = new JButton("Refresh Data");
        btnRefreshLihat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshLihat.setBackground(new Color(34, 139, 87));
        btnRefreshLihat.setForeground(Color.WHITE);
        btnRefreshLihat.setFocusPainted(false);
        btnRefreshLihat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnRefreshLihat_mouseClicked(e);
            }
        });

        JPanel pnlBtnLihat = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtnLihat.setOpaque(false);
        pnlBtnLihat.add(btnRefreshLihat);

        // Tabel daftar rental
        tblRental = new JTable();
        mdlRental = new DefaultTableModel(
            new String[]{"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRental.setModel(mdlRental);
        tblRental.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblRental.setRowHeight(26);
        tblRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRental.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblRental.getTableHeader().setBackground(new Color(34, 139, 87));
        tblRental.getTableHeader().setForeground(Color.WHITE);
        tblRental.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                tblRental_mouseClicked(e);
            }
        });

        scrRental = new JScrollPane(tblRental);
        scrRental.setBorder(BorderFactory.createTitledBorder("Daftar Semua Rental"));

        // Tabel detail barang (muncul saat klik baris rental)
        tblDetailLihat = new JTable();
        mdlDetailLihat = new DefaultTableModel(
            new String[]{"ID Barang", "Nama Barang", "Harga Sewa", "Jumlah", "Subtotal"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetailLihat.setModel(mdlDetailLihat);
        tblDetailLihat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDetailLihat.setRowHeight(26);
        tblDetailLihat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblDetailLihat.getTableHeader().setBackground(new Color(34, 139, 87));
        tblDetailLihat.getTableHeader().setForeground(Color.WHITE);

        scrDetailLihat = new JScrollPane(tblDetailLihat);
        scrDetailLihat.setBorder(BorderFactory.createTitledBorder("Detail Barang Rental (klik baris atas)"));

        lblInfoDetailLihat = new JLabel("  Klik baris rental untuk melihat detail barang.");
        lblInfoDetailLihat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoDetailLihat.setForeground(Color.GRAY);

        JPanel pnlBawahLihat = new JPanel(new BorderLayout(4, 4));
        pnlBawahLihat.setOpaque(false);
        pnlBawahLihat.add(scrDetailLihat,   BorderLayout.CENTER);
        pnlBawahLihat.add(lblInfoDetailLihat, BorderLayout.SOUTH);
        pnlBawahLihat.setPreferredSize(new Dimension(0, 220));

        panel.add(pnlBtnLihat,   BorderLayout.NORTH);
        panel.add(scrRental,     BorderLayout.CENTER);
        panel.add(pnlBawahLihat, BorderLayout.SOUTH);

        return panel;
    }


    // ================================================================
    //  BUILD TAB 2 — TAMBAH RENTAL
    // ================================================================

    private JPanel buildTabTambah() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(245, 248, 245));

        // ---- Panel KIRI: Form input + tabel pilih barang ----
        JPanel pnlKiri = new JPanel(new BorderLayout(0, 8));
        pnlKiri.setOpaque(false);

        // Form customer & tanggal
        JPanel pnlFormTambah = new JPanel(new GridBagLayout());
        pnlFormTambah.setBackground(Color.WHITE);
        pnlFormTambah.setBorder(BorderFactory.createTitledBorder("Data Rental"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Label + ComboBox Customer
        lblCmbCustomer = new JLabel("Customer:");
        lblCmbCustomer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        pnlFormTambah.add(lblCmbCustomer, gbc);

        cmbCustomer = new JComboBox<>();
        cmbCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCustomer.setPreferredSize(new Dimension(260, 30));
        gbc.gridx = 1;
        pnlFormTambah.add(cmbCustomer, gbc);

        // Label + Spinner Tanggal
        lblSpnTanggal = new JLabel("Tanggal Pinjam:");
        lblSpnTanggal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1;
        pnlFormTambah.add(lblSpnTanggal, gbc);

        spnTanggal = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spnTanggal.setEditor(new JSpinner.DateEditor(spnTanggal, "dd/MM/yyyy"));
        spnTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spnTanggal.setPreferredSize(new Dimension(260, 30));
        gbc.gridx = 1;
        pnlFormTambah.add(spnTanggal, gbc);

        // Tabel pilih barang
        tblPilihBarang = new JTable();
        mdlPilihBarang = new DefaultTableModel(
            new String[]{"ID", "Nama Barang", "Harga Sewa", "Stok"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPilihBarang.setModel(mdlPilihBarang);
        tblPilihBarang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPilihBarang.setRowHeight(26);
        tblPilihBarang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPilihBarang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPilihBarang.getTableHeader().setBackground(new Color(34, 139, 87));
        tblPilihBarang.getTableHeader().setForeground(Color.WHITE);

        scrPilihBarang = new JScrollPane(tblPilihBarang);
        scrPilihBarang.setBorder(BorderFactory.createTitledBorder("Pilih Barang"));
        scrPilihBarang.setPreferredSize(new Dimension(0, 170));

        // Tombol tambah ke keranjang
        btnTambahKeranjang = new JButton("+ Tambah ke Keranjang");
        btnTambahKeranjang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTambahKeranjang.setBackground(new Color(34, 139, 87));
        btnTambahKeranjang.setForeground(Color.WHITE);
        btnTambahKeranjang.setFocusPainted(false);
        btnTambahKeranjang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnTambahKeranjang_mouseClicked(e);
            }
        });

        JPanel pnlBtnKeranjang = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBtnKeranjang.setOpaque(false);
        pnlBtnKeranjang.add(btnTambahKeranjang);

        pnlKiri.add(pnlFormTambah, BorderLayout.NORTH);
        pnlKiri.add(scrPilihBarang, BorderLayout.CENTER);
        pnlKiri.add(pnlBtnKeranjang, BorderLayout.SOUTH);

        // ---- Panel KANAN: Keranjang ----
        JPanel pnlKanan = new JPanel(new BorderLayout(0, 8));
        pnlKanan.setOpaque(false);
        pnlKanan.setPreferredSize(new Dimension(300, 0));

        tblKeranjang = new JTable();
        mdlKeranjang = new DefaultTableModel(
            new String[]{"Nama Barang", "Harga", "Jml", "Subtotal"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKeranjang.setModel(mdlKeranjang);
        tblKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKeranjang.setRowHeight(26);
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKeranjang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblKeranjang.getTableHeader().setBackground(new Color(34, 139, 87));
        tblKeranjang.getTableHeader().setForeground(Color.WHITE);

        scrKeranjang = new JScrollPane(tblKeranjang);
        scrKeranjang.setBorder(BorderFactory.createTitledBorder("Keranjang Rental"));

        lblTotalHarga = new JLabel("Total: Rp 0");
        lblTotalHarga.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalHarga.setForeground(new Color(21, 100, 60));
        lblTotalHarga.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        btnHapusKeranjang = new JButton("Hapus dari Keranjang");
        btnHapusKeranjang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHapusKeranjang.setBackground(new Color(220, 53, 69));
        btnHapusKeranjang.setForeground(Color.WHITE);
        btnHapusKeranjang.setFocusPainted(false);
        btnHapusKeranjang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnHapusKeranjang_mouseClicked(e);
            }
        });

        btnSimpanRental = new JButton("SIMPAN RENTAL");
        btnSimpanRental.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpanRental.setBackground(new Color(25, 135, 84));
        btnSimpanRental.setForeground(Color.WHITE);
        btnSimpanRental.setFocusPainted(false);
        btnSimpanRental.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnSimpanRental_mouseClicked(e);
            }
        });

        JPanel pnlKananBtn = new JPanel(new GridLayout(3, 1, 4, 4));
        pnlKananBtn.setOpaque(false);
        pnlKananBtn.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        pnlKananBtn.add(lblTotalHarga);
        pnlKananBtn.add(btnHapusKeranjang);
        pnlKananBtn.add(btnSimpanRental);

        pnlKanan.add(scrKeranjang,  BorderLayout.CENTER);
        pnlKanan.add(pnlKananBtn,   BorderLayout.SOUTH);

        panel.add(pnlKiri,  BorderLayout.CENTER);
        panel.add(pnlKanan, BorderLayout.EAST);

        return panel;
    }


    // ================================================================
    //  BUILD TAB 3 — UPDATE STATUS
    // ================================================================

    private JPanel buildTabUpdate() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(245, 248, 245));

        // Tabel daftar rental
        tblUpdateRental = new JTable();
        mdlUpdateRental = new DefaultTableModel(
            new String[]{"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUpdateRental.setModel(mdlUpdateRental);
        tblUpdateRental.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblUpdateRental.setRowHeight(26);
        tblUpdateRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUpdateRental.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblUpdateRental.getTableHeader().setBackground(new Color(34, 139, 87));
        tblUpdateRental.getTableHeader().setForeground(Color.WHITE);

        scrUpdateRental = new JScrollPane(tblUpdateRental);
        scrUpdateRental.setBorder(BorderFactory.createTitledBorder("Pilih Rental yang Akan Diupdate"));

        // Panel form update
        JPanel pnlFormUpdate = new JPanel(new GridBagLayout());
        pnlFormUpdate.setBackground(Color.WHITE);
        pnlFormUpdate.setBorder(BorderFactory.createTitledBorder("Ubah Status Rental"));
        pnlFormUpdate.setPreferredSize(new Dimension(0, 120));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        lblCmbStatusBaru = new JLabel("Status Baru:");
        lblCmbStatusBaru.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        pnlFormUpdate.add(lblCmbStatusBaru, gbc);

        cmbStatusBaru = new JComboBox<>(new String[]{
            RentalService.STATUS_DIPINJAM,
            RentalService.STATUS_SELESAI,
            RentalService.STATUS_DIBATALKAN
        });
        cmbStatusBaru.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatusBaru.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        pnlFormUpdate.add(cmbStatusBaru, gbc);

        btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdateStatus.setBackground(new Color(255, 165, 0));
        btnUpdateStatus.setForeground(new Color(33, 37, 41));
        btnUpdateStatus.setFocusPainted(false);
        btnUpdateStatus.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnUpdateStatus_mouseClicked(e);
            }
        });
        gbc.gridx = 2;
        pnlFormUpdate.add(btnUpdateStatus, gbc);

        lblInfoUpdate = new JLabel(" ");
        lblInfoUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        pnlFormUpdate.add(lblInfoUpdate, gbc);

        panel.add(scrUpdateRental, BorderLayout.CENTER);
        panel.add(pnlFormUpdate,   BorderLayout.SOUTH);

        return panel;
    }


    // ================================================================
    //  BUILD TAB 4 — HAPUS RENTAL
    // ================================================================

    private JPanel buildTabHapus() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(245, 248, 245));

        // Tabel daftar rental
        tblHapusRental = new JTable();
        mdlHapusRental = new DefaultTableModel(
            new String[]{"ID", "Customer", "Tgl Pinjam", "Status", "Total Harga"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHapusRental.setModel(mdlHapusRental);
        tblHapusRental.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHapusRental.setRowHeight(26);
        tblHapusRental.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHapusRental.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblHapusRental.getTableHeader().setBackground(new Color(34, 139, 87));
        tblHapusRental.getTableHeader().setForeground(Color.WHITE);

        scrHapusRental = new JScrollPane(tblHapusRental);
        scrHapusRental.setBorder(BorderFactory.createTitledBorder("Pilih Rental yang Akan Dihapus"));

        // Panel bawah
        JPanel pnlFormHapus = new JPanel(new BorderLayout(8, 4));
        pnlFormHapus.setBackground(Color.WHITE);
        pnlFormHapus.setBorder(BorderFactory.createTitledBorder("Hapus Rental"));
        pnlFormHapus.setPreferredSize(new Dimension(0, 110));

        lblWarningHapus = new JLabel("  Menghapus rental akan mengembalikan stok barang secara otomatis!");
        lblWarningHapus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWarningHapus.setForeground(new Color(130, 80, 0));
        lblWarningHapus.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        btnHapusRental = new JButton("HAPUS RENTAL");
        btnHapusRental.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHapusRental.setBackground(new Color(220, 53, 69));
        btnHapusRental.setForeground(Color.WHITE);
        btnHapusRental.setFocusPainted(false);
        btnHapusRental.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                btnHapusRental_mouseClicked(e);
            }
        });

        lblInfoHapus = new JLabel(" ");
        lblInfoHapus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel pnlHapusBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        pnlHapusBtn.setOpaque(false);
        pnlHapusBtn.add(btnHapusRental);
        pnlHapusBtn.add(lblInfoHapus);

        pnlFormHapus.add(lblWarningHapus, BorderLayout.NORTH);
        pnlFormHapus.add(pnlHapusBtn,    BorderLayout.CENTER);

        panel.add(scrHapusRental, BorderLayout.CENTER);
        panel.add(pnlFormHapus,   BorderLayout.SOUTH);

        return panel;
    }


    // ================================================================
    //
    //   ███╗   ███╗ ██████╗ ██╗   ██╗███████╗███████╗
    //   ████╗ ████║██╔═══██╗██║   ██║██╔════╝██╔════╝
    //   ██╔████╔██║██║   ██║██║   ██║███████╗█████╗
    //   ██║╚██╔╝██║██║   ██║██║   ██║╚════██║██╔══╝
    //   ██║ ╚═╝ ██║╚██████╔╝╚██████╔╝███████║███████╗
    //   ╚═╝     ╚═╝ ╚═════╝  ╚═════╝ ╚══════╝╚══════╝
    //
    //   ACTION MOUSE CLICKED — SEMUA CRUD
    //
    // ================================================================


    // ----------------------------------------------------------------
    //  [READ] Tab 1 — Refresh daftar rental
    // ----------------------------------------------------------------

    private void btnRefreshLihat_mouseClicked(MouseEvent e) {
        loadTabelLihat();
        mdlDetailLihat.setRowCount(0);
        lblInfoDetailLihat.setText("  Data berhasil di-refresh.");
        lblInfoDetailLihat.setForeground(new Color(25, 135, 84));
    }


    // ----------------------------------------------------------------
    //  [READ] Tab 1 — Klik baris tabel → tampilkan detail barang
    // ----------------------------------------------------------------

    private void tblRental_mouseClicked(MouseEvent e) {
        int row = tblRental.getSelectedRow();
        if (row < 0) return;

        int idRental = (int) mdlRental.getValueAt(row, 0);

        // Ambil detail barang via service
        List<RentalDetail> details = service.getDetailByRentalId(idRental);

        mdlDetailLihat.setRowCount(0);
        for (RentalDetail d : details) {
            mdlDetailLihat.addRow(new Object[]{
                d.getIdBarang(),
                d.getNamaBarang(),
                RentalService.formatRupiah(d.getHargaSewa()),
                d.getJumlahPinjam(),
                RentalService.formatRupiah(d.getSubtotal())
            });
        }

        String namaCustomer = mdlRental.getValueAt(row, 1).toString();
        lblInfoDetailLihat.setText(
            "  Rental #" + idRental + " (" + namaCustomer + ") — "
            + details.size() + " barang dipinjam."
        );
        lblInfoDetailLihat.setForeground(new Color(33, 37, 41));
    }


    // ----------------------------------------------------------------
    //  [CREATE] Tab 2 — Tambah barang ke keranjang
    // ----------------------------------------------------------------

    private void btnTambahKeranjang_mouseClicked(MouseEvent e) {
        int row = tblPilihBarang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih barang dari tabel terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil data barang dari cache dataItems
        int    idBarang   = (int)    dataItems[row][0];
        String namaBarang = (String) dataItems[row][1];
        int    hargaSewa  = (int)    dataItems[row][2];
        int    stok       = (int)    dataItems[row][3];

        // Cegah barang duplikat di keranjang
        for (int i = 0; i < mdlKeranjang.getRowCount(); i++) {
            if (mdlKeranjang.getValueAt(i, 0).toString().equals(namaBarang)) {
                JOptionPane.showMessageDialog(this,
                    "Barang ini sudah ada di keranjang!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Dialog input jumlah
        String inputJml = JOptionPane.showInputDialog(this,
            "Masukkan jumlah pinjam untuk:\n" + namaBarang
            + "\nStok tersedia: " + stok,
            "Jumlah Pinjam", JOptionPane.QUESTION_MESSAGE);

        if (inputJml == null || inputJml.trim().isEmpty()) return;

        int jumlah;
        try {
            jumlah = Integer.parseInt(inputJml.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Jumlah harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (jumlah <= 0 || jumlah > stok) {
            JOptionPane.showMessageDialog(this,
                "Jumlah tidak valid! Stok tersedia: " + stok,
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tambahkan ke tabel keranjang
        int subtotal = hargaSewa * jumlah;
        mdlKeranjang.addRow(new Object[]{
            namaBarang,
            RentalService.formatRupiah(hargaSewa),
            jumlah,
            RentalService.formatRupiah(subtotal)
        });

        // Simpan id dan harga ke list tersembunyi
        keranjangIdBarang.add(idBarang);
        keranjangHargaSewa.add(hargaSewa);

        // Update total harga
        totalHargaKeranjang += subtotal;
        lblTotalHarga.setText("Total: " + RentalService.formatRupiah(totalHargaKeranjang));
    }


    // ----------------------------------------------------------------
    //  [CREATE] Tab 2 — Hapus barang dari keranjang
    // ----------------------------------------------------------------

    private void btnHapusKeranjang_mouseClicked(MouseEvent e) {
        int row = tblKeranjang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih barang yang ingin dihapus dari keranjang!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kurangi total harga
        int jumlah = (int) mdlKeranjang.getValueAt(row, 2);
        int harga  = keranjangHargaSewa.get(row);
        totalHargaKeranjang -= harga * jumlah;

        // Hapus dari tabel dan list tersembunyi
        mdlKeranjang.removeRow(row);
        keranjangIdBarang.remove(row);
        keranjangHargaSewa.remove(row);

        lblTotalHarga.setText("Total: " + RentalService.formatRupiah(totalHargaKeranjang));
    }


    // ----------------------------------------------------------------
    //  [CREATE] Tab 2 — Simpan rental ke database
    // ----------------------------------------------------------------

    private void btnSimpanRental_mouseClicked(MouseEvent e) {
        // Validasi customer dipilih
        if (cmbCustomer.getSelectedIndex() < 0 || dataCustomers.length == 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih customer terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi keranjang tidak kosong
        if (mdlKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Keranjang masih kosong! Tambahkan barang terlebih dahulu.",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil data dari form
        int    idCustomer  = (int) dataCustomers[cmbCustomer.getSelectedIndex()][0];
        String namaCustomer = cmbCustomer.getSelectedItem().toString();
        Date   tglPinjam   = (Date) spnTanggal.getValue();

        // Susun list RentalDetail dari isi keranjang
        List<RentalDetail> details = new ArrayList<>();
        for (int i = 0; i < mdlKeranjang.getRowCount(); i++) {
            RentalDetail d = new RentalDetail();
            d.setIdBarang(keranjangIdBarang.get(i));
            d.setNamaBarang(mdlKeranjang.getValueAt(i, 0).toString());
            d.setHargaSewa(keranjangHargaSewa.get(i));
            d.setJumlahPinjam((int) mdlKeranjang.getValueAt(i, 2));
            details.add(d);
        }

        // Dialog konfirmasi
        int confirm = JOptionPane.showConfirmDialog(this,
            "Simpan rental ini?\n"
            + "Customer   : " + namaCustomer + "\n"
            + "Jumlah item: " + details.size() + "\n"
            + "Total harga: " + RentalService.formatRupiah(totalHargaKeranjang),
            "Konfirmasi Simpan", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Kirim ke service
        String result = service.tambahRental(idCustomer, tglPinjam, details);

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                "Rental berhasil disimpan!\nStok barang telah otomatis berkurang.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // Reset semua form dan keranjang
            cmbCustomer.setSelectedIndex(0);
            spnTanggal.setValue(new Date());
            mdlKeranjang.setRowCount(0);
            keranjangIdBarang.clear();
            keranjangHargaSewa.clear();
            totalHargaKeranjang = 0;
            lblTotalHarga.setText("Total: Rp 0");

            // Refresh tabel barang (stok sudah berubah)
            loadTabelPilihBarang();
            loadTabelLihat();
        } else {
            JOptionPane.showMessageDialog(this, result, "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ----------------------------------------------------------------
    //  [UPDATE] Tab 3 — Update status rental
    // ----------------------------------------------------------------

    private void btnUpdateStatus_mouseClicked(MouseEvent e) {
        int row = tblUpdateRental.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih rental dari tabel terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    idRental     = (int) mdlUpdateRental.getValueAt(row, 0);
        String namaCustomer = mdlUpdateRental.getValueAt(row, 1).toString();
        String statusLama   = mdlUpdateRental.getValueAt(row, 3).toString();
        String statusBaru   = (String) cmbStatusBaru.getSelectedItem();

        // Cegah update ke status yang sama
        if (statusLama.equals(statusBaru)) {
            lblInfoUpdate.setText("  Status sudah " + statusBaru + ", tidak ada perubahan.");
            lblInfoUpdate.setForeground(new Color(130, 80, 0));
            return;
        }

        // Dialog konfirmasi
        int confirm = JOptionPane.showConfirmDialog(this,
            "Update status Rental #" + idRental + " (" + namaCustomer + ")?\n"
            + "Status lama : " + statusLama + "\n"
            + "Status baru : " + statusBaru,
            "Konfirmasi Update", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Kirim ke service
        String result = service.updateStatusRental(idRental, statusBaru);

        if ("SUCCESS".equals(result)) {
            lblInfoUpdate.setText(
                "  Rental #" + idRental + " berhasil diupdate → " + statusBaru);
            lblInfoUpdate.setForeground(new Color(25, 135, 84));
            loadTabelUpdate();
            loadTabelLihat();
        } else {
            lblInfoUpdate.setText("  Gagal: " + result);
            lblInfoUpdate.setForeground(new Color(220, 53, 69));
        }
    }


    // ----------------------------------------------------------------
    //  [DELETE] Tab 4 — Hapus rental dari database
    // ----------------------------------------------------------------

    private void btnHapusRental_mouseClicked(MouseEvent e) {
        int row = tblHapusRental.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih rental dari tabel terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    idRental     = (int) mdlHapusRental.getValueAt(row, 0);
        String namaCustomer = mdlHapusRental.getValueAt(row, 1).toString();
        String tglPinjam    = mdlHapusRental.getValueAt(row, 2).toString();

        // Dialog konfirmasi dengan peringatan keras
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin MENGHAPUS rental ini?\n\n"
            + "ID Rental  : " + idRental + "\n"
            + "Customer   : " + namaCustomer + "\n"
            + "Tgl Pinjam : " + tglPinjam + "\n\n"
            + "PERHATIAN: Stok barang akan dikembalikan.\n"
            + "Aksi ini tidak dapat dibatalkan!",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Kirim ke service
        String result = service.hapusRental(idRental);

        if ("SUCCESS".equals(result)) {
            lblInfoHapus.setText("  Rental #" + idRental + " berhasil dihapus.");
            lblInfoHapus.setForeground(new Color(25, 135, 84));
            loadTabelHapus();
            loadTabelLihat();
        } else {
            lblInfoHapus.setText("  Gagal: " + result);
            lblInfoHapus.setForeground(new Color(220, 53, 69));
        }
    }


    // ================================================================
    //  LOAD DATA — Helper methods
    // ================================================================

    private void loadAllData() {
        loadComboCustomer();
        loadTabelPilihBarang();
        loadTabelLihat();
    }

    private void loadComboCustomer() {
        dataCustomers = service.getAllCustomers();
        cmbCustomer.removeAllItems();
        for (Object[] row : dataCustomers) {
            cmbCustomer.addItem(row[1].toString());
        }
    }

    private void loadTabelPilihBarang() {
        dataItems = service.getAvailableItems();
        mdlPilihBarang.setRowCount(0);
        for (Object[] row : dataItems) {
            mdlPilihBarang.addRow(new Object[]{
                row[0],
                row[1],
                RentalService.formatRupiah((int) row[2]),
                row[3]
            });
        }
    }

    private void loadTabelLihat() {
        mdlRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            mdlRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
    }

    private void loadTabelUpdate() {
        mdlUpdateRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            mdlUpdateRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
    }

    private void loadTabelHapus() {
        mdlHapusRental.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Rental r : service.getAllRental()) {
            mdlHapusRental.addRow(new Object[]{
                r.getIdRental(),
                r.getNamaCustomer(),
                sdf.format(r.getTglPinjam()),
                r.getStatusRental(),
                RentalService.formatRupiah(r.getTotalHarga())
            });
        }
    }


    // ================================================================
    //  MAIN — Run standalone
    // ================================================================

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new RentalView().setVisible(true));
    }


    // ================================================================
    //  VARIABLE DECLARATIONS
    //  (Semua komponen GUI dideklarasikan di sini, NetBeans style)
    // ================================================================

    // --- Layout utama ---
    private JPanel        pnlHeader;
    private JTabbedPane   tabbedPane;
    private JLabel        lblHeaderJudul;
    private JLabel        lblHeaderSub;
    private JLabel        lblStatusBar;

    // --- Tab panels ---
    private JPanel        pnlTabLihat;
    private JPanel        pnlTabTambah;
    private JPanel        pnlTabUpdate;
    private JPanel        pnlTabHapus;

    // ---- Tab 1: Lihat Rental ----
    private JTable              tblRental;
    private DefaultTableModel   mdlRental;
    private JScrollPane         scrRental;
    private JTable              tblDetailLihat;
    private DefaultTableModel   mdlDetailLihat;
    private JScrollPane         scrDetailLihat;
    private JButton             btnRefreshLihat;
    private JLabel              lblInfoDetailLihat;

    // ---- Tab 2: Tambah Rental ----
    private JLabel              lblCmbCustomer;
    private JComboBox<String>   cmbCustomer;
    private JLabel              lblSpnTanggal;
    private JSpinner            spnTanggal;
    private JTable              tblPilihBarang;
    private DefaultTableModel   mdlPilihBarang;
    private JScrollPane         scrPilihBarang;
    private JButton             btnTambahKeranjang;
    private JTable              tblKeranjang;
    private DefaultTableModel   mdlKeranjang;
    private JScrollPane         scrKeranjang;
    private JLabel              lblTotalHarga;
    private JButton             btnHapusKeranjang;
    private JButton             btnSimpanRental;

    // ---- Tab 3: Update Status ----
    private JTable              tblUpdateRental;
    private DefaultTableModel   mdlUpdateRental;
    private JScrollPane         scrUpdateRental;
    private JLabel              lblCmbStatusBaru;
    private JComboBox<String>   cmbStatusBaru;
    private JButton             btnUpdateStatus;
    private JLabel              lblInfoUpdate;

    // ---- Tab 4: Hapus Rental ----
    private JTable              tblHapusRental;
    private DefaultTableModel   mdlHapusRental;
    private JScrollPane         scrHapusRental;
    private JLabel              lblWarningHapus;
    private JButton             btnHapusRental;
    private JLabel              lblInfoHapus;
}
