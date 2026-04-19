import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// A utility class that handles all user input from the console.
// Every method keeps asking until the user enters a valid value,
// so no other class needs to worry about validation or error handling.
// All methods are static because InputHelper holds no state of its own.


class InputHelper {

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
    public static LocalTime readTime(Scanner scanner, String prompt) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        boolean valid = false;
        LocalTime time = LocalTime.now();

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    time = LocalTime.parse(input, timeFormat);
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid time. Use HH:mm (e.g. 08:30).");
                }
            }
        }

        return time;
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
    public static LocalDate readDate(Scanner scanner, String prompt) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        boolean valid = false;
        LocalDate date = LocalDate.now();

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    date = LocalDate.parse(input, dateFormat);
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid date. Use DD-MM-YYYY (e.g. 22-04-2026).");
                }
            }
        }

        return date;
    }
}