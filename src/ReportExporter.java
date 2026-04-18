import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

class ReportExporter {

    public boolean exportSummaryAsText(String filePath, String riderName,
                                       LocalDate date, SummaryReport summaryReport,
                                       JourneyManager manager) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Rider: " + riderName);
            writer.newLine();
            writer.write("Date: " + date);
            writer.newLine();
            writer.newLine();
            writer.write(summaryReport.buildSummaryText(manager, date));
            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export summary to " + filePath);
        }

        return success;
    }

    public boolean exportSummaryAsCsv(String filePath, String riderName,
                                      LocalDate date, JourneyManager manager) {
        boolean success = false;
        List<Journey> dayJourneys = manager.getJourneysForDate(date);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            // Report header info
            writer.write("Rider:," + riderName);
            writer.newLine();
            writer.write("Date:," + date);
            writer.newLine();
            writer.newLine();

            // Column headers
            writer.write("id,dateTime,fromZone,toZone,band,type,baseFare,discountApplied,discountedFare,chargedFare");
            writer.newLine();


            BigDecimal totalCharged = BigDecimal.ZERO;
            int i = 0;
            while (i < dayJourneys.size()) {
                Journey j = dayJourneys.get(i);
                writer.write(
                        j.getId() + "," +
                                j.getDateTime().format(fmt) + "," +
                                j.getFromZone() + "," +
                                j.getToZone() + "," +
                                j.getBand().name() + "," +
                                j.getType().name() + "," +
                                j.getBaseFare() + "," +
                                j.getDiscountApplied() + "," +
                                j.getDiscountedFare() + "," +
                                j.getChargedFare()
                );
                writer.newLine();
                totalCharged = totalCharged.add(j.getChargedFare());
                i++;
            }

            writer.newLine();
            writer.write("Total journeys:," + dayJourneys.size());
            writer.newLine();
            writer.write("Total charged (GBP):," + MoneyUtil.money(totalCharged));
            writer.newLine();

            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export CSV summary to " + filePath);
        }

        return success;
    }
}