# IY4113 Part 2 Milestone 3

| Assessment Details | Please Complete All Details                                 |
| ------------------ | ----------------------------------------------------------- |
| Group              | B                                                           |
| Module Title       | IY4113 Applied Software Engineering using Object-Orientated |
| Programming        | Java                                                        |
| Assessment Type    | Coursework                                                  |
| Module Tutor Name  | Jonathan Shore                                              |
| Student ID Number  | P0469808                                                    |
| Date of Submission | 06.04.2026                                                  |
| Word Count         | 5368                                                        |
| GitHub Link        | https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808   |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*

- [x] *Where I have used AI, I have cited and referenced appropriately.

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 2, at least 3)

------------------------------------------------------------------------------------------------------------------------------

#### Research 1:

**Title of research:** How to read and write JSON files using core Java

**Reference:** Attacomsian. (2019). *How to read and write JSON files using core Java*. Available at: [How to read and write JSON Files in Java](https://attacomsian.com/blog/java-read-write-json-files) [Accessed 04 April 2026]

**How does the research help with coding practise?**:

Part 2 requires saving rider profiles and system configuration to JSON files between sessions. Java has no built-in JSON library, so this source showed me how to handle JSON using only core Java building JSON strings with StringBuilder and writing them with Files.writeString(). This directly informed my JsonFileHandler class, which serialises rider profiles and config data without any external dependencies. The source also highlighted that special characters in string values (like quotes or backslashes) can break a JSON file if not escaped. I will add an escape() helper method in JsonFileHandler specifically because of this. 

**Key coding ideas you could reuse in your program:**

Use Files.writeString() and Files.readString() for clean, one-line file access with no streams to manage manually. Build JSON output field by field using StringBuilder so the format is readable and easy to debug. Always wrap file operations in try-catch, return null or false on failure, and print a specific error message so the user knows what went wrong.

**Screenshot of research:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%201.png)

------------------------------------------------------------------------------------------------------------------------------

#### Research 2:

**Title of research:** Reading and Writing CSVs in Java

**Reference (link):** Stack Abuse. (2019). *Reading and Writing CSVs in Java*. Available at: [Reading and Writing CSVs in Java](https://stackabuse.com/reading-and-writing-csvs-in-java/) [Accessed 04 April 2026]

**How does the research help with coding practise?:** 

Also requirements of Part 2 requires importing journeys from a CSV file and exporting journeys and summary reports to CSV. This source showed how to handle CSV files using only core Java BufferedReader for reading and FileWriter for writing with no external libraries needed. The most important thing I learned was that the header row must be read and skipped before the loop starts, otherwise the program tries to parse column names as journey data. I will apply this in readJourneys() with a reader.readLine() call before the loop begins. The source also explained that this approach breaks if field values contain commas. This is why I will try to keep the rider profile details separate from the journey CSV entirely the rider's name and passenger type are already stored in the JSON profile file, so duplicating them in every CSV row would be messy and harder to maintain if the rider's details ever changed. All write operations will use String.join() followed by writer.newLine(), and invalid rows during import are skipped with a warning rather than stopping the whole operation.

**Key coding ideas you could reuse in your program:**

Read CSV files line by line with BufferedReader.readLine() in a while loop until null is returned. Write each row as a comma-separated string using String.join() followed by writer.newLine(). Skip invalid rows with a warning message instead of stopping the whole import this way one bad row does not block all the others from loading.

**Screenshot of research:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%202.png)

---

#### Research 3:

**Title of research:** LocalDateTime Class in Java

**Reference:** Nkamphoa. (2026). *LocalDateTime Class in Java*. Available at: [LocalDateTime Class in Java](https://nkamphoa.com/localdatetime-class-in-java/) [Accessed 04 April 2026]

**How does the research help with coding practice:**

In Part 1 the Journey class used LocalDate to record when a journey was added just the date, with no time. Part 2 requirement specificaly says that each journey must include a date and time, so LocalDate was no longer sufficient. I needed to understand LocalDateTime and how to switch to it without breaking the existing journey logic. This source explained that LocalDateTime stores both date and time in a single object with the default format yyyy-MM-dd-mm:ss, and that it can be parsed from a string using LocalDateTime.parse() with a custom DateTimeFormatter for example DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"). This is what I will use in the Menu.readDateTime() method, where the user enters a journey time in the format 04/04/2026 08:30 and the input is parsed into a LocalDateTime object. The source also explained that LocalDateTime keeps toLocalDate() available, which let me keep the existing date-based logic in JourneyManager such as grouping journeys by day for the daily cap calculation without rewriting it. I simply call j.getDate() which internally calls dateTime.toLocalDate() on the stored LocalDateTime.

Key coding ideas you could reuse:

Use DateTimeFormatter.of Pattern ("dd/MM/yyyy HH:mm") to parse user-entered date and time strings into a LocalDateTime object define the formatter as a static final constant in the class so it is created once and reused. Call dateTime.toLocalDate() when you only need the date part of a LocalDateTime, which avoids having to store a separate LocalDate field. Catch DateTimeParseException when parsing user input and re-prompt with a clear format example.

**Screenshot:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%203.png)

------------------------------------------------------------------------------------------------------------------------------

### Program Code

---

Paste the current program code created so far. It does not have to be runnable code (document though if it does not work!)

------------------------------------------------------------------------------------------------------------------------------

*Program code goes here:*

```java
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

public class CityRideLite {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice = -1;

        JourneyManager manager = new JourneyManager();
        SummaryReport summaryReport = new SummaryReport();

        do {
            System.out.println("\n=== CityRide Lite ===");
            System.out.println("1) Add journey");
            System.out.println("2) List journeys");
            System.out.println("3) Summary");
            System.out.println("4) Remove journey");
            System.out.println("0) Exit");
            System.out.print("Choose an option (0-4): ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("ERROR: Please enter a number.");
                continue;
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addJourneyUI(sc, manager);
                    break;

                case 2:
                    listJourneysUI(manager);
                    break;

                case 3:
                    summaryUI(sc, manager, summaryReport);
                    break;

                case 4:
                    removeJourneyUI(sc, manager);
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("ERROR: Please enter a number from 0 to 4.");
                    break;
            }

        } while (choice != 0);

        sc.close();
    }

    private static void addJourneyUI(Scanner sc, JourneyManager manager) {

        LocalDate date = readDateDayMonthYear(sc);
        int from = readIntInRange(sc, "From zone (1-5): ", 1, 5);
        int to = readIntInRange(sc, "To zone (1-5): ", 1, 5);

        CityRideDataset.TimeBand band = readTimeBand(sc);
        CityRideDataset.PassengerType type = readPassengerType(sc);

        boolean added = manager.addJourney(date, from, to, band, type);

        if (added) {
            System.out.println("Journey added.");
        } else {
            System.out.println("Journey not added");
        }
    }

    private static void listJourneysUI(JourneyManager manager) {

        List<Journey> list = manager.getJourneys();

        if (list.isEmpty()) {
            System.out.println("No journeys recorded.");
        } else {
            int i = 0;

            while (i < list.size()) {
                System.out.println(list.get(i));
                i++;
            }
        }
    }

    private static void summaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport) {

        LocalDate date = readDateDayMonthYear(sc);
        summaryReport.printSummary(manager, date);
    }

    private static void removeJourneyUI(Scanner sc, JourneyManager manager) {

        List<Journey> list = manager.getJourneys();

        if (list.isEmpty()) {
            System.out.println("No journeys to remove.");
        } else {

            listJourneysUI(manager);

            int id = readIntMin(sc, "Enter journey ID to remove (>=1): ", 1);

            boolean confirm = readYesNo(sc, "Confirm remove journey ID=" + id + "? (Y/N): ");

            if (!confirm) {
                System.out.println("Removal cancelled.");
            } else {
                boolean removed = manager.removeJourneyById(id);

                if (removed) {
                    System.out.println("Journey removed.");
                } else {
                    System.out.println("No journey found with ID=" + id);
                }
            }
        }
    }

    private static String readRequiredName(Scanner sc) {

        boolean valid = false;
        String name = "";

        while (!valid) {

            System.out.print("Enter passenger name (required): ");
            name = sc.nextLine().trim();

            if (name.length() == 0) {
                System.out.println("Name cannot be empty.");
            } else {
                valid = true;
            }
        }

        return name;
    }

    private static LocalDate readDateDayMonthYear(Scanner sc) {

        boolean valid = false;
        LocalDate date = LocalDate.now();

        while (!valid) {

            System.out.print("Enter date (DD-MM-YYYY): ");
            String s = sc.nextLine().trim();

            if (s.length() == 0) {
                System.out.println("Date is required.");
            } else {

                try {
                    date = LocalDate.parse(s, DATE_FORMAT);
                    valid = true;
                } catch (DateTimeParseException ex) {
                    System.out.println("Invalid date. Example: 22-02-2026");
                }
            }
        }

        return date;
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {

        boolean valid = false;
        int value = min;

        while (!valid) {
            System.out.print(prompt);

            String input = sc.nextLine().trim();

            if (!isDigits(input)) {
                System.out.println("Invalid input. Please enter a whole number.");
            } else {
                value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println("Out of range. Try again.");
                } else {
                    valid = true;
                }
            }
        }

        return value;
    }

    private static CityRideDataset.TimeBand readTimeBand(Scanner sc) {

        boolean valid = false;
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.PEAK;

        while (!valid) {
            System.out.print("Time band (P=PEAK, O=OFF_PEAK): ");
            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("P") || s.equals("PEAK")) {
                band = CityRideDataset.TimeBand.PEAK;
                valid = true;
            } else if (s.equals("O") || s.equals("OFF_PEAK")) {
                band = CityRideDataset.TimeBand.OFF_PEAK;
                valid = true;
            } else {
                System.out.println("Invalid input. Type P or O.");
            }
        }

        return band;
    }

    private static CityRideDataset.PassengerType readPassengerType(Scanner sc) {

        boolean valid = false;
        CityRideDataset.PassengerType type = CityRideDataset.PassengerType.ADULT;

        while (!valid) {
            System.out.print("Passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("SENIOR")) {
                s = "SENIOR_CITIZEN";
            }

            if (s.equals("ADULT")) {
                type = CityRideDataset.PassengerType.ADULT;
                valid = true;
            } else if (s.equals("STUDENT")) {
                type = CityRideDataset.PassengerType.STUDENT;
                valid = true;
            } else if (s.equals("CHILD")) {
                type = CityRideDataset.PassengerType.CHILD;
                valid = true;
            } else if (s.equals("SENIOR_CITIZEN")) {
                type = CityRideDataset.PassengerType.SENIOR_CITIZEN;
                valid = true;
            } else {
                System.out.println("Invalid input. Try: ADULT, STUDENT, CHILD, SENIOR_CITIZEN.");
            }
        }

        return type;
    }

    private static boolean isDigits(String text) {

        boolean ok = true;

        if (text.length() == 0) {
            ok = false;
        } else {
            int i = 0;
            while (i < text.length() && ok) {
                char c = text.charAt(i);

                if (!Character.isDigit(c)) {
                    ok = false;
                }

                i++;
            }
        }

        return ok;
    }

    private static int readIntMin(Scanner sc, String prompt, int min) {

        boolean isValid = false;
        int value = min;

        while (!isValid) {

            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (!isDigits(input)) {
                System.out.println("Invalid input. Please enter a whole number.");
            } else {

                value = Integer.parseInt(input);

                if (value < min) {
                    System.out.println("Out of range. Input should be positive whole number. " + min);
                } else {
                    isValid = true;
                }
            }
        }

        return value;
    }

    private static boolean readYesNo(Scanner sc, String prompt) {

        boolean valid = false;
        boolean answer = false;

        while (!valid) {

            System.out.print(prompt);
            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("Y") || s.equals("YES")) {
                answer = true;
                valid = true;
            } else if (s.equals("N") || s.equals("NO")) {
                answer = false;
                valid = true;
            } else {
                System.out.println("Invalid input. Type Y or N.");
            }
        }

        return answer;
    }
    private static void startApp(Scanner sc) {
    }

    private static int readRoleChoice(Scanner sc) {
        return 0;
    }

    private static void openRiderFlow(Scanner sc,
                                      JourneyManager manager,
                                      SummaryReport summaryReport,
                                      //RiderMenu riderMenu,
                                      ProfileManager profileManager,
                                      ReportExporter reportExporter,
                                      CsvFileHandler csvFileHandler,
                                      JsonFileHandler jsonFileHandler) {
    }

    private static void openAdminFlow(Scanner sc,
                                      AdminMenu adminMenu,
                                      ConfigManager configManager) {
    }

}

class FareCalculator {

    public BigDecimal discountedFare(int fromZone, int toZone,
                                     CityRideDataset.TimeBand band,
                                     CityRideDataset.PassengerType type) {

        BigDecimal result = new BigDecimal("0.00");


        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, band);


        if (baseFare != null) {


            BigDecimal discountRate = CityRideDataset.DISCOUNT_RATE.get(type);


            BigDecimal discountAmount = baseFare.multiply(discountRate);


            BigDecimal discounted = baseFare.subtract(discountAmount);

            result = money(discounted);
        }

        return result;
    }

    public BigDecimal applyCap(BigDecimal runningTotal, BigDecimal discountedFare,
                               CityRideDataset.PassengerType type) {

        BigDecimal result;

        BigDecimal cap = CityRideDataset.DAILY_CAP.get(type);


        if (runningTotal.compareTo(cap) >= 0) {
            result = money("0.00");
        } else {

            BigDecimal remaining = cap.subtract(runningTotal);


            if (discountedFare.compareTo(remaining) > 0) {
                result = money(remaining);
            } else {
                result = money(discountedFare);
            }
        }

        return result;
    }


    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(String s) {
        BigDecimal value = new BigDecimal(s);
        return money(value);
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

    public void recalculateChargedFaresForDay(LocalDate date) {
    }

    // return true if journey was added, false if not added
    public boolean addJourney(LocalDate date, int fromZone, int toZone, CityRideDataset.TimeBand band, CityRideDataset.PassengerType type) {

        boolean added = false;

        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {

            BigDecimal discountedFare = calc.discountedFare(fromZone, toZone, band, type);

            BigDecimal runningTotal = getTotalChargedForDay(date, type);

            BigDecimal chargedFare = calc.applyCap(runningTotal, discountedFare, type);

            int id = nextID; //generating a unique id for every journey here!
            nextID++;

            Journey newJourney = new Journey(date, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare); //adding the generated id to the journey

            journeys.add(newJourney);

            added = true;
        }


        return added;
    }

    public boolean removeJourneyById(int id) {
        // Removing the journey by id not by index as I did before.

        boolean removed = false;

        int i = 0;
        while (i < journeys.size() && !removed) {

            Journey j = journeys.get(i);

            if (j.getId() == id) {
                journeys.remove(i);
                removed = true;
            } else {
                i++;
            }
        }

        return removed;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                total = total.add(j.getChargedFare());
            }

            i++;
        }

        return total;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {

        BigDecimal total = new BigDecimal("0.00");

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
}

class RiderProfile {

    enum PaymentOption {
        CARD,
        CASH
    }

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private PaymentOption defaultPaymentOption;

    public RiderProfile() {
    }

    public RiderProfile(String name,
                        CityRideDataset.PassengerType passengerType,
                        PaymentOption defaultPaymentOption) {
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
}

class ProfileManager {

    private RiderProfile currentProfile;

    public RiderProfile createProfile(String name,
                                      CityRideDataset.PassengerType passengerType,
                                      RiderProfile.PaymentOption paymentOption) {

        RiderProfile profile = new RiderProfile(name, passengerType, paymentOption);
        currentProfile = profile;

        return profile;
    }

    public RiderProfile loadProfile(String filePath, JsonFileHandler jsonFileHandler) {
        return null;
    }

    public boolean saveProfile(String filePath, JsonFileHandler jsonFileHandler) {
        return false;
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

    public void showMenu(Scanner sc,
                         JourneyManager manager,
                         SummaryReport summaryReport,
                         ProfileManager profileManager,
                         ReportExporter reportExporter,
                         CsvFileHandler csvFileHandler,
                         JsonFileHandler jsonFileHandler) {
    }

    private void createProfileUI(Scanner sc, ProfileManager profileManager) {
    }

    private void loadProfileUI(Scanner sc, ProfileManager profileManager, JsonFileHandler jsonFileHandler) {
    }

    private void saveProfileUI(ProfileManager profileManager, JsonFileHandler jsonFileHandler) {
    }

    private void addJourneyUI(Scanner sc, JourneyManager manager, ProfileManager profileManager) {
    }

    private void editJourneyUI(Scanner sc, JourneyManager manager, ProfileManager profileManager) {
    }

    private void deleteJourneyUI(Scanner sc, JourneyManager manager) {
    }

    private void listJourneysUI(JourneyManager manager) {
    }

    private void showSummaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport) {
    }

    private void importJourneysUI(Scanner sc, JourneyManager manager, CsvFileHandler csvFileHandler) {
    }

    private void exportJourneysUI(Scanner sc, JourneyManager manager, CsvFileHandler csvFileHandler) {
    }

    private void exportSummaryUI(Scanner sc,
                                 JourneyManager manager,
                                 SummaryReport summaryReport,
                                 ReportExporter reportExporter,
                                 ProfileManager profileManager) {
    }

    private void saveCurrentDayStateUI(ProfileManager profileManager,
                                       JourneyManager manager,
                                       JsonFileHandler jsonFileHandler) {
    }
}

class ConfigManager {

    public SystemConfig loadConfig(JsonFileHandler jsonFileHandler) {
        return null;
    }

    public SystemConfig createDefaultConfig() {
        return null;
    }

    public boolean saveConfig(SystemConfig config, JsonFileHandler jsonFileHandler) {
        return false;
    }

    public SystemConfig getCurrentConfig() {
        return null;
    }

    public void setCurrentConfig(SystemConfig config) {
    }

    public void updateBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
    }

    public void updateDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
    }

    public void updateDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
    }

    public void updatePeakWindow(String peakStart, String peakEnd) {
    }
}

class CsvFileHandler {

    public List<Journey> importJourneys(String filePath) {
        return new ArrayList<>();
    }

    public boolean exportJourneys(String filePath, List<Journey> journeys) {
        return false;
    }

    public boolean exportLineItems(String filePath, List<Journey> journeys) {
        return false;
    }
}

class JsonFileHandler {

    public RiderProfile loadProfile(String filePath) {
        return null;
    }

    public boolean saveProfile(String filePath, RiderProfile profile) {
        return false;
    }

    public SystemConfig loadConfig(String filePath) {
        return null;
    }

    public boolean saveConfig(String filePath, SystemConfig config) {
        return false;
    }
}

class SystemConfig {

    public BigDecimal getBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band) {
        return null;
    }

    public void setBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
    }

    public BigDecimal getDiscount(CityRideDataset.PassengerType type) {
        return null;
    }

    public void setDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
    }

    public BigDecimal getDailyCap(CityRideDataset.PassengerType type) {
        return null;
    }

    public void setDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
    }

    public String getPeakStart() {
        return null;
    }

    public String getPeakEnd() {
        return null;
    }

    public void setPeakWindow(String peakStart, String peakEnd) {
    }
}

class ReportExporter {

    public boolean exportSummaryAsText(String filePath,
                                       String riderName,
                                       LocalDate date,
                                       SummaryReport summaryReport,
                                       JourneyManager manager) {

        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            writer.write("Rider: " + riderName);
            writer.newLine();
            writer.newLine();
            writer.write(summaryReport.buildSummaryText(manager, date));
            writer.close();

            success = true;

        } catch (IOException ex) {
            System.out.println("Error writing summary text file.");
        }

        return success;
    }
}

class AdminMenu {

    public void showMenu(Scanner sc, ConfigManager configManager) {
    }

    private boolean loginUI(Scanner sc) {
        return false;
    }

    private void viewConfigUI(ConfigManager configManager) {
    }

    private void updateBaseFareUI(Scanner sc, ConfigManager configManager) {
    }

    private void updateDiscountUI(Scanner sc, ConfigManager configManager) {
    }

    private void updateDailyCapUI(Scanner sc, ConfigManager configManager) {
    }

    private void updatePeakWindowUI(Scanner sc, ConfigManager configManager) {
    }
}

class SummaryReport {

    public void printSummary(JourneyManager manager, LocalDate date) {

        List<Journey> list = manager.getJourneys();

        int totalJourneys = 0;
        BigDecimal totalCharged = new BigDecimal("0.00");

        int mostExpensiveId = -1;
        BigDecimal mostExpensiveFare = new BigDecimal("0.00");

        int peakCount = 0;
        int offPeakCount = 0;

        int[][] zonePairCounts = new int[6][6]; // 1..5 used

        int i = 0;
        while (i < list.size()) {

            Journey j = list.get(i);

            if (j.getDate().equals(date)) {

                totalJourneys++;

                BigDecimal charged = j.getChargedFare();
                totalCharged = totalCharged.add(charged);

                if (mostExpensiveId == -1 || charged.compareTo(mostExpensiveFare) > 0) {
                    mostExpensiveFare = charged;
                    mostExpensiveId = j.getId();
                }

                if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                    peakCount++;
                } else {
                    offPeakCount++;
                }

                int from = j.getFromZone();
                int to = j.getToZone();
                if (from >= 1 && from <= 5 && to >= 1 && to <= 5) {
                    zonePairCounts[from][to] = zonePairCounts[from][to] + 1;
                }
            }

            i++;
        }

        System.out.println("\n Daily Summary (" + date + ")");
        System.out.println("Total number of journeys: " + totalJourneys);
        System.out.println("Total cost charged: " + totalCharged.setScale(2, RoundingMode.HALF_UP));

        BigDecimal average = new BigDecimal("0.00");
        if (totalJourneys > 0) {
            average = totalCharged.divide(new BigDecimal(totalJourneys), 2, RoundingMode.HALF_UP);
        }
        System.out.println("Average cost per journey: " + average);

        if (mostExpensiveId == -1) {
            System.out.println("Most expensive journey: ");
        } else {
            System.out.println("Most expensive journey: ID=" + mostExpensiveId + " | charged=" +
                    mostExpensiveFare.setScale(2, RoundingMode.HALF_UP));
        }

        System.out.println("\n--- Category Counts (" + date + ") ---");
        System.out.println("Peak journeys: " + peakCount);
        System.out.println("Off-peak journeys: " + offPeakCount);

        System.out.println("\nZone pair counts:");
        int from = 1;
        while (from <= 5) {
            int to = 1;
            while (to <= 5) {
                int c = zonePairCounts[from][to];
                if (c > 0) {
                    System.out.println(from + "->" + to + ": " + c);
                }
                to++;
            }
            from++;
        }
    }

    public BigDecimal calculateSavings(JourneyManager manager, LocalDate date) {

        BigDecimal savings = new BigDecimal("0.00");

        List<Journey> list = manager.getJourneys();

        int i = 0;
        while (i < list.size()) {

            Journey currentJourney = list.get(i);

            if (currentJourney.getDate().equals(date)) {

                BigDecimal discountedFare = currentJourney.getDiscountedFare();
                BigDecimal chargedFare = currentJourney.getChargedFare();

                BigDecimal journeySaving = discountedFare.subtract(chargedFare);
                savings = savings.add(journeySaving);
            }

            i++;
        }

        return savings.setScale(2, RoundingMode.HALF_UP);
    }

    public String buildSummaryText(JourneyManager manager, LocalDate date) {

        List<Journey> list = manager.getJourneys();

        int totalJourneys = 0;
        BigDecimal totalCharged = new BigDecimal("0.00");
        int mostExpensiveId = -1;
        BigDecimal mostExpensiveFare = new BigDecimal("0.00");
        int peakCount = 0;
        int offPeakCount = 0;

        int i = 0;
        while (i < list.size()) {

            Journey currentJourney = list.get(i);

            if (currentJourney.getDate().equals(date)) {

                totalJourneys++;
                totalCharged = totalCharged.add(currentJourney.getChargedFare());

                if (mostExpensiveId == -1 ||
                        currentJourney.getChargedFare().compareTo(mostExpensiveFare) > 0) {
                    mostExpensiveFare = currentJourney.getChargedFare();
                    mostExpensiveId = currentJourney.getId();
                }

                if (currentJourney.getBand() == CityRideDataset.TimeBand.PEAK) {
                    peakCount++;
                } else {
                    offPeakCount++;
                }
            }

            i++;
        }

        BigDecimal average = new BigDecimal("0.00");
        if (totalJourneys > 0) {
            average = totalCharged.divide(new BigDecimal(totalJourneys), 2, RoundingMode.HALF_UP);
        }

        BigDecimal savings = calculateSavings(manager, date);

        String text = "";
        text = text + "CityRide Lite Daily Summary\n";
        text = text + "Date: " + date + "\n";
        text = text + "Total journeys: " + totalJourneys + "\n";
        text = text + "Total charged: " + totalCharged.setScale(2, RoundingMode.HALF_UP) + "\n";
        text = text + "Average cost per journey: " + average + "\n";

        if (mostExpensiveId == -1) {
            text = text + "Most expensive journey: none\n";
        } else {
            text = text + "Most expensive journey: ID=" + mostExpensiveId +
                    " | charged=" + mostExpensiveFare.setScale(2, RoundingMode.HALF_UP) + "\n";
        }

        text = text + "Savings from cap: " + savings + "\n";
        text = text + "Peak journeys: " + peakCount + "\n";
        text = text + "Off-peak journeys: " + offPeakCount + "\n";

        return text;
    }
}

class Journey {
    private LocalDate date;
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

    public Journey(LocalDate date, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.date = date;
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
        return date;
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

    public String toString() {
        return "ID=" + id + " | " + date + " | " + type + " | " + band + " | "
                + fromZone + "->" + toZone
                + " | zonesCrossed=" + zonesCrossed
                + " | base=" + baseFare
                + " | discount=" + discountApplied
                + " | discounted=" + discountedFare
                + " | charged=" + chargedFare;
    }
    public void setDate(LocalDate date) {
    }

    public void setFromZone(int fromZone) {
    }

    public void setToZone(int toZone) {
    }

    public void setBand(CityRideDataset.TimeBand band) {
    }

    public void setType(CityRideDataset.PassengerType type) {
    }

    public void setChargedFare(BigDecimal chargedFare) {
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
```

