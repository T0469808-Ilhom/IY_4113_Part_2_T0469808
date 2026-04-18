import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

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
            System.out.println("1. Add a base fare");
            System.out.println("2. Update a base fare");
            System.out.println("3. Delete a base fare (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateBaseFareUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetBaseFareUI(sc, configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }


    private void updateBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Base Fare ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK (P-Peak/ O-Off Peak): ");
        BigDecimal newFare = InputHelper.readPositiveBigDecimal(sc, "Enter new fare in GBP (e.g. 3.50): ");

        configManager.updateBaseFare(fromZone, toZone, band, newFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Base Fare (Restore Default) ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK (P-Peak/ O-Off Peak): ");

        BigDecimal defaultFare = CityRideDataset.getBaseFare(fromZone, toZone, band);
        configManager.updateBaseFare(fromZone, toZone, band, defaultFare);

        System.out.println("Fare deleted and restored to default: GBP " + defaultFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageDiscountsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Passenger Discounts ===");
            System.out.println("1. Add a discount");
            System.out.println("2. Update a discount");
            System.out.println("3. Delete a discount (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateDiscountUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetDiscountUI(sc, configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updateDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Passenger Discount ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal discount = InputHelper.readDiscountRate(sc,
                "Enter new discount as decimal (0.00 to 1.00, e.g. 0.25 for 25%): ");

        configManager.updateDiscount(type, discount);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Discount (Restore Default) ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultDiscount = CityRideDataset.DISCOUNT_RATE.get(type);
        configManager.updateDiscount(type, defaultDiscount);

        System.out.println("Discount deleted and restored to default: " +
                defaultDiscount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageCapsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Daily Caps ===");
            System.out.println("1. Add a daily cap");
            System.out.println("2. Update a daily cap");
            System.out.println("3. Delete a daily cap (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateDailyCapUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetDailyCapUI(sc, configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updateDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Daily Cap ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal newCap = InputHelper.readPositiveBigDecimal(sc,
                "Enter new daily cap in GBP (e.g. 8.00): ");

        configManager.updateDailyCap(type, newCap);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Daily Cap (Restore Default) ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultCap = CityRideDataset.DAILY_CAP.get(type);
        configManager.updateDailyCap(type, defaultCap);

        System.out.println("Daily cap deleted and restored to default: GBP " + defaultCap);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void managePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Peak Window ===");
            System.out.println("Current: " + configManager.getCurrentConfig().getPeakStart()
                    + " - " + configManager.getCurrentConfig().getPeakEnd());
            System.out.println("1. Add / Set peak window");
            System.out.println("2. Update peak window");
            System.out.println("3. Delete peak window (restore default 07:00 - 09:00)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updatePeakWindowUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetPeakWindowUI(configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updatePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Peak Window ===");

        String peakStart = InputHelper.readTimeString(sc, "Enter new peak start (HH:mm, e.g. 07:00): ");
        String peakEnd   = InputHelper.readTimeString(sc, "Enter new peak end (HH:mm, e.g. 09:00): ");

        if (peakStart.compareTo(peakEnd) >= 0) {
            System.out.println("ERROR: Start time must be before end time. No changes saved.");
        }
        else {
            configManager.updatePeakWindow(peakStart, peakEnd);
            saveAndReport(configManager, jsonFileHandler);
        }
    }


    private void resetPeakWindowUI(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Peak Window (Restore Default) ===");

        configManager.updatePeakWindow("07:00", "09:00");

        System.out.println("Peak window deleted and restored to default: 07:00 - 09:00");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void saveAndReport(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        boolean saved = configManager.saveConfig(configManager.getCurrentConfig(), jsonFileHandler);

        if (saved) {
            System.out.println("Changes saved successfully.");
        }
        else {
            System.out.println("ERROR: Could not save config.");
        }
    }
}