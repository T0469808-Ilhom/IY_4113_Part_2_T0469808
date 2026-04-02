import java.util.Scanner;
import java.util.List;
import java.time.LocalDate;
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
                    LocalDate summaryDate = readDateDayMonthYear(sc);
                    summaryReport.printSummary(manager, summaryDate);
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