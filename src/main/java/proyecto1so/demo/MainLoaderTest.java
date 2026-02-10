package proyecto1so.demo;

import proyecto1so.loader.LoadResult;
import proyecto1so.loader.ProcessLoader;
import proyecto1so.model.Process;

public class MainLoaderTest {

    public static void main(String[] args) {
        String jsonPath = args.length > 0 ? args[0] : "sample-data/processes.json";

        ProcessLoader loader = new ProcessLoader();

        System.out.println("[LOADER TEST] JSON => " + jsonPath);
        LoadResult jsonResult = loader.loadFromJson(jsonPath);
        printResult("JSON", jsonResult);
    }

    private static void printResult(String label, LoadResult result) {
        Process[] processes = result.getProcesses();
        String[] errors = result.getErrors();

        System.out.println("[" + label + "] processes loaded: " + processes.length);
        for (int i = 0; i < processes.length; i++) {
            Process p = processes[i];
            System.out.println("  " + p.getPid()
                    + " burst=" + p.getBurstTime()
                    + " arrival=" + p.getArrivalTime()
                    + " priority=" + p.getPriority()
                    + " deadline=" + (p.getDeadlineTick() == Integer.MAX_VALUE ? "-" : p.getDeadlineTick()));
        }

        System.out.println("[" + label + "] errors: " + errors.length);
        for (int i = 0; i < errors.length; i++) {
            System.out.println("  - " + errors[i]);
        }
    }
}
