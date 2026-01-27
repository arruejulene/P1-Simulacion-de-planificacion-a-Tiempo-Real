package proyecto1so.model;

public class StateManager {
    public static boolean canTransition(ProcessState from, ProcessState to) {
        if (from == null || to == null) return false;
        if (from == to) return true;

        switch (from) {
            case NEW:
                return to == ProcessState.READY
                        || to == ProcessState.READY_SUSPENDED;

            case READY:
                return to == ProcessState.RUNNING
                        || to == ProcessState.READY_SUSPENDED;

            case RUNNING:
                return to == ProcessState.READY
                        || to == ProcessState.BLOCKED
                        || to == ProcessState.TERMINATED
                        || to == ProcessState.READY_SUSPENDED;

            case BLOCKED:
                return to == ProcessState.READY
                        || to == ProcessState.BLOCKED_SUSPENDED;

            case READY_SUSPENDED:
                return to == ProcessState.READY;

            case BLOCKED_SUSPENDED:
                return to == ProcessState.BLOCKED
                        || to == ProcessState.READY_SUSPENDED;

            case TERMINATED:
                return false;

            default:
                return false;
        }
    }

    public static boolean transition(PCB pcb, ProcessState to) {
        if (pcb == null) return false;
        ProcessState from = pcb.getState();
        if (!canTransition(from, to)) return false;
        pcb.setState(to);
        return true;
    }
}

