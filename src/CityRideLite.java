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
}

class ProfileManager{

}

class ConfigManager{
}

class CsvFileHandler{
}
class JsonFileHandler{
}

class SystemConfig {

}

class ReportExporter {

}

class AdminMenu{

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