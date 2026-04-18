import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

class RiderMenu {
    private LocalDate activeDate;

    public void showMenu(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                         ProfileManager profileManager, ReportExporter reportExporter,
                         CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                         ConfigManager configManager) {

        profileSetupUI(sc, profileManager, jsonFileHandler, manager, csvFileHandler);

        if (profileManager.hasCurrentProfile()) {
            chooseActiveDateUI(sc);

            journeyMenuUI(sc, manager, summaryReport, profileManager,
                    reportExporter, csvFileHandler, configManager);

            saveProfileBeforeExitUI(sc, profileManager, jsonFileHandler);
            saveJourneysBeforeExitUI(sc, manager, csvFileHandler, profileManager);
        }
    }

    private void chooseActiveDateUI(Scanner sc) {
        System.out.println("\n=== Active Day ===");
        activeDate = InputHelper.readDate(sc,
                "Enter active date (DD-MM-YYYY, e.g. 22-04-2026): ");
        System.out.println("Active day set to: " + activeDate);
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

            choice = InputHelper.readIntInRange(sc, "Enter your choice (0-2, e.g. 1): ", 0, 2);

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
        String name = InputHelper.readRequiredText(sc, "Enter your name (e.g. Ali Khan): ");
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
            System.out.println("Active day: " + activeDate);
            System.out.println("Welcome, " + name + "!");
            System.out.println("1. Add journey");
            System.out.println("2. Edit journey");
            System.out.println("3. Delete journey");
            System.out.println("4. List journeys");
            System.out.println("5. Calculate/View running totals");
            System.out.println("6. Summary and Reports");
            System.out.println("7. Change active day");
            System.out.println("0. Exit");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-7, e.g. 1): ", 0, 7);

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
                    showRunningTotalsUI(manager, profileManager, configManager);
                    break;
                case 6:
                    summaryAndReportsMenuUI(sc, manager, summaryReport, profileManager,
                            reportExporter, csvFileHandler);
                    break;
                case 7:
                    chooseActiveDateUI(sc);
                    break;
            }

        } while (choice != 0);
    }

    private void addJourneyUI(Scanner sc, JourneyManager manager,
                              ProfileManager profileManager, ConfigManager configManager) {
        System.out.println("\n=== Add Journey ===");
        System.out.println("Active day: " + activeDate);

        LocalTime time = InputHelper.readTime(sc, "Enter time (HH:mm, e.g. 08:30): ");
        LocalDateTime dateTime = LocalDateTime.of(activeDate, time);

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5, e.g. 2): ", 1, 5);
        int toZone = InputHelper.readIntInRange(sc, "Enter to zone (1-5, e.g. 4): ", 1, 5);

        CityRideDataset.TimeBand band =
                configManager.determineBand(dateTime, configManager.getCurrentConfig());

        System.out.println("Time band: " + band + " (set automatically from journey time)");

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
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to edit for the active day.");
        }
        else {
            printJourneys(dayJourneys);

            int id = InputHelper.readIntInRange(sc, "Enter journey ID to edit (e.g. 3): ", 1, 999);
            Journey j = findActiveDayJourneyById(manager, id);

            if (j == null) {
                System.out.println("ERROR: No journey found with ID=" + id + " for the active day.");
            }
            else {
                LocalDate oldDate = j.getDate();
                LocalTime newTime = InputHelper.readTime(sc, "Enter new time (HH:mm, e.g. 09:15): ");
                LocalDateTime newDateTime = LocalDateTime.of(activeDate, newTime);
                CityRideDataset.TimeBand newBand =
                        configManager.determineBand(newDateTime, configManager.getCurrentConfig());

                System.out.println("Time band: " + newBand + " (set automatically from journey time)");

                j.setDateTime(newDateTime);
                j.setBand(newBand);
                j.setFromZone(InputHelper.readIntInRange(sc, "Enter new from zone (1-5, e.g. 2): ", 1, 5));
                j.setToZone(InputHelper.readIntInRange(sc, "Enter new to zone (1-5, e.g. 4): ", 1, 5));

                manager.recalculateFaresForJourney(j, configManager.getCurrentConfig());
                manager.recalculateChargedFaresForDay(oldDate, j.getType(), configManager.getCurrentConfig());
                manager.recalculateChargedFaresForDay(j.getDate(), j.getType(), configManager.getCurrentConfig());

                System.out.println("Journey updated successfully.");
            }
        }
    }

    private void deleteJourneyUI(Scanner sc, JourneyManager manager, ConfigManager configManager) {
        System.out.println("\n=== Delete Journey ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to delete for the active day.");
        }
        else {
            printJourneys(dayJourneys);

            int id = InputHelper.readIntInRange(sc, "Enter journey ID to delete (e.g. 2): ", 1, 999);
            Journey j = findActiveDayJourneyById(manager, id);

            if (j == null) {
                System.out.println("ERROR: No journey found with ID=" + id + " for the active day.");
            }
            else {
                boolean confirm = InputHelper.readYesNo(sc,
                        "Confirm delete journey ID=" + id + " for " + activeDate + "? (Y/N): ");

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
    }

    private void listJourneysUI(JourneyManager manager) {
        System.out.println("\n=== Journey List ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys recorded for the active day.");
        }
        else {
            printJourneys(dayJourneys);
        }
    }


    private Journey findActiveDayJourneyById(JourneyManager manager, int id) {
        Journey found = null;
        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        int i = 0;
        while (i < dayJourneys.size() && found == null) {
            if (dayJourneys.get(i).getId() == id) {
                found = dayJourneys.get(i);
            }
            i++;
        }

        return found;
    }

    private void printJourneys(List<Journey> journeys) {
        int i = 0;
        while (i < journeys.size()) {
            System.out.println(journeys.get(i));
            i++;
        }
    }

    private void showRunningTotalsUI(JourneyManager manager, ProfileManager profileManager,
                                     ConfigManager configManager) {
        System.out.println("\n=== Running Totals ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys recorded yet for the active day.");
        }
        else {
            CityRideDataset.PassengerType type = profileManager.getCurrentProfile().getPassengerType();
            BigDecimal total = manager.getTotalChargedForDay(activeDate, type);
            BigDecimal cap = configManager.getCurrentConfig().getDailyCap(type);

            System.out.println("Passenger type: " + type);
            System.out.println("Total charged: GBP " + MoneyUtil.money(total));
            System.out.println("Daily cap: GBP " + MoneyUtil.money(cap));

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
            System.out.println("Active day: " + activeDate);
            System.out.println("1. Show daily summary");
            System.out.println("2. Import journeys from CSV");
            System.out.println("3. Export active day journeys to CSV");
            System.out.println("4. Export summary as text");
            System.out.println("5. Export summary as CSV");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-5, e.g. 1): ", 0, 5);

            switch (choice) {
                case 1:
                    showSummaryUI(manager, summaryReport);
                    break;
                case 2:
                    importJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 3:
                    exportJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 4:
                    exportSummaryAsTextUI(manager, summaryReport, reportExporter, profileManager);
                    break;
                case 5:
                    exportSummaryAsCsvUI(manager, reportExporter, profileManager);
                    break;
            }

        } while (choice != 0);
    }

    private void showSummaryUI(JourneyManager manager, SummaryReport summaryReport) {
        System.out.println("\n=== Daily Summary ===");
        System.out.println("Active day: " + activeDate);
        summaryReport.printSummary(manager, activeDate);
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


    private void exportSummaryAsTextUI(JourneyManager manager, SummaryReport summaryReport,
                                       ReportExporter reportExporter, ProfileManager profileManager) {
        System.out.println("\n=== Export Summary as Text ===");
        System.out.println("Active day: " + activeDate);

        String riderName = profileManager.getCurrentProfile().getName();
        String filePath  = buildSummaryFileName(profileManager, activeDate, "txt");

        boolean success = reportExporter.exportSummaryAsText(filePath, riderName, activeDate, summaryReport, manager);

        if (success) {
            System.out.println("Text summary exported to " + filePath);
        }
        else {
            System.out.println("ERROR: Could not export summary.");
        }
    }


    private void exportSummaryAsCsvUI(JourneyManager manager,
                                      ReportExporter reportExporter, ProfileManager profileManager) {
        System.out.println("\n=== Export Summary as CSV ===");
        System.out.println("Active day: " + activeDate);

        String riderName = profileManager.getCurrentProfile().getName();
        String filePath  = buildSummaryFileName(profileManager, activeDate, "csv");

        boolean success = reportExporter.exportSummaryAsCsv(filePath, riderName, activeDate, manager);

        if (success) {
            System.out.println("CSV summary exported to " + filePath);
        }
        else {
            System.out.println("ERROR: Could not export CSV summary.");
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
    // Exports only the active day journeys to the rider's CSV file
    private void exportJourneysUI(JourneyManager manager, CsvFileHandler csvFileHandler,
                                  ProfileManager profileManager) {
        System.out.println("\n=== Export Journeys ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to export for the active day.");
        }
        else {
            String filePath = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(filePath, dayJourneys);

            if (success) {
                System.out.println("Journeys exported to " + filePath);
            }
            else {
                System.out.println("ERROR: Could not export journeys.");
            }
        }
    }

    private void saveJourneysBeforeExitUI(Scanner sc, JourneyManager manager,
                                          CsvFileHandler csvFileHandler,
                                          ProfileManager profileManager) {
        boolean save = InputHelper.readYesNo(sc, "Save active day journeys? (Y/N): ");

        if (save) {
            List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);
            String fileName = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(fileName, dayJourneys);

            if (success) {
                System.out.println("Journeys for " + activeDate + " saved to " + fileName);
            }
            else {
                System.out.println("ERROR: Could not save journeys.");
            }
        }
    }


    private String buildSummaryFileName(ProfileManager profileManager, LocalDate date, String extension) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String profileId = profileManager.getCurrentProfile().getProfileId();
        String safeName  = profileManager.getCurrentProfile().getName().replace(" ", "_");
        return profileId + "_" + safeName + "_" + date.format(dateFormat) + "_summary." + extension;
    }
}