import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.FileReader;

public class CityRideLite {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        startApp(sc);
        sc.close();
    }

    // Creates all objects, loads config, then routes to rider or admin
    private static void startApp(Scanner sc) {
        JsonFileHandler jsonFileHandler = new JsonFileHandler();
        ConfigManager configManager = new ConfigManager();
        SystemConfig config = configManager.loadConfig(jsonFileHandler);

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

    // Reads and validates the role selection from the user
    private static int readRoleChoice(Scanner sc) {
        System.out.println("\n=== CityRide Lite ===");
        System.out.println("1. Rider");
        System.out.println("2. Admin");
        System.out.println("0. Exit");

        return InputHelper.readIntInRange(sc, "Enter your choice: ", 0, 2);
    }

    // Opens the rider flow - passes all dependencies to the rider menu
    private static void openRiderFlow(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                      ProfileManager profileManager, ReportExporter reportExporter,
                                      CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                                      ConfigManager configManager, RiderMenu riderMenu) {
        riderMenu.showMenu(sc, manager, summaryReport, profileManager,
                reportExporter, csvFileHandler, jsonFileHandler, configManager);
    }

    // Opens the admin flow - passes all dependencies to the admin menu
    private static void openAdminFlow(Scanner sc, AdminMenu adminMenu,
                                      ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        adminMenu.showMenu(sc, configManager, jsonFileHandler);
    }
}

class FareCalculator {

    public BigDecimal discountedFare(SystemConfig config, int fromZone, int toZone,
                                     CityRideDataset.TimeBand band,
                                     CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;

        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountRate = config.getDiscount(type);

            if (discountRate != null) {
                BigDecimal discountAmount = baseFare.multiply(discountRate);
                BigDecimal discounted = baseFare.subtract(discountAmount);
                result = money(discounted);
            }
        }

        return result;
    }

    public BigDecimal applyCap(SystemConfig config, BigDecimal runningTotal, BigDecimal discountedFare,
                               CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal cap = config.getDailyCap(type);

        if (cap != null) {
            if (runningTotal.compareTo(cap) >= 0) {
                result = money(BigDecimal.ZERO);
            } else {
                BigDecimal remaining = cap.subtract(runningTotal);

                if (discountedFare.compareTo(remaining) > 0) {
                    result = money(remaining);
                } else {
                    result = money(discountedFare);
                }
            }
        }

        return result;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

class JourneyManager {

    private List<Journey> journeys;
    private FareCalculator calc;
    private int nextID;

    public JourneyManager() {
        journeys = new ArrayList<>();
        calc = new FareCalculator();
        nextID = 1;
    }

    public Journey findJourneyById(int id) {
        Journey foundJourney = null;

        int i = 0;
        while (i < journeys.size() && foundJourney == null) {
            Journey currentJourney = journeys.get(i);

            if (currentJourney.getId() == id) {
                foundJourney = currentJourney;
            }

            i++;
        }

        return foundJourney;
    }

    private List<Journey> getJourneysForDateAndType(LocalDate date, CityRideDataset.PassengerType type) {
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < journeys.size()) {
            Journey j = journeys.get(i);

            if (j.getDate().equals(date) && j.getType() == type) {
                result.add(j);
            }

            i++;
        }

        return result;
    }
    public void appendJourneys(List<Journey> importedJourneys) {
        int i = 0;

        while (i < importedJourneys.size()) {
            Journey journey = importedJourneys.get(i);
            journeys.add(journey);

            if (journey.getId() >= nextID) {
                nextID = journey.getId() + 1;
            }

            i++;
        }
    }

    public void recalculateChargedFaresForDay(LocalDate date, CityRideDataset.PassengerType type, SystemConfig config) {
        List<Journey> dayJourneys = getJourneysForDateAndType(date, type);
        BigDecimal runningTotal = BigDecimal.ZERO;

        int i = 0;
        while (i < dayJourneys.size()) {
            Journey j = dayJourneys.get(i);

            BigDecimal newCharged = calc.applyCap(config, runningTotal, j.getDiscountedFare(), j.getType());
            j.setChargedFare(newCharged);
            runningTotal = runningTotal.add(newCharged);

            i++;
        }
    }

    public boolean addJourney(LocalDateTime dateTime, int fromZone, int toZone,
                              CityRideDataset.TimeBand band, CityRideDataset.PassengerType type,
                              SystemConfig config) {

        boolean added = false;

        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, fromZone, toZone, band, type);
            BigDecimal runningTotal = getTotalChargedForDay(dateTime.toLocalDate(), type);
            BigDecimal chargedFare = calc.applyCap(config, runningTotal, discountedFare, type);

            int id = nextID;
            nextID++;

            Journey newJourney = new Journey(dateTime, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare);

            journeys.add(newJourney);
            added = true;
        }

        return added;
    }

    public boolean removeJourneyById(int id, SystemConfig config) {
        boolean removed = false;
        LocalDate removedDate = null;
        CityRideDataset.PassengerType removedType = null;

        int i = 0;
        while (i < journeys.size() && !removed) {
            Journey j = journeys.get(i);

            if (j.getId() == id) {
                removedDate = j.getDate();
                removedType = j.getType();
                journeys.remove(i);
                removed = true;
            } else {
                i++;
            }
        }

        if (removed) {
            recalculateChargedFaresForDay(removedDate, removedType, config);
        }

        return removed;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {
        BigDecimal total = BigDecimal.ZERO;

        int i = 0;
        while (i < journeys.size()) {
            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                if (j.getType() == type) {
                    total = total.add(j.getChargedFare());
                }
            }

            i++;
        }

        return total;
    }

    public void recalculateFaresForJourney(Journey j, SystemConfig config) {
        BigDecimal baseFare = config.getBaseFare(j.getFromZone(), j.getToZone(), j.getBand());

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, j.getFromZone(), j.getToZone(), j.getBand(), j.getType());
            j.setBaseFare(baseFare);
            j.setDiscountedFare(discountedFare);
            j.setDiscountApplied(baseFare.subtract(discountedFare).setScale(2, RoundingMode.HALF_UP));
        }
    }
    public void setJourneys(List<Journey> newJourneys) {
        journeys.clear();
        nextID = 1;

        int i = 0;
        while (i < newJourneys.size()) {
            Journey journey = newJourneys.get(i);
            journeys.add(journey);

            if (journey.getId() >= nextID) {
                nextID = journey.getId() + 1;
            }

            i++;
        }
    }

    public void clearJourneys() {
        journeys.clear();
        nextID = 1;
    }
}

class RiderProfile {

    enum PaymentOption {
        CARD,
        CASH
    }

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private PaymentOption defaultPaymentOption;
    private String profileId;

    public RiderProfile() {
    }

    public RiderProfile(String profileId, String name,
                        CityRideDataset.PassengerType passengerType,
                        PaymentOption defaultPaymentOption) {
        this.profileId = profileId;
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPaymentOption = defaultPaymentOption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CityRideDataset.PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(CityRideDataset.PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public PaymentOption getDefaultPaymentOption() {
        return defaultPaymentOption;
    }

    public void setDefaultPaymentOption(PaymentOption defaultPaymentOption) {
        this.defaultPaymentOption = defaultPaymentOption;
    }
    public String getProfileId() {
        return profileId;
    }
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}

class ProfileManager {

    private RiderProfile currentProfile;

    // Creates a new profile with a generated unique ID
    public RiderProfile createProfile(String name,
                                      CityRideDataset.PassengerType passengerType,
                                      RiderProfile.PaymentOption paymentOption,
                                      JsonFileHandler jsonFileHandler) {
        String profileId = generateProfileId(jsonFileHandler);
        currentProfile = new RiderProfile(profileId, name, passengerType, paymentOption);
        return currentProfile;
    }

    private String generateProfileId(JsonFileHandler jsonFileHandler) {
        int count = jsonFileHandler.loadProfileCount();
        count++;
        jsonFileHandler.saveProfileCount(count);
        return "R" + count;
    }

    public RiderProfile loadProfile(String profileId, JsonFileHandler jsonFileHandler) {
        RiderProfile profile = jsonFileHandler.loadProfile(profileId + ".json");

        if (profile != null) {
            currentProfile = profile;
        }

        return profile;
    }

    public boolean saveProfile(JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveProfile(currentProfile.getProfileId() + ".json", currentProfile);
    }

    public RiderProfile getCurrentProfile() {

        return currentProfile;
    }

    public void setCurrentProfile(RiderProfile profile) {

        currentProfile = profile;
    }

    public boolean hasCurrentProfile() {
        boolean hasProfile = false;

        if (currentProfile != null) {
            hasProfile = true;
        }

        return hasProfile;
    }
}

class RiderMenu {

    public void showMenu(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                         ProfileManager profileManager, ReportExporter reportExporter,
                         CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                         ConfigManager configManager) {

        profileSetupUI(sc, profileManager, jsonFileHandler, manager, csvFileHandler);

        if (profileManager.hasCurrentProfile()) {

            journeyMenuUI(sc, manager, summaryReport, profileManager,
                    reportExporter, csvFileHandler, configManager);

            saveProfileBeforeExitUI(sc, profileManager, jsonFileHandler);
            saveJourneysBeforeExitUI(sc, manager, csvFileHandler, profileManager);
        }
    }

    private void profileSetupUI(Scanner sc, ProfileManager profileManager,
                                JsonFileHandler jsonFileHandler, JourneyManager manager,
                                CsvFileHandler csvFileHandler) {
        int choice;

        do {
            System.out.println("\n=== CityRide Lite ===");
            System.out.println("1. Create new profile");
            System.out.println("2. Load existing profile");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Enter your choice: ", 0, 2);

            if (choice == 1) {
                createProfileUI(sc, profileManager, jsonFileHandler, manager);
            }
            else if (choice == 2) {
                loadProfileUI(sc, profileManager, jsonFileHandler, manager, csvFileHandler);
            }

        } while (choice != 0 && !profileManager.hasCurrentProfile());
    }
    private void createProfileUI(Scanner sc, ProfileManager profileManager,
                                 JsonFileHandler jsonFileHandler, JourneyManager manager) {
        System.out.println("\n=== Create Profile ===");
        String name = InputHelper.readRequiredText(sc, "Enter your name: ");
        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        RiderProfile.PaymentOption payment = InputHelper.readPaymentOption(sc,
                "Enter payment option (CARD/CASH): ");

        RiderProfile profile = profileManager.createProfile(name, type, payment, jsonFileHandler);
        profileManager.saveProfile(jsonFileHandler);
        manager.clearJourneys();

        System.out.println("Profile created successfully!");
        System.out.println("Your profile ID is: " + profile.getProfileId());
        System.out.println("Please remember your ID to load your profile next time.");
    }

    private void loadProfileUI(Scanner sc, ProfileManager profileManager,
                               JsonFileHandler jsonFileHandler, JourneyManager manager,
                               CsvFileHandler csvFileHandler) {
        System.out.println("\n=== Load Profile ===");

        List<String> available = jsonFileHandler.listAvailableProfiles();

        if (available.isEmpty()) {
            System.out.println("No profiles found. Please create a new profile.");
        }
        else {
            System.out.println("Available profiles:");
            int i = 0;
            while (i < available.size()) {
                System.out.println("  " + available.get(i));
                i++;
            }

            String profileId = InputHelper.readRequiredText(sc, "Enter your profile ID (e.g. R1): ");
            RiderProfile profile = profileManager.loadProfile(profileId, jsonFileHandler);

            if (profile != null) {
                String journeysFile = profile.getProfileId() + "_journeys.csv";
                List<Journey> loadedJourneys = csvFileHandler.importJourneys(journeysFile);

                manager.setJourneys(loadedJourneys);

                System.out.println("Profile loaded. Welcome back, " + profile.getName() + "!");
                System.out.println("Loaded journeys: " + loadedJourneys.size());
            }
            else {
                System.out.println("ERROR: No profile found with ID " + profileId + ". Please try again.");
            }
        }
    }

    private void journeyMenuUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                               ProfileManager profileManager, ReportExporter reportExporter,
                               CsvFileHandler csvFileHandler, ConfigManager configManager) {
        int choice;

        do {
            String name = profileManager.getCurrentProfile().getName();
            System.out.println("\n=== Journey Menu ===");
            System.out.println("Welcome, " + name + "!");
            System.out.println("1. Add journey");
            System.out.println("2. Edit journey");
            System.out.println("3. Delete journey");
            System.out.println("4. List journeys");
            System.out.println("5. View running totals");
            System.out.println("6. Summary and Reports");
            System.out.println("0. Exit");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-6): ", 0, 6);

            switch (choice) {
                case 1:
                    addJourneyUI(sc, manager, profileManager, configManager);
                    break;
                case 2:
                    editJourneyUI(sc, manager, configManager);
                    break;
                case 3:
                    deleteJourneyUI(sc, manager, configManager);
                    break;
                case 4:
                    listJourneysUI(manager);
                    break;
                case 5:
                    showRunningTotalsUI(manager, profileManager, configManager, sc);
                    break;
                case 6:
                    summaryAndReportsMenuUI(sc, manager, summaryReport, profileManager, reportExporter, csvFileHandler);
                    break;
            }

        } while (choice != 0);
    }

    private void addJourneyUI(Scanner sc, JourneyManager manager, ProfileManager profileManager, ConfigManager configManager) {
        System.out.println("\n=== Add Journey ===");
        LocalDateTime dateTime = InputHelper.readDateTime(sc, "Enter date and time (DD-MM-YYYY HH:mm, e.g. 22-04-2026 08:30): ");
        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK): ");
        CityRideDataset.PassengerType type = profileManager.getCurrentProfile().getPassengerType();
        boolean added = manager.addJourney(dateTime, fromZone, toZone, band, type, configManager.getCurrentConfig());

        if (added) {
            System.out.println("Journey added successfully.");
        }
        else {
            System.out.println("ERROR: Could not add journey. Please check zone values.");
        }
    }

    private void editJourneyUI(Scanner sc, JourneyManager manager, ConfigManager configManager) {
        System.out.println("\n=== Edit Journey ===");

        if (manager.getJourneys().isEmpty()) {
            System.out.println("No journeys to edit.");
        }
        else {
            listJourneysUI(manager);
            int id = InputHelper.readIntInRange(sc, "Enter journey ID to edit: ", 1, 999);
            Journey j = manager.findJourneyById(id);

            if (j == null) {
                System.out.println("ERROR: No journey found with ID=" + id);
            }
            else {
                j.setDateTime(InputHelper.readDateTime(sc, "Enter new date and time (DD-MM-YYYY HH:mm): "));
                j.setFromZone(InputHelper.readIntInRange(sc, "Enter new from zone (1-5): ", 1, 5));
                j.setToZone(InputHelper.readIntInRange(sc, "Enter new to zone (1-5): ", 1, 5));
                j.setBand(InputHelper.readTimeBand(sc, "Enter new time band (PEAK/OFF-PEAK): "));
                manager.recalculateFaresForJourney(j, configManager.getCurrentConfig());
                manager.recalculateChargedFaresForDay(j.getDate(), j.getType(), configManager.getCurrentConfig());
                System.out.println("Journey updated successfully.");
            }
        }
    }

    private void deleteJourneyUI(Scanner sc, JourneyManager manager, ConfigManager configManager) {
        System.out.println("\n=== Delete Journey ===");

        if (manager.getJourneys().isEmpty()) {
            System.out.println("No journeys to delete.");
        }
        else {
            listJourneysUI(manager);
            int id = InputHelper.readIntInRange(sc, "Enter journey ID to delete: ", 1, 999);
            boolean confirm = InputHelper.readYesNo(sc, "Confirm delete journey ID=" + id + "? (Y/N): ");

            if (confirm) {
                boolean removed = manager.removeJourneyById(id, configManager.getCurrentConfig());
                if (removed) {
                    System.out.println("Journey deleted and fares recalculated.");
                }
                else {
                    System.out.println("ERROR: No journey found with ID=" + id);
                }
            }
            else {
                System.out.println("Deletion cancelled.");
            }
        }
    }

    private void listJourneysUI(JourneyManager manager) {
        System.out.println("\n=== Journey List ===");

        if (manager.getJourneys().isEmpty()) {
            System.out.println("No journeys recorded.");
        }
        else {
            int i = 0;
            while (i < manager.getJourneys().size()) {
                System.out.println(manager.getJourneys().get(i));
                i++;
            }
        }
    }

    private void showRunningTotalsUI(JourneyManager manager, ProfileManager profileManager,
                                     ConfigManager configManager, Scanner sc) {
        System.out.println("\n=== Running Totals ===");

        if (manager.getJourneys().isEmpty()) {
            System.out.println("No journeys recorded yet.");
        }
        else {
            LocalDate date = InputHelper.readDateTime(sc,
                    "Enter date to view totals (DD-MM-YYYY HH:mm, e.g. 22-04-2026 08:30): ").toLocalDate();
            CityRideDataset.PassengerType type = profileManager.getCurrentProfile().getPassengerType();
            BigDecimal total = manager.getTotalChargedForDay(date, type);
            BigDecimal cap = configManager.getCurrentConfig().getDailyCap(type);

            System.out.println("Passenger type: " + type);
            System.out.println("Total charged: GBP " + total.setScale(2, RoundingMode.HALF_UP));
            System.out.println("Daily cap: GBP " + cap.setScale(2, RoundingMode.HALF_UP));

            if (total.compareTo(cap) >= 0) {
                System.out.println("Cap reached: Yes");
            }
            else {
                System.out.println("Cap reached: No");
            }
        }
    }

    private void summaryAndReportsMenuUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                         ProfileManager profileManager, ReportExporter reportExporter,
                                         CsvFileHandler csvFileHandler) {
        int choice;

        do {
            System.out.println("\n=== Summary and Reports ===");
            System.out.println("1. Show daily summary");
            System.out.println("2. Import journeys from CSV");
            System.out.println("3. Export journeys to CSV");
            System.out.println("4. Export summary report");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-4): ", 0, 4);

            switch (choice) {
                case 1:
                    showSummaryUI(sc, manager, summaryReport);
                    break;
                case 2:
                    importJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 3:
                    exportJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 4:
                    exportSummaryUI(sc, manager, summaryReport, reportExporter, profileManager);
                    break;
            }

        } while (choice != 0);
    }

    private void showSummaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport) {
        System.out.println("\n=== Daily Summary ===");
        LocalDate date = InputHelper.readDateTime(sc, "Enter date (DD-MM-YYYY HH:mm, e.g. 22-04-2026 08:30): ").toLocalDate();
        summaryReport.printSummary(manager, date);
    }

    private void importJourneysUI(JourneyManager manager, CsvFileHandler csvFileHandler,
                                  ProfileManager profileManager) {
        System.out.println("\n=== Import Journeys ===");

        String filePath = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
        List<Journey> imported = csvFileHandler.importJourneys(filePath);

        if (imported.isEmpty()) {
            System.out.println("No journeys found in " + filePath);
        }
        else {
            manager.setJourneys(imported);
            System.out.println(imported.size() + " journeys imported from " + filePath);
        }
    }

    private void exportJourneysUI(JourneyManager manager, CsvFileHandler csvFileHandler,
                                  ProfileManager profileManager) {
        System.out.println("\n=== Export Journeys ===");

        if (manager.getJourneys().isEmpty()) {
            System.out.println("No journeys to export.");
        }
        else {
            String filePath = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(filePath, manager.getJourneys());

            if (success) {
                System.out.println("Journeys exported to " + filePath);
            }
            else {
                System.out.println("ERROR: Could not export journeys.");
            }
        }
    }

    private void exportSummaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                 ReportExporter reportExporter, ProfileManager profileManager) {
        System.out.println("\n=== Export Summary ===");

        LocalDate date = InputHelper.readDateTime(sc,
                "Enter date (DD-MM-YYYY HH:mm, e.g. 22-04-2026 08:30): ").toLocalDate();

        String riderName = profileManager.getCurrentProfile().getName();
        String filePath = buildSummaryFileName(profileManager, date);

        boolean success = reportExporter.exportSummaryAsText(filePath, riderName, date, summaryReport, manager);

        if (success) {
            System.out.println("Summary exported to " + filePath);
        }
        else {
            System.out.println("ERROR: Could not export summary.");
        }
    }


    private void saveProfileBeforeExitUI(Scanner sc, ProfileManager profileManager,
                                         JsonFileHandler jsonFileHandler) {
        boolean save = InputHelper.readYesNo(sc, "Save your profile? (Y/N): ");

        if (save) {
            boolean success = profileManager.saveProfile(jsonFileHandler);
            if (success) {
                System.out.println("Profile saved.");
            }
            else {
                System.out.println("ERROR: Could not save profile.");
            }
        }
    }

    private void saveJourneysBeforeExitUI(Scanner sc, JourneyManager manager,
                                          CsvFileHandler csvFileHandler,
                                          ProfileManager profileManager) {
        boolean save = InputHelper.readYesNo(sc, "Save today's journeys? (Y/N): ");

        if (save) {
            String fileName = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(fileName, manager.getJourneys());

            if (success) {
                System.out.println("Journeys saved to " + fileName);
            }
            else {
                System.out.println("ERROR: Could not save journeys.");
            }
        }
    }
    private String buildSummaryFileName(ProfileManager profileManager, LocalDate date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String profileId = profileManager.getCurrentProfile().getProfileId();
        String riderName = profileManager.getCurrentProfile().getName();
        String safeName = riderName.replace(" ", "_");

        return profileId + "_" + safeName + "_" + date.format(dateFormat) + "_summary.txt";
    }
}

class ConfigManager {

    private SystemConfig currentConfig;

    // Loads config from file. If it fails or file is missing, creates safe defaults instead
    public SystemConfig loadConfig(JsonFileHandler jsonFileHandler) {
        SystemConfig config = jsonFileHandler.loadConfig("config.json");

        if (config == null) {
            // File missing or invalid - start with safe default values
            System.out.println("Config file not found. Using default values.");
            config = createDefaultConfig();
        }

        currentConfig = config;
        return currentConfig;
    }

    // Builds a default config using the values already defined in CityRideDataset
    public SystemConfig createDefaultConfig() {
        SystemConfig config = new SystemConfig();

        // Load all base fares from the dataset as defaults
        int from = 1;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = 1;
            while (to <= CityRideDataset.MAX_ZONE) {
                BigDecimal peakFare = CityRideDataset.getBaseFare(from, to, CityRideDataset.TimeBand.PEAK);
                BigDecimal offPeakFare = CityRideDataset.getBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK);
                config.setBaseFare(from, to, CityRideDataset.TimeBand.PEAK, peakFare);
                config.setBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK, offPeakFare);
                to++;
            }
            from++;
        }

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            config.setDiscount(types[i], CityRideDataset.DISCOUNT_RATE.get(types[i]));
            config.setDailyCap(types[i], CityRideDataset.DAILY_CAP.get(types[i]));
            i++;
        }

        return config;
    }

    public boolean saveConfig(SystemConfig config, JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveConfig("config.json", config);
    }

    public SystemConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(SystemConfig config) {
        currentConfig = config;
    }

    public void updateBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
        currentConfig.setBaseFare(fromZone, toZone, band, fare);
    }

    public void updateDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
        currentConfig.setDiscount(type, discount);
    }

    public void updateDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
        currentConfig.setDailyCap(type, cap);
    }

    public void updatePeakWindow(String peakStart, String peakEnd) {
        currentConfig.setPeakWindow(peakStart, peakEnd);
    }
}

class CsvFileHandler {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public List<Journey> importJourneys(String filePath) {
        List<Journey> journeys = new ArrayList<>();

        try {
            java.io.File file = new java.io.File(filePath);

            if (!file.exists()) {
                return journeys;
            }

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            if (line != null && line.startsWith("id,")) {
                line = reader.readLine();
            }

            while (line != null) {
                Journey journey = parseJourney(line);

                if (journey != null) {
                    journeys.add(journey);
                }

                line = reader.readLine();
            }

            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not import journeys from " + filePath);
        }

        return journeys;
    }

    public boolean exportJourneys(String filePath, List<Journey> journeys) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            writer.write("id,dateTime,fromZone,toZone,band,type,baseFare,discountedFare,chargedFare");
            writer.newLine();

            int i = 0;
            while (i < journeys.size()) {
                Journey j = journeys.get(i);

                writer.write(
                        j.getId() + "," +
                                j.getDateTime().format(DATE_TIME_FORMAT) + "," +
                                j.getFromZone() + "," +
                                j.getToZone() + "," +
                                j.getBand().name() + "," +
                                j.getType().name() + "," +
                                j.getBaseFare().toPlainString() + "," +
                                j.getDiscountedFare().toPlainString() + "," +
                                j.getChargedFare().toPlainString()
                );
                writer.newLine();

                i++;
            }

            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export journeys to " + filePath);
        }

        return success;
    }

    private Journey parseJourney(String line) {
        Journey journey = null;
        String[] parts = line.split(",");

        if (parts.length == 9) {
            try {
                int id = Integer.parseInt(parts[0].trim());
                LocalDateTime dateTime = LocalDateTime.parse(parts[1].trim(), DATE_TIME_FORMAT);
                int fromZone = Integer.parseInt(parts[2].trim());
                int toZone = Integer.parseInt(parts[3].trim());
                CityRideDataset.TimeBand band = CityRideDataset.TimeBand.valueOf(parts[4].trim());
                CityRideDataset.PassengerType type = CityRideDataset.PassengerType.valueOf(parts[5].trim());
                BigDecimal baseFare = new BigDecimal(parts[6].trim());
                BigDecimal discountedFare = new BigDecimal(parts[7].trim());
                BigDecimal chargedFare = new BigDecimal(parts[8].trim());

                journey = new Journey(dateTime, id, fromZone, toZone, band, type,
                        baseFare, discountedFare, chargedFare);
            }
            catch (Exception ex) {
                System.out.println("WARNING: Skipping invalid journey line in CSV.");
            }
        }

        return journey;
    }
}

class JsonFileHandler {

    public boolean saveProfile(String filePath, RiderProfile profile) {
        boolean success = false;

        if (profile == null) {
            System.out.println("ERROR: No profile to save.");
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write("{");
                writer.newLine();
                writer.write("  \"profileId\": \"" + profile.getProfileId() + "\",");
                writer.newLine();
                writer.write("  \"name\": \"" + profile.getName() + "\",");
                writer.newLine();
                writer.write("  \"passengerType\": \"" + profile.getPassengerType().name() + "\",");
                writer.newLine();
                writer.write("  \"defaultPaymentOption\": \"" + profile.getDefaultPaymentOption().name() + "\"");
                writer.newLine();
                writer.write("}");
                writer.close();
                success = true;
            }
            catch (IOException ex) {
                System.out.println("ERROR: Could not save profile to " + filePath);
            }
        }

        return success;
    }

    public RiderProfile loadProfile(String filePath) {
        RiderProfile profile = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            profile = parseProfile(reader);
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not load profile from " + filePath);
        }

        return profile;
    }

    public int loadProfileCount() {
        int count = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("profiles_count.txt"));
            String line = reader.readLine();
            if (line != null) {
                count = Integer.parseInt(line.trim());
            }
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("No profile count file found. Starting from 0.");
        }

        return count;
    }

    public boolean saveProfileCount(int count) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("profiles_count.txt"));
            writer.write(String.valueOf(count));
            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not save profile count.");
        }

        return success;
    }

    // Returns a list of all profile IDs found in the current directory
    public List<String> listAvailableProfiles() {
        List<String> profiles = new ArrayList<>();

        try {
            java.io.File folder = new java.io.File(".");
            java.io.File[] files = folder.listFiles();

            if (files != null) {
                int i = 0;
                while (i < files.length) {
                    String fileName = files[i].getName();
                    if (fileName.startsWith("R") && fileName.endsWith(".json")
                            && !fileName.equals("config.json")) {
                        profiles.add(fileName.replace(".json", ""));
                    }
                    i++;
                }
            }
        }
        catch (Exception ex) {
            System.out.println("ERROR: Could not list profiles.");
        }

        return profiles;
    }

    private RiderProfile parseProfile(BufferedReader reader) {
        String profileId = "";
        String name = "";
        CityRideDataset.PassengerType passengerType = CityRideDataset.PassengerType.ADULT;
        RiderProfile.PaymentOption paymentOption = RiderProfile.PaymentOption.CARD;

        try {
            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (line.startsWith("\"profileId\"")) {
                    profileId = extractValue(line);
                }
                else if (line.startsWith("\"name\"")) {
                    name = extractValue(line);
                }
                else if (line.startsWith("\"passengerType\"")) {
                    try {
                        passengerType = CityRideDataset.PassengerType.valueOf(extractValue(line));
                    }
                    catch (IllegalArgumentException ex) {
                        System.out.println("WARNING: Invalid passenger type in profile file. Using ADULT.");
                    }
                }
                else if (line.startsWith("\"defaultPaymentOption\"")) {
                    try {
                        paymentOption = RiderProfile.PaymentOption.valueOf(extractValue(line));
                    }
                    catch (IllegalArgumentException ex) {
                        System.out.println("WARNING: Invalid payment option in profile file. Using CARD.");
                    }
                }

                line = reader.readLine();
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not read profile data.");
        }

        RiderProfile profile = null;

        if (!name.isEmpty()) {
            profile = new RiderProfile(profileId, name, passengerType, paymentOption);
        }

        return profile;
    }

    public boolean saveConfig(String filePath, SystemConfig config) {
        boolean success = false;

        if (config == null) {
            System.out.println("ERROR: No config to save.");
        } else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write("{");
                writer.newLine();
                writer.write("  \"peakStart\": \"" + config.getPeakStart() + "\",");
                writer.newLine();
                writer.write("  \"peakEnd\": \"" + config.getPeakEnd() + "\",");
                writer.newLine();
                writeDiscounts(writer, config);
                writeCaps(writer, config);
                writeBaseFares(writer, config);
                writer.write("}");
                writer.close();
                success = true;
            }
            catch (IOException ex) {
                System.out.println("ERROR: Could not save config to " + filePath);
            }
        }

        return success;
    }

    public SystemConfig loadConfig(String filePath) {
        SystemConfig config = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            config = parseConfig(reader);
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not load config from " + filePath);
        }

        return config;
    }

    private void writeDiscounts(BufferedWriter writer, SystemConfig config) {
        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        try {
            while (i < types.length) {
                writer.write("  \"discount_" + types[i].name() + "\": \"" + config.getDiscount(types[i]) + "\",");
                writer.newLine();
                i++;
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not write discounts.");
        }
    }

    private void writeCaps(BufferedWriter writer, SystemConfig config) {
        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        try {
            while (i < types.length) {
                writer.write("  \"cap_" + types[i].name() + "\": \"" + config.getDailyCap(types[i]) + "\",");
                writer.newLine();
                i++;
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not write caps.");
        }
    }

    private void writeBaseFares(BufferedWriter writer, SystemConfig config) {
        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            writeBaseFaresForZone(writer, config, from);
            from++;
        }
    }

    private void writeBaseFaresForZone(BufferedWriter writer, SystemConfig config, int from) {
        int to = CityRideDataset.MIN_ZONE;
        try {
            while (to <= CityRideDataset.MAX_ZONE) {
                BigDecimal peak = config.getBaseFare(from, to, CityRideDataset.TimeBand.PEAK);
                BigDecimal offPeak = config.getBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK);
                writer.write("  \"fare_" + from + "_" + to + "_PEAK\": \"" + peak + "\",");
                writer.newLine();
                writer.write("  \"fare_" + from + "_" + to + "_OFF_PEAK\": \"" + offPeak + "\",");
                writer.newLine();
                to++;
            }
        } catch (IOException ex) {
            System.out.println("ERROR: Could not write base fares.");
        }
    }

    private SystemConfig parseConfig(BufferedReader reader) {
        SystemConfig config = new SystemConfig();

        try {
            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (line.startsWith("\"peakStart\"")) {
                    config.setPeakWindow(extractValue(line), config.getPeakEnd());
                }
                else if (line.startsWith("\"peakEnd\"")) {
                    config.setPeakWindow(config.getPeakStart(), extractValue(line));
                }
                else if (line.startsWith("\"discount_")) {
                    parseDiscount(line, config);
                }
                else if (line.startsWith("\"cap_")) {
                    parseCap(line, config);
                }
                else if (line.startsWith("\"fare_")) {
                    parseFare(line, config);
                }

                line = reader.readLine();
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not read config data.");
        }

        return config;
    }

    private void parseDiscount(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String typeName = parts[1].replace("discount_", "");
        config.setDiscount(CityRideDataset.PassengerType.valueOf(typeName), new BigDecimal(extractValue(line)));
    }

    private void parseCap(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String typeName = parts[1].replace("cap_", "");
        config.setDailyCap(CityRideDataset.PassengerType.valueOf(typeName), new BigDecimal(extractValue(line)));
    }

    private void parseFare(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String[] keyParts = parts[1].replace("fare_", "").split("_", 3);
        int from = Integer.parseInt(keyParts[0]);
        int to = Integer.parseInt(keyParts[1]);
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.valueOf(keyParts[2]);
        config.setBaseFare(from, to, band, new BigDecimal(extractValue(line)));
    }

    private String extractValue(String line) {
        String value = "";

        if (line.endsWith(",")) {
            line = line.substring(0, line.length() - 1);
        }

        String[] parts = line.split("\"");

        if (parts.length >= 4) {
            value = parts[3];
        }

        return value;
    }
}

class SystemConfig {

    // Stores base fares using the same key format as CityRideDataset: "from-to-BAND"
    private Map<String, BigDecimal> baseFares;
    private Map<CityRideDataset.PassengerType, BigDecimal> discounts;
    private Map<CityRideDataset.PassengerType, BigDecimal> dailyCaps;
    private String peakStart;
    private String peakEnd;

    public SystemConfig() {
        baseFares = new HashMap<>();
        discounts = new HashMap<>();
        dailyCaps = new HashMap<>();
        peakStart = "07:00";
        peakEnd = "09:00";
    }

    public BigDecimal getBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band) {
        return baseFares.get(CityRideDataset.key(fromZone, toZone, band));
    }

    public void setBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
        baseFares.put(CityRideDataset.key(fromZone, toZone, band), fare);
    }

    public BigDecimal getDiscount(CityRideDataset.PassengerType type) {

        return discounts.get(type);
    }

    public void setDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {

        discounts.put(type, discount);
    }

    public BigDecimal getDailyCap(CityRideDataset.PassengerType type) {
        return dailyCaps.get(type);
    }

    public void setDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {

        dailyCaps.put(type, cap);
    }

    public String getPeakStart() {

        return peakStart;
    }

    public String getPeakEnd() {

        return peakEnd;
    }

    public void setPeakWindow(String peakStart, String peakEnd) {
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
    }
}

class ReportExporter {

    public boolean exportSummaryAsText(String filePath, String riderName,
                                       LocalDate date, SummaryReport summaryReport,
                                       JourneyManager manager) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Rider: " + riderName);
            writer.newLine();
            writer.write("Date: " + date);
            writer.newLine();
            writer.newLine();
            writer.write(summaryReport.buildSummaryText(manager, date));
            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export summary to " + filePath);
        }

        return success;
    }
}

class AdminMenu {

    private static final String ADMIN_PASSWORD = "1234";

    public void showMenu(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        boolean loggedIn = loginUI(sc);

        if (!loggedIn) {
            System.out.println("Incorrect password. Access denied.");
            return;
        }

        int choice;
        do {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. View current config");
            System.out.println("2. Manage base fares");
            System.out.println("3. Manage passenger discounts");
            System.out.println("4. Manage daily caps");
            System.out.println("5. Manage peak window");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-5): ", 0, 5);

            switch (choice) {
                case 1:
                    viewConfigUI(configManager);
                    break;
                case 2:
                    manageFaresUI(sc, configManager, jsonFileHandler);
                    break;
                case 3:
                    manageDiscountsUI(sc, configManager, jsonFileHandler);
                    break;
                case 4:
                    manageCapsUI(sc, configManager, jsonFileHandler);
                    break;
                case 5:
                    managePeakWindowUI(sc, configManager, jsonFileHandler);
                    break;
            }
        } while (choice != 0);
    }


    private boolean loginUI(Scanner sc) {
        boolean result = false;

        System.out.println("\n=== Admin Login ===");
        System.out.print("Enter admin password: ");
        String input = sc.nextLine().trim();

        if (input.equals(ADMIN_PASSWORD)) {
            result = true;
        }

        return result;
    }


    private void viewConfigUI(ConfigManager configManager) {
        SystemConfig config = configManager.getCurrentConfig();

        System.out.println("\n=== Current Configuration ===");
        System.out.println("Peak window: " + config.getPeakStart() + " - " + config.getPeakEnd());

        printDiscounts(config);
        printDailyCaps(config);
        printBaseFares(config, CityRideDataset.TimeBand.PEAK, "PEAK");
        printBaseFares(config, CityRideDataset.TimeBand.OFF_PEAK, "OFF-PEAK");
    }


    private void printDiscounts(SystemConfig config) {
        System.out.println("\nPassenger discounts:");

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            BigDecimal percent = config.getDiscount(types[i])
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP);
            System.out.println("  " + types[i] + ": " + percent + "%");
            i++;
        }
    }


    private void printDailyCaps(SystemConfig config) {
        System.out.println("\nDaily caps:");

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            System.out.println("  " + types[i] + ": GBP " + config.getDailyCap(types[i]));
            i++;
        }
    }


    private void printBaseFares(SystemConfig config, CityRideDataset.TimeBand band, String label) {
        System.out.println("\nBase fares (" + label + "):");

        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = CityRideDataset.MIN_ZONE;
            while (to <= CityRideDataset.MAX_ZONE) {
                System.out.println("  Zone " + from + " -> " + to + ": GBP " + config.getBaseFare(from, to, band));
                to++;
            }
            from++;
        }
    }


    private void manageFaresUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Base Fares ===");
            System.out.println("1. Update a base fare");
            System.out.println("2. Reset a base fare to default");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-2): ", 0, 2);

            if (choice == 1) {
                updateBaseFareUI(sc, configManager, jsonFileHandler);
            } else if (choice == 2) {
                resetBaseFareUI(sc, configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }


    private void updateBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Update Base Fare ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK): ");
        BigDecimal newFare = InputHelper.readPositiveBigDecimal(sc, "Enter new fare in GBP (e.g. 3.50): ");

        configManager.updateBaseFare(fromZone, toZone, band, newFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Reset Base Fare to Default ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK): ");

        BigDecimal defaultFare = CityRideDataset.getBaseFare(fromZone, toZone, band);
        configManager.updateBaseFare(fromZone, toZone, band, defaultFare);

        System.out.println("Fare reset to default: GBP " + defaultFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageDiscountsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Passenger Discounts ===");
            System.out.println("1. Update a discount");
            System.out.println("2. Reset a discount to default");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-2): ", 0, 2);

            if (choice == 1) {
                updateDiscountUI(sc, configManager, jsonFileHandler);
            } else if (choice == 2) {
                resetDiscountUI(sc, configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }

    private void updateDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Update Passenger Discount ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal discount = InputHelper.readDiscountRate(sc,
                "Enter new discount as decimal (0.00 to 1.00, e.g. 0.25 for 25%): ");

        configManager.updateDiscount(type, discount);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Reset Discount to Default ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultDiscount = CityRideDataset.DISCOUNT_RATE.get(type);
        configManager.updateDiscount(type, defaultDiscount);

        System.out.println("Discount reset to default: " +
                defaultDiscount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageCapsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Daily Caps ===");
            System.out.println("1. Update a daily cap");
            System.out.println("2. Reset a daily cap to default");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-2): ", 0, 2);

            if (choice == 1) {
                updateDailyCapUI(sc, configManager, jsonFileHandler);
            } else if (choice == 2) {
                resetDailyCapUI(sc, configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }


    private void updateDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Update Daily Cap ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal newCap = InputHelper.readPositiveBigDecimal(sc,
                "Enter new daily cap in GBP (e.g. 8.00): ");

        configManager.updateDailyCap(type, newCap);
        saveAndReport(configManager, jsonFileHandler);
    }

    private void resetDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Reset Daily Cap to Default ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultCap = CityRideDataset.DAILY_CAP.get(type);
        configManager.updateDailyCap(type, defaultCap);

        System.out.println("Daily cap reset to default: GBP " + defaultCap);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void managePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Peak Window ===");
            System.out.println("Current: " + configManager.getCurrentConfig().getPeakStart()
                    + " - " + configManager.getCurrentConfig().getPeakEnd());
            System.out.println("1. Update peak window");
            System.out.println("2. Reset peak window to default");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-2): ", 0, 2);

            if (choice == 1) {
                updatePeakWindowUI(sc, configManager, jsonFileHandler);
            } else if (choice == 2) {
                resetPeakWindowUI(configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }


    private void updatePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Update Peak Window ===");

        String peakStart = InputHelper.readTimeString(sc, "Enter new peak start (HH:mm, e.g. 07:00): ");
        String peakEnd   = InputHelper.readTimeString(sc, "Enter new peak end (HH:mm, e.g. 09:00): ");


        if (peakStart.compareTo(peakEnd) >= 0) {
            System.out.println("ERROR: Start time must be before end time. No changes saved.");
        } else {
            configManager.updatePeakWindow(peakStart, peakEnd);
            saveAndReport(configManager, jsonFileHandler);
        }
    }


    private void resetPeakWindowUI(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Reset Peak Window to Default ===");

        configManager.updatePeakWindow("07:00", "09:00");

        System.out.println("Peak window reset to default: 07:00 - 09:00");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void saveAndReport(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        boolean saved = configManager.saveConfig(configManager.getCurrentConfig(), jsonFileHandler);

        if (saved) {
            System.out.println("Changes saved successfully.");
        } else {
            System.out.println("ERROR: Could not save config.");
        }
    }
}

class SummaryReport {

    // Holds all calculated values for one day's summary
    private class SummaryData {
        int totalJourneys = 0;
        int mostExpensiveId = -1;
        int peakCount = 0;
        int offPeakCount = 0;
        int[][] zonePairCounts = new int[CityRideDataset.MAX_ZONE + 1][CityRideDataset.MAX_ZONE + 1];
        int[] zoneCounts = new int[CityRideDataset.MAX_ZONE + 1];
        BigDecimal average = BigDecimal.ZERO;
        BigDecimal savings = BigDecimal.ZERO;
        BigDecimal totalCharged = BigDecimal.ZERO;
        BigDecimal mostExpensiveFare = BigDecimal.ZERO;
    }

    // Loops through journeys and fills in all summary values for the given date
    private SummaryData calculateSummaryData(JourneyManager manager, LocalDate date) {
        SummaryData data = new SummaryData();
        List<Journey> list = manager.getJourneys();

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);

            if (j.getDate().equals(date)) {
                data.totalJourneys++;
                data.totalCharged = data.totalCharged.add(j.getChargedFare());

                if (data.mostExpensiveId == -1 || j.getChargedFare().compareTo(data.mostExpensiveFare) > 0) {
                    data.mostExpensiveFare = j.getChargedFare();
                    data.mostExpensiveId = j.getId();
                }

                if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                    data.peakCount++;
                }
                else {
                    data.offPeakCount++;
                }

                int fromZone = j.getFromZone();
                int toZone = j.getToZone();

