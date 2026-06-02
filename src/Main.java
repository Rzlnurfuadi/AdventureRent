import modules.rental.views.RentalView;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set tampilan Nimbus agar lebih bagus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* pakai default */ }

        // Jalankan GUI di thread yang benar
        SwingUtilities.invokeLater(() -> {
            new RentalView().setVisible(true);
        });
    }
}