import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Builds and displays the end-of-day summary for a rider.
// It calculates totals, averages, the most expensive journey, cap savings,
// and counts journeys by time band and zone pair.
// The summary can be printed to the console or returned as a string
// so ReportExporter can write it to a file.

class SummaryReport {

    public void printSummary(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        System.out.println(buildSummaryText(data, date));
        printZonePairCounts(data.getZonePairCounts());
        printZoneCounts(data.getZoneCounts());
    }

    public String buildSummaryText(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        return buildSummaryText(data, date);
    }

    private String buildSummaryText(SummaryData data, LocalDate date) {
        String result = "";

        result = result + "=== CityRide Lite Daily Summary ===" + "\n";
        result = result + "Date: " + date + "\n";
        result = result + "Total journeys: " + data.getTotalJourneys() + "\n";
        result = result + "Total charged: GBP " + MoneyUtil.money(data.getTotalCharged()) + "\n";
        result = result + "Average cost per journey: GBP " + MoneyUtil.money(data.getAverage()) + "\n";

        if (data.getMostExpensiveId() == -1) {
            result = result + "Most expensive journey: none" + "\n";
        }
        else {
            result = result + "Most expensive journey: ID " + data.getMostExpensiveId()
                    + " (GBP " + MoneyUtil.money(data.getMostExpensiveFare()) + ")" + "\n";
        }

        result = result + "Savings from cap: GBP " + MoneyUtil.money(data.getSavings()) + "\n";

        if (data.getSavings().compareTo(BigDecimal.ZERO) > 0) {
            result = result + "Cap reached: Yes" + "\n";
        }
        else {
            result = result + "Cap reached: No" + "\n";
        }

        result = result + "Peak journeys: " + data.getPeakCount() + "\n";
        result = result + "Off-peak journeys: " + data.getOffPeakCount() + "\n";

        return result;
    }

    private SummaryData calculateSummaryData(JourneyManager manager, LocalDate date) {
        SummaryData data = new SummaryData();
        List<Journey> list = manager.getJourneysForDate(date);

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);

            data.setTotalJourneys(data.getTotalJourneys() + 1);
            data.setTotalCharged(data.getTotalCharged().add(j.getChargedFare()));

            if (data.getMostExpensiveId() == -1 || j.getChargedFare().compareTo(data.getMostExpensiveFare()) > 0) {
                data.setMostExpensiveFare(j.getChargedFare());
                data.setMostExpensiveId(j.getId());
            }

            if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                data.setPeakCount(data.getPeakCount() + 1);
            }
            else {
                data.setOffPeakCount(data.getOffPeakCount() + 1);
            }

            int fromZone = j.getFromZone();
            int toZone = j.getToZone();

            if (isValidZone(fromZone) && isValidZone(toZone)) {
                data.getZonePairCounts()[fromZone][toZone]++;
                data.getZoneCounts()[fromZone]++;

                if (fromZone != toZone) {
                    data.getZoneCounts()[toZone]++;
                }
            }

            i++;
        }

        if (data.getTotalJourneys() > 0) {
            data.setAverage(data.getTotalCharged().divide(new BigDecimal(data.getTotalJourneys()), 2, RoundingMode.HALF_UP)
            );
        }

        data.setSavings(calculateSavings(manager, date));

        return data;
    }

    private BigDecimal calculateSavings(JourneyManager manager, LocalDate date) {
        BigDecimal savings = BigDecimal.ZERO;
        List<Journey> list = manager.getJourneysForDate(date);

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);
            savings = savings.add(j.getDiscountedFare().subtract(j.getChargedFare()));
            i++;
        }

        return MoneyUtil.money(savings);
    }

    private void printZonePairCounts(int[][] zonePairCounts) {
        System.out.println("\nZone pair counts:");
        boolean hasAny = false;

        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = CityRideDataset.MIN_ZONE;
            while (to <= CityRideDataset.MAX_ZONE) {
                if (zonePairCounts[from][to] > 0) {
                    System.out.println(from + "->" + to + ": " + zonePairCounts[from][to]);
                    hasAny = true;
                }
                to++;
            }
            from++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    private void printZoneCounts(int[] zoneCounts) {
        System.out.println("\nZone counts:");
        boolean hasAny = false;

        int zone = CityRideDataset.MIN_ZONE;
        while (zone <= CityRideDataset.MAX_ZONE) {
            if (zoneCounts[zone] > 0) {
                System.out.println("Zone " + zone + ": " + zoneCounts[zone]);
                hasAny = true;
            }
            zone++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    private boolean isValidZone(int zone) {
        boolean valid = false;

        if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) {
            valid = true;
        }

        return valid;
    }
}