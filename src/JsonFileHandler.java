import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Reads and writes JSON files for profiles and system configuration.
// It uses a simple manual parser rather than an external library
// so the program has no dependencies outside of core Java.
// It also tracks how many profiles have been created using a counter file,
// which is how unique profile IDs are generated across sessions.

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