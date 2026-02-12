package proyecto1so.ui;

import javax.swing.table.AbstractTableModel;
import proyecto1so.model.Process;

public class MissionQueueTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Process Name", "Importance"};
    private Process[] data = new Process[0];

    public void setData(Process[] data) {
        this.data = data == null ? new Process[0] : data;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Process p = data[rowIndex];
        switch (columnIndex) {
            case 0: return p.getPid();
            case 1: return p.getPriority();
            default: return "";
        }
    }
}