---

### Updated Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Updated%20gant%20char%202.png)

------------------------------------------------------------------------------------------------------------------------------

### Diary Entries (at least 4)

------------------------------------------------------------------------------------------------------------------------------

**(I have sent you coloborators invitation for the git hub repository becouse I have created new one for the part 2. Please accept it and the code commits history can be seen in the git hub in src folder. Thank you!)**

#### **03.04.2026:  Adapting Part 1 code to Part 2.**

I started today by going back through my Part 1 code and comparing it properly with the Part 2 requirements. The first thing I realised was that I could not just reuse everything as it was, because even though this is the same CityRide Lite program, Part 2 has quite a lot of new features that need different logic and methods. In Part 1, the main focus was on adding journeys, fare calculation, summaries, and similar core logic, but in Part 2 the system is supposed to become more realistic by adding rider profiles, file handling, and a separate admin side.

Because of that, I decided to adapt the Part 1 code into Part 2 instead of starting again from zero. The new requirements need different logic, and some of the methods from Part 1 do not really belong in the same places anymore, so I had to rethink the structure and decide which class each method should belong to. At the beginning I also thought about separating everything into different Java files, but it quickly became a headache to move parts around while I was still in the planning stage. In the end I decided to keep everything together for now, because it makes this stage easier to manage. What I mainly did today was create a draft skeleton of the methods, place them into the most suitable classes, and name them clearly, but I did not  implement  ane new Part 2 methods yet.

Another difficulty today was deciding what should stay from Part 1 and what should be removed. Some methods looked useful at first, but when I compared them properly to the new requirements, I realised that they either needed different logic or were no longer suitable in their current form. Some classes already had a useful structure, but other parts clearly needed to be expanded. By the end of the session I had adapted the old code into a more suitable draft for Part 2 and created a clearer starting point for the new features.

#### **04.04.2026: Conducting the research.**

Today I focused on research to support the coding side of the project. I did not want to start writing the file-handling parts blindly because Part 2 requires JSON for profiles and config, CSV for journeys and reports, and also date and time handling for journeys. Because of that I decided to look for sources that were directly useful for the actual coding problems which I will face.

The first source I researched was JSON in core Java. This was important because the rider profile and the configuration both need to be saved between sessions, but Java does not give simple built-in way to do that and have idea of how the text is written. After that I looked at CSV reading and writing, because importing journeys and exporting reports are requirements in Part 2. I also researched LocalDateTime because Part 2 requires date and time together, while my Part 1 logic was originally had LocalDate only.

What I found today will be helpfull later becouse these researches  were not just random reading. Each source gave me something specific that I could reuse in the program. For example, the CSV research helped me think about skipping the header row and keeping the logic simple, while the LocalDateTime research helped me think about how to add time without destroying the existing date-based fare logic. By the end of the session I had three relevant sources, added screenshots for them, and linked each one to a coding decision I plan to use in the program.

#### 05.04.2026: Refining the draft structure and preparing the first methods

Today I continued from the skeleton draft I made earlier and spent more time refining the overall code structure before implementing more of the Part 2 features. At this stage I still felt that jumping straight into the harder parts like full JSON and CSV handling would not be the best idea, because too many other parts of the program were still only placeholders. Because of that I decided to focus first on making the draft more organised and preparing the easier methods that could be added without breaking the existing Part 1 logic.

A lot of today was spent checking whether the responsibilities of the classes made sense. I went through classes like JourneyManager, SummaryReport, ProfileManager, ReportExporter and the menu classes, and thought more carefully about which methods should actually belong to each one. That part took longer than I expected because some methods looked simple at first, but if they were placed in the wrong class it would make the code more confusing later.

I also started identifying which empty methods from the draft would be the easiest ones to implement first. My aim was to identify what classes have dependencies on each other, and then choose a few smaller methods that would be easiest to implment. This felt like a better approach than rushing into the harder features and ending up with incomplete or messy code. By the end of the session I had a more stable draft structure, clearer method placement, and a better idea of which functions I could begin implementing first.

#### **06.04.2026: Started implementing some functions.**

Today I moved on from structure and planning into actual implementation. Because I was close to the Milestone 2 deadline, I made a  decision to focus on just two methods: buildSummaryText() in SummaryReport and exportSummaryAsText() in ReportExporter. I thought this was the better approach because it is more useful to show a small number of finished and understandable methods than a larger amount of unfinished code that I would not be able to explain properly.

The two methods I worked on today were both connected to the reporting side of Part 2. In SummaryReport, I implemented buildSummaryText(), which creates a readable daily summary using the journey data already stored in the program. It includes things like the total number of journeys, total charged, average cost, most expensive journey, savings from the cap, and peak/off-peak counts. After that, in ReportExporter, I implemented exportSummaryAsText(), which takes that summary text and writes it into a text file. I chose these methods because they were simple enough to complete in the time I had left, but they still match the Part 2 requirement for human-readable report output.

One issue I ran into today was that after adding the new methods, I still had some of the original empty draft methods left in the code. This caused duplicate method errors, so I had to go back through the classes and remove the unused stub versions while keeping only the implemented ones. Even though this was only a small problem, it showed me that working from a draft skeleton can create its own mistakes.

------------------------------------------------------------------------------------------------------------------------------
