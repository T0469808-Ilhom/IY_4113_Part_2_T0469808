import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Handles reading and writing journey data as CSV files.
// Importing reads an existing file and skips any rows that cannot be parsed,
// so a corrupt line does not stop the rest of the journeys from loading.
// Exporting writes the active day's journeys with a header row
// so the file can be opened in a spreadsheet if needed.


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