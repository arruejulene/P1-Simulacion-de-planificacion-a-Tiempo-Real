package proyecto1so.ui;

import javax.swing.table.AbstractTableModel;
import proyecto1so.model.Process;
import proyecto1so.model.ProcessState;

public class ProcessTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "PID", "Remaining", "Priority", "DeadlineRem"
    };

    private Process[] data = new Process[0];
    private int currentTick = 0;

    public void setData(Process[] data, int currentTick) {
        this.data = data == null ? new Process[0] : data;
        this.currentTick = currentTick;
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
            case 1: return p.getRemainingTime();
            case 2: return p.getPriority();
            case 3:
                int tickForDeadline = currentTick;
                if (p.getState() == ProcessState.TERMINATED && p.getFinishTick() != null) {
                    tickForDeadline = p.getFinishTick();
                }
                int rem = p.getDeadlineRemaining(tickForDeadline);
                return rem == Integer.MAX_VALUE ? "-" : rem;
            default: return "";
        }
    }
}
