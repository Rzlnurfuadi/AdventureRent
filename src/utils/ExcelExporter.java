package utils; // Sesuaikan dengan nama package kamu

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JTable;
import javax.swing.table.TableModel;
// Import library Apache POI (Pastikan JAR sudah dimasukkan ke Libraries)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {    

    public void exportTable(JTable table, File file) {
        try {
            // Membuat Workbook (File Excel baru format .xlsx)
            Workbook workbook = new XSSFWorkbook();
            // Membuat Sheet baru
            Sheet sheet = workbook.createSheet("Laporan Rental");

            // Mengambil model dari JTable kamu (Ini memanggil DefaultTableModel)
            TableModel model = table.getModel();

            // 1. MEMBUAT BARIS HEADER (Judul Kolom)
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < model.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(model.getColumnName(i));
            }

            // 2. MEMASUKKAN DATA BARIS DEMI BARIS
            for (int i = 0; i < model.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1); // i + 1 karena baris 0 dipakai header
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Cell cell = row.createCell(j);
                    // Ambil nilai dari tabel, ubah jadi String
                    Object value = model.getValueAt(i, j);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(""); // Jika data kosong
                    }
                }
            }

            // 3. MENYIMPAN KE FILE .xlsx
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            workbook.close();

        } catch (Exception e) {
            System.out.println("Gagal export: " + e.getMessage());
        }
    }
}