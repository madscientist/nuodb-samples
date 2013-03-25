package com.nuodb.storefront;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadType;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

public class StorefrontApp {
    /**
     * Command line utility to perform various actions related to the Storefront application. 
     * 
     * Specify each action as a separate argument. Valid actions are:
     * <ul>
     * <li>create -- create schema</li>
     * <li>drop -- drop schema</li>
     * <li>showddl -- display drop and create DDL</li>
     * <li>generate -- generate dummy storefront data</li>
     * <li>load -- load storefront data from products.json file</li>
     * <li>simulate -- simulate customer activity</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema();
                System.out.println("Tables created successfully.");
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema();
                System.out.println("Tables dropped successfully.");
            } else if ("showddl".equalsIgnoreCase(action)) {
                showDdl();
            } else if ("generate".equalsIgnoreCase(action)) {
                generateData();
                System.out.println("Data generated successfully.  " + getProductStats());
            } else if ("load".equalsIgnoreCase(action)) {
                loadData();
                System.out.println("Data loaded successfully.  " + getProductStats());
            } else if ("simulate".equalsIgnoreCase(action)) {
                simulateActivity();
            } else {
                throw new IllegalArgumentException("Unknown action:  " + action);
            }
        }
    }
    protected static String getProductStats() {
        ProductFilter filter = new ProductFilter();
        filter.setPageSize(0);
        IStorefrontService svc = StorefrontFactory.createStorefrontService();
        int numProducts = svc.getProducts(filter).getTotalCount();
        int numCategories = svc.getCategories().getTotalCount();
        return "There are " + numProducts + " products across " + numCategories + " categories.";
    }

    public static void createSchema() {
        StorefrontFactory.createSchemaExport().create(false, true);
    }

    public static void dropSchema() {
        StorefrontFactory.createSchemaExport().drop(false, true);
    }
    
    public static void showDdl() {
        StorefrontFactory.createSchemaExport().drop(true, false);
        StorefrontFactory.createSchemaExport().create(true, false);
    }

    public static void generateData() {
        StorefrontFactory.createDataGeneratorService().generate(100, 1000, 2, 10);
    }

    public static void loadData() throws IOException {
        InputStream stream = StorefrontApp.class.getClassLoader().getResourceAsStream("sample-products.json");
        ObjectMapper mapper = new ObjectMapper();
        List<Product> products = mapper.readValue(stream, new TypeReference<ArrayList<Product>>() {
        });
        StorefrontFactory.createDataGeneratorService().generate(100, products, 10);
    }
    
    public static void simulateActivity() throws InterruptedException {
        ISimulatorService simulator = StorefrontFactory.getSimulatorService();
        simulator.addWorkload(WorkloadType.SIMILATED_BROWSER, 20, 250);
        simulator.addWorkload(WorkloadType.SIMILATED_SHOPPER_FAST, 20, 250);

        for (int i = 0; i < 20; i++) {
            printSimulatorStats(simulator, System.out);
            Thread.sleep(5 * 1000);
        }
        printSimulatorStats(simulator, System.out);
        simulator.shutdown();
    }

    private static void printSimulatorStats(ISimulatorService simulator, PrintStream out) {
        out.println();
        out.println(String.format("%-25s %8s %8s %8s %8s | %7s %9s %7s %9s", "Workload", "Active", "Failed", "Killed", "Complete", "Steps",
                "Avg (s)", "Work", "Avg (s)"));
        for (WorkloadStats stats : simulator.getWorkloadStats()) {
            out.println(String.format("%-25s %8d %8d %8d %8d | %7d %9.3f %7d %9.3f",
                    stats.getWorkloadType(),
                    stats.getActiveWorkerCount(),
                    stats.getFailedWorkerCount(),
                    stats.getKilledWorkerCount(),
                    stats.getCompletedWorkerCount(),
                    stats.getWorkInvocationCount(),
                    stats.getAvgWorkTimeMs() / 1000f,
                    stats.getWorkCompletionCount(),
                    stats.getAvgWorkCompletionTimeMs() / 1000f));
        }

        out.println();
        out.println(String.format("%-25s %20s", "Step:", "# Completions:"));
        for (Map.Entry<WorkloadStep, Integer> stepCount : simulator.getWorkloadStepCompletionCounts().entrySet()) {
            out.println(String.format("%-25s %20d", stepCount.getKey(), stepCount.getValue()));
        }
    }
}