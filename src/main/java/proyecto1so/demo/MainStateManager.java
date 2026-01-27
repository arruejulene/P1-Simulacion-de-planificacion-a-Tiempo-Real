package proyecto1so.demo;

import proyecto1so.model.PCB;
import proyecto1so.model.ProcessState;
import proyecto1so.model.StateManager;
import proyecto1so.model.TaskType;

public class MainStateManager {
    public static void main(String[] args) {
        PCB p = new PCB(
                1,
                "P1",
                3,
                50,
                20,
                TaskType.CPU_BOUND,
                false,
                0,
                0,
                0
        );

        System.out.println("INITIAL: " + p.getState());

        System.out.println("NEW -> READY: " + StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());
        System.out.println("READY -> RUNNING: " + StateManager.transition(p, ProcessState.RUNNING) + " state=" + p.getState());
        System.out.println("RUNNING -> BLOCKED: " + StateManager.transition(p, ProcessState.BLOCKED) + " state=" + p.getState());
        System.out.println("BLOCKED -> READY: " + StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());

        System.out.println("READY -> TERMINATED (INVALID): " + StateManager.transition(p, ProcessState.TERMINATED) + " state=" + p.getState());

        System.out.println("READY -> READY_SUSPENDED: " + StateManager.transition(p, ProcessState.READY_SUSPENDED) + " state=" + p.getState());
        System.out.println("READY_SUSPENDED -> READY: " + StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());

        System.out.println("RUNNING -> READY_SUSPENDED (need RUNNING first): " + StateManager.transition(p, ProcessState.READY_SUSPENDED) + " state=" + p.getState());

        System.out.println();
        PCB p2 = new PCB(
                2,
                "P2",
                2,
                40,
                10,
                TaskType.IO_BOUND,
                false,
                0,
                0,
                0
        );

        System.out.println("P2 INITIAL: " + p2.getState());
        System.out.println("NEW -> READY_SUSPENDED: " + StateManager.transition(p2, ProcessState.READY_SUSPENDED) + " state=" + p2.getState());
        System.out.println("READY_SUSPENDED -> READY: " + StateManager.transition(p2, ProcessState.READY) + " state=" + p2.getState());
        System.out.println("READY -> RUNNING: " + StateManager.transition(p2, ProcessState.RUNNING) + " state=" + p2.getState());
        System.out.println("RUNNING -> BLOCKED: " + StateManager.transition(p2, ProcessState.BLOCKED) + " state=" + p2.getState());
        System.out.println("BLOCKED -> BLOCKED_SUSPENDED: " + StateManager.transition(p2, ProcessState.BLOCKED_SUSPENDED) + " state=" + p2.getState());
        System.out.println("BLOCKED_SUSPENDED -> READY_SUSPENDED (event happened): " + StateManager.transition(p2, ProcessState.READY_SUSPENDED) + " state=" + p2.getState());
        System.out.println("READY_SUSPENDED -> READY (reactivation): " + StateManager.transition(p2, ProcessState.READY) + " state=" + p2.getState());
        System.out.println("READY -> RUNNING: " + StateManager.transition(p2, ProcessState.RUNNING) + " state=" + p2.getState());
        System.out.println("RUNNING -> TERMINATED: " + StateManager.transition(p2, ProcessState.TERMINATED) + " state=" + p2.getState());
       
        System.out.println("BLOCKED_SUSPENDED -> RUNNING (INVALID): " + StateManager.transition(p2, ProcessState.RUNNING) + " state=" + p2.getState());

    }
}