                if (isValidZone(fromZone) && isValidZone(toZone)) {
                    data.zonePairCounts[fromZone][toZone]++;
                    data.zoneCounts[fromZone]++;
                    if (fromZone != toZone) {
                        data.zoneCounts[toZone]++;
                    }
                }
            }

            i++;
        }

        if (data.totalJourneys > 0) {
            data.average = data.totalCharged.divide(new BigDecimal(data.totalJourneys), 2, RoundingMode.HALF_UP);
        }

        data.savings = calculateSavings(manager, date);
        return data;
    }


    // Prints the daily summary to the console
    public void printSummary(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        System.out.println(buildSummaryText(manager, date));
        printZonePairCounts(data.zonePairCounts);
        printZoneCounts(data.zoneCounts);
    }

    // Builds summary as a text string so ReportExporter can write it to a file
    public String buildSummaryText(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        String result = "";

        result = result + "=== CityRide Lite Daily Summary ===" + "\n";
        result = result + "Date: " + date + "\n";
        result = result + "Total journeys: " + data.totalJourneys + "\n";
        result = result + "Total charged: GBP " + money(data.totalCharged) + "\n";
        result = result + "Average cost per journey: GBP " + money(data.average) + "\n";

        if (data.mostExpensiveId == -1) {
            result = result + "Most expensive journey: none" + "\n";
        }
        else {
            result = result + "Most expensive journey: ID " + data.mostExpensiveId
                    + " (GBP " + money(data.mostExpensiveFare) + ")" + "\n";
        }

        result = result + "Savings from cap: GBP " + money(data.savings) + "\n";

        if (data.savings.compareTo(BigDecimal.ZERO) > 0) {
            result = result + "Cap reached: Yes" + "\n";
        }
        else {
            result = result + "Cap reached: No" + "\n";
        }

        result = result + "Peak journeys: " + data.peakCount + "\n";
        result = result + "Off-peak journeys: " + data.offPeakCount + "\n";

        return result;
    }

    // Calculates total savings from cap for a given date
    // Saving per journey = discounted fare minus what was actually charged
    private BigDecimal calculateSavings(JourneyManager manager, LocalDate date) {
        BigDecimal savings = BigDecimal.ZERO;
        List<Journey> list = manager.getJourneys();

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);
            if (j.getDate().equals(date)) {
                savings = savings.add(j.getDiscountedFare().subtract(j.getChargedFare()));
            }
            i++;
        }

        return money(savings);
    }

    // Prints zone pair counts to the console
    private void printZonePairCounts(int[][] zonePairCounts) {
        System.out.println("\nZone pair counts:");
        boolean hasAny = false;

        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = CityRideDataset.MIN_ZONE;
            while (to <= CityRideDataset.MAX_ZONE) {
                if (zonePairCounts[from][to] > 0) {
                    System.out.println(from + "->" + to + ": " + zonePairCounts[from][to]);
                    hasAny = true;
                }
                to++;
            }
            from++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    // Prints zone involvement counts to the console
    private void printZoneCounts(int[] zoneCounts) {
        System.out.println("\nZone counts:");
        boolean hasAny = false;

        int zone = CityRideDataset.MIN_ZONE;
        while (zone <= CityRideDataset.MAX_ZONE) {
            if (zoneCounts[zone] > 0) {
                System.out.println("Zone " + zone + ": " + zoneCounts[zone]);
                hasAny = true;
            }
            zone++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    // Checks if a zone number is within the valid dataset range
    private boolean isValidZone(int zone) {
        boolean valid = false;
        if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) {
            valid = true;
        }
        return valid;
    }

    // Rounds a money value to 2 decimal places for consistent display
    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

class Journey {
    private LocalDateTime dateTime;
    private int fromZone;
    private int toZone;
    private int zonesCrossed;

    private CityRideDataset.TimeBand band;
    private CityRideDataset.PassengerType type;

    private BigDecimal baseFare;
    private BigDecimal discountedFare;
    private BigDecimal discountApplied;
    private BigDecimal chargedFare;

    private int id;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Journey(LocalDateTime dateTime, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.dateTime = dateTime;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - fromZone) + 1;
        this.band = band;
        this.type = type;
        this.baseFare = baseFare;
        this.discountedFare = discountedFare;
        this.discountApplied = baseFare.subtract(discountedFare).setScale(2, RoundingMode.HALF_UP);
        this.chargedFare = chargedFare;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public CityRideDataset.PassengerType getType() {
        return type;
    }

    public CityRideDataset.TimeBand getBand() {
        return band;
    }

    public int getFromZone() {
        return fromZone;
    }

    public int getToZone() {
        return toZone;
    }

    public int getZonesCrossed() {
        return zonesCrossed;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDiscountedFare() {
        return discountedFare;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public BigDecimal getChargedFare() {
        return chargedFare;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setFromZone(int fromZone) {
        this.fromZone = fromZone;
        this.zonesCrossed = Math.abs(this.toZone - fromZone) + 1;
    }

    public void setToZone(int toZone) {
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - this.fromZone) + 1;
    }

    public void setBand(CityRideDataset.TimeBand band) {
        this.band = band;
    }

    public void setType(CityRideDataset.PassengerType type) {
        this.type = type;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public void setDiscountedFare(BigDecimal discountedFare) {
        this.discountedFare = discountedFare;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public void setChargedFare(BigDecimal chargedFare) {
        this.chargedFare = chargedFare;
    }

    public String toString() {
        return "ID: " + id
                + " | " + dateTime.format(DATE_TIME_FORMAT)
                + " | " + type
                + " | " + band
                + " | " + fromZone + "->" + toZone
                + " | Zones crossed: " + zonesCrossed
                + " | Base: GBP " + baseFare
                + " | Discount: GBP " + discountApplied
                + " | Discounted: GBP " + discountedFare
                + " | Charged: GBP " + chargedFare;
    }
}

class Money {

    public static BigDecimal toMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

class InputHelper {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        boolean valid = false;
        int value = min;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    value = Integer.parseInt(input);

                    if (value < min || value > max) {
                        System.out.println("Please enter a number from " + min + " to " + max + ".");
                    }
                    else {
                        valid = true;
                    }

                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a whole number.");
                }
            }
        }

        return value;
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        boolean valid = false;
        boolean answer = false;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("Y") || input.equals("YES")) {
                answer = true;
                valid = true;
            }
            else if (input.equals("N") || input.equals("NO")) {
                answer = false;
                valid = true;
            }
            else {
                System.out.println("Please enter Y or N.");
            }
        }

        return answer;
    }

    public static String readRequiredText(Scanner scanner, String prompt) {
        boolean valid = false;
        String text = "";

        while (!valid) {
            System.out.print(prompt);
            text = scanner.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                valid = true;
            }
        }

        return text;
    }

    public static LocalDateTime readDateTime(Scanner scanner, String prompt) {
        boolean valid = false;
        LocalDateTime dateTime = LocalDateTime.now();

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    dateTime = LocalDateTime.parse(input, DATE_TIME_FORMAT);
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid date and time. Use DD-MM-YYYY HH:mm.");
                }
            }
        }

        return dateTime;
    }

    public static CityRideDataset.TimeBand readTimeBand(Scanner scanner, String prompt) {
        boolean valid = false;
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.PEAK;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("P") || input.equals("PEAK")) {
                band = CityRideDataset.TimeBand.PEAK;
                valid = true;
            }
            else if (input.equals("O") || input.equals("OFF-PEAK")
                    || input.equals("OFF_PEAK") || input.equals("OFFPEAK")) {
                band = CityRideDataset.TimeBand.OFF_PEAK;
                valid = true;
            }
            else {
                System.out.println("Invalid time band. Enter PEAK or OFF-PEAK.");
            }
        }

        return band;
    }

    public static CityRideDataset.PassengerType readPassengerType(Scanner scanner, String prompt) {
        boolean valid = false;
        CityRideDataset.PassengerType type = CityRideDataset.PassengerType.ADULT;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("ADULT")) {
                type = CityRideDataset.PassengerType.ADULT;
                valid = true;
            }
            else if (input.equals("STUDENT")) {
                type = CityRideDataset.PassengerType.STUDENT;
                valid = true;
            }
            else if (input.equals("CHILD")) {
                type = CityRideDataset.PassengerType.CHILD;
                valid = true;
            }
            else if (input.equals("SENIOR") || input.equals("SENIOR CITIZEN")
                    || input.equals("SENIOR_CITIZEN") || input.equals("SENIORCITIZEN")) {
                type = CityRideDataset.PassengerType.SENIOR_CITIZEN;
                valid = true;
            }
            else {
                System.out.println("Invalid passenger type. Allowed: Adult, Student, Child, Senior Citizen.");
            }
        }

        return type;
    }

    public static RiderProfile.PaymentOption readPaymentOption(Scanner scanner, String prompt) {
        boolean valid = false;
        RiderProfile.PaymentOption paymentOption = RiderProfile.PaymentOption.CARD;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("CARD")) {
                paymentOption = RiderProfile.PaymentOption.CARD;
                valid = true;
            }
            else if (input.equals("CASH")) {
                paymentOption = RiderProfile.PaymentOption.CASH;
                valid = true;
            }
            else {
                System.out.println("Invalid payment option. Enter CARD or CASH.");
            }
        }

        return paymentOption;
    }

    public static BigDecimal readPositiveBigDecimal(Scanner scanner, String prompt) {
        boolean valid = false;
        BigDecimal value = BigDecimal.ZERO;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    value = new BigDecimal(input);

                    if (value.compareTo(BigDecimal.ZERO) <= 0) {
                        System.out.println("Value must be greater than zero.");
                    }
                    else {
                        valid = true;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number (e.g. 3.50).");
                }
            }
        }

        return value;
    }

    public static BigDecimal readDiscountRate(Scanner scanner, String prompt) {
        boolean valid = false;
        BigDecimal value = BigDecimal.ZERO;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            } else {
                try {
                    value = new BigDecimal(input);

                    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
                        System.out.println("Discount must be between 0.00 and 1.00.");
                    } else {
                        valid = true;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a valid decimal (e.g. 0.25).");
                }
            }
        }

        return value;
    }

    // Reads a time string in HH:mm format - used for peak window start and end
    public static String readTimeString(Scanner scanner, String prompt) {
        boolean valid = false;
        String time = "";

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            } else {
                try {
                    java.time.LocalTime.parse(input);
                    time = input;
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid time. Use HH:mm format (e.g. 07:00).");
                }
            }
        }

        return time;
    }
}

