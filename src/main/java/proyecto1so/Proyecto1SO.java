/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package proyecto1so;

import javax.swing.SwingUtilities;
import proyecto1so.ui.MainSimulationUI;

public class Proyecto1SO {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainSimulationUI ui = new MainSimulationUI();
            ui.setVisible(true);
        });
    }
}
