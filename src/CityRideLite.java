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

    public static void main(String[] args) {

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

    private List<Journey> getJourneysForDate(LocalDate date) {
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < journeys.size()) {
            if (journeys.get(i).getDate().equals(date)) {
                result.add(journeys.get(i));
            }
            i++;
        }

        return result;
    }

    public void recalculateChargedFaresForDay(LocalDate date) {
        List<Journey> dayJourneys = getJourneysForDate(date);
        BigDecimal runningTotal = new BigDecimal("0.00");

        int i = 0;
        while (i < dayJourneys.size()) {
            Journey j = dayJourneys.get(i);
            BigDecimal newCharged = calc.applyCap(runningTotal, j.getDiscountedFare(), j.getType());
            j.setChargedFare(newCharged);
            runningTotal = runningTotal.add(newCharged);
            i++;
        }
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
//commit
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

        // Load default discounts from dataset
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            config.setDiscount(type, CityRideDataset.DISCOUNT_RATE.get(type));
            config.setDailyCap(type, CityRideDataset.DAILY_CAP.get(type));
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
        this.date = date;
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

    public void setChargedFare(BigDecimal chargedFare) {
        this.chargedFare = chargedFare;
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