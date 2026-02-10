package proyecto1so.loader;

import proyecto1so.model.Process;

public class LoadResult {
    private final Process[] processes;
    private final String[] errors;

    public LoadResult(Process[] processes, String[] errors) {
        this.processes = processes == null ? new Process[0] : processes;
        this.errors = errors == null ? new String[0] : errors;
    }

    public Process[] getProcesses() {
        return processes;
    }

    public String[] getErrors() {
        return errors;
    }
}
