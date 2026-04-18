import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public CityRideDataset.TimeBand determineBand(LocalDateTime dateTime, SystemConfig config) {
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.OFF_PEAK;

        LocalTime journeyTime = dateTime.toLocalTime();
        LocalTime peakStart = LocalTime.parse(config.getPeakStart());
        LocalTime peakEnd = LocalTime.parse(config.getPeakEnd());

        if (!journeyTime.isBefore(peakStart) && journeyTime.isBefore(peakEnd)) {
            band = CityRideDataset.TimeBand.PEAK;
        }

        return band;
    }

    public boolean saveConfig(SystemConfig config, JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveConfig("config.json", config);
    }

    public SystemConfig getCurrentConfig() {
        return currentConfig;
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