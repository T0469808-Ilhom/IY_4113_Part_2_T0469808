import java.util.Scanner;

public class CityRideLite {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        startApp(sc);
        sc.close();
    }

    private static void startApp(Scanner sc) {
        JsonFileHandler jsonFileHandler = new JsonFileHandler();
        ConfigManager configManager = new ConfigManager();
        configManager.loadConfig(jsonFileHandler);


        JourneyManager manager = new JourneyManager();
        SummaryReport summaryReport = new SummaryReport();
        ProfileManager profileManager = new ProfileManager();
        ReportExporter reportExporter = new ReportExporter();
        CsvFileHandler csvFileHandler = new CsvFileHandler();
        RiderMenu riderMenu = new RiderMenu();
        AdminMenu adminMenu = new AdminMenu();

        boolean running = true;
        while (running) {
            int choice = readRoleChoice(sc);

            if (choice == 1) {
                openRiderFlow(sc, manager, summaryReport, profileManager,
                        reportExporter, csvFileHandler, jsonFileHandler, configManager, riderMenu);
            }
            else if (choice == 2) {
                openAdminFlow(sc, adminMenu, configManager, jsonFileHandler);
            }
            else {
                running = false;
            }
        }

        System.out.println("Goodbye!");
    }


    private static int readRoleChoice(Scanner sc) {
        System.out.println("\n=== CityRide Lite ===");
        System.out.println("1. Rider");
        System.out.println("2. Admin");
        System.out.println("0. Exit");

        return InputHelper.readIntInRange(sc, "Enter your choice: ", 0, 2);
    }


    private static void openRiderFlow(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                      ProfileManager profileManager, ReportExporter reportExporter,
                                      CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                                      ConfigManager configManager, RiderMenu riderMenu) {
        riderMenu.showMenu(sc, manager, summaryReport, profileManager,
                reportExporter, csvFileHandler, jsonFileHandler, configManager);
    }


    private static void openAdminFlow(Scanner sc, AdminMenu adminMenu,
                                      ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        adminMenu.showMenu(sc, configManager, jsonFileHandler);
    }
}