final class CityRideDataset {

    private CityRideDataset() {
    }

    public static final int MIN_ZONE = 1;
    public static final int MAX_ZONE = 5;

    public enum TimeBand {
        PEAK,
        OFF_PEAK
    }

    public enum PassengerType {
        ADULT,
        STUDENT,
        CHILD,
        SENIOR_CITIZEN
    }

    public static final Map<PassengerType, BigDecimal> DISCOUNT_RATE = Map.of(
            PassengerType.ADULT, new BigDecimal("0.00"),
            PassengerType.STUDENT, new BigDecimal("0.25"),
            PassengerType.CHILD, new BigDecimal("0.50"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("0.30")
    );

    public static final Map<PassengerType, BigDecimal> DAILY_CAP = Map.of(
            PassengerType.ADULT, new BigDecimal("8.00"),
            PassengerType.STUDENT, new BigDecimal("6.00"),
            PassengerType.CHILD, new BigDecimal("4.00"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("7.00")
    );

    public static final Map<String, BigDecimal> BASE_FARE = buildBaseFare();

    public static BigDecimal getBaseFare(int fromZone, int toZone, TimeBand timeBand) {
        return BASE_FARE.get(key(fromZone, toZone, timeBand));
    }

    public static String key(int fromZone, int toZone, TimeBand timeBand) {
        return fromZone + "-" + toZone + "-" + timeBand.name();
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<String, BigDecimal> buildBaseFare() {
        Map<String, BigDecimal> m = new HashMap<>();

        // Peak fares
        put(m, 1, 1, TimeBand.PEAK, "2.50");
        put(m, 1, 2, TimeBand.PEAK, "3.20");
        put(m, 1, 3, TimeBand.PEAK, "3.80");
        put(m, 1, 4, TimeBand.PEAK, "4.40");
        put(m, 1, 5, TimeBand.PEAK, "5.00");

        put(m, 2, 1, TimeBand.PEAK, "3.20");
        put(m, 2, 2, TimeBand.PEAK, "2.30");
        put(m, 2, 3, TimeBand.PEAK, "3.10");
        put(m, 2, 4, TimeBand.PEAK, "3.80");
        put(m, 2, 5, TimeBand.PEAK, "4.50");

        put(m, 3, 1, TimeBand.PEAK, "3.80");
        put(m, 3, 2, TimeBand.PEAK, "3.10");
        put(m, 3, 3, TimeBand.PEAK, "2.10");
        put(m, 3, 4, TimeBand.PEAK, "3.00");
        put(m, 3, 5, TimeBand.PEAK, "3.70");

        put(m, 4, 1, TimeBand.PEAK, "4.40");
        put(m, 4, 2, TimeBand.PEAK, "3.80");
        put(m, 4, 3, TimeBand.PEAK, "3.00");
        put(m, 4, 4, TimeBand.PEAK, "2.00");
        put(m, 4, 5, TimeBand.PEAK, "2.90");

        put(m, 5, 1, TimeBand.PEAK, "5.00");
        put(m, 5, 2, TimeBand.PEAK, "4.50");
        put(m, 5, 3, TimeBand.PEAK, "3.70");
        put(m, 5, 4, TimeBand.PEAK, "2.90");
        put(m, 5, 5, TimeBand.PEAK, "1.90");

        // Off-peak fares
        put(m, 1, 1, TimeBand.OFF_PEAK, "2.00");
        put(m, 1, 2, TimeBand.OFF_PEAK, "2.70");
        put(m, 1, 3, TimeBand.OFF_PEAK, "3.20");
        put(m, 1, 4, TimeBand.OFF_PEAK, "3.70");
        put(m, 1, 5, TimeBand.OFF_PEAK, "4.20");

        put(m, 2, 1, TimeBand.OFF_PEAK, "2.70");
        put(m, 2, 2, TimeBand.OFF_PEAK, "1.90");
        put(m, 2, 3, TimeBand.OFF_PEAK, "2.60");
        put(m, 2, 4, TimeBand.OFF_PEAK, "3.20");
        put(m, 2, 5, TimeBand.OFF_PEAK, "3.80");

        put(m, 3, 1, TimeBand.OFF_PEAK, "3.20");
        put(m, 3, 2, TimeBand.OFF_PEAK, "2.60");
        put(m, 3, 3, TimeBand.OFF_PEAK, "1.70");
        put(m, 3, 4, TimeBand.OFF_PEAK, "2.50");
        put(m, 3, 5, TimeBand.OFF_PEAK, "3.10");

        put(m, 4, 1, TimeBand.OFF_PEAK, "3.70");
        put(m, 4, 2, TimeBand.OFF_PEAK, "3.20");
        put(m, 4, 3, TimeBand.OFF_PEAK, "2.50");
        put(m, 4, 4, TimeBand.OFF_PEAK, "1.60");
        put(m, 4, 5, TimeBand.OFF_PEAK, "2.40");

        put(m, 5, 1, TimeBand.OFF_PEAK, "4.20");
        put(m, 5, 2, TimeBand.OFF_PEAK, "3.80");
        put(m, 5, 3, TimeBand.OFF_PEAK, "3.10");
        put(m, 5, 4, TimeBand.OFF_PEAK, "2.40");
        put(m, 5, 5, TimeBand.OFF_PEAK, "1.50");

        return Map.copyOf(m);
    }

    private static void put(Map<String, BigDecimal> m, int from, int to, TimeBand band, String amount) {
        m.put(key(from, to, band), money(amount));
    }
}