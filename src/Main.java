import com.formdev.flatlaf.FlatIntelliJLaf;
import vista.VistaPrincipal;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new vista.VistaPrincipal().setVisible(true);
        });
    }
}
