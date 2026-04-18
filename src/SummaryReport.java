import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

class SummaryReport {

    // Holds all calculated values for one day's summary
    private class SummaryData {
        int totalJourneys = 0;
        int mostExpensiveId = -1;
        int peakCount = 0;
        int offPeakCount = 0;
        int[][] zonePairCounts = new int[CityRideDataset.MAX_ZONE + 1][CityRideDataset.MAX_ZONE + 1];
        int[] zoneCounts = new int[CityRideDataset.MAX_ZONE + 1];
        BigDecimal average = BigDecimal.ZERO;
        BigDecimal savings = BigDecimal.ZERO;
        BigDecimal totalCharged = BigDecimal.ZERO;
        BigDecimal mostExpensiveFare = BigDecimal.ZERO;
    }

    private SummaryData calculateSummaryData(JourneyManager manager, LocalDate date) {
        SummaryData data = new SummaryData();
        // Uses getJourneysForDate so no need to manually filter by date inside the loop
        List<Journey> list = manager.getJourneysForDate(date);

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);

            data.totalJourneys++;
            data.totalCharged = data.totalCharged.add(j.getChargedFare());

            if (data.mostExpensiveId == -1 || j.getChargedFare().compareTo(data.mostExpensiveFare) > 0) {
                data.mostExpensiveFare = j.getChargedFare();
                data.mostExpensiveId = j.getId();
            }

            if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                data.peakCount++;
            }
            else {
                data.offPeakCount++;
            }

            int fromZone = j.getFromZone();
            int toZone = j.getToZone();

            if (isValidZone(fromZone) && isValidZone(toZone)) {
                data.zonePairCounts[fromZone][toZone]++;
                data.zoneCounts[fromZone]++;

                if (fromZone != toZone) {
                    data.zoneCounts[toZone]++;
                }
            }

            i++;
        }

        if (data.totalJourneys > 0) {
            data.average = data.totalCharged.divide(new BigDecimal(data.totalJourneys), 2, RoundingMode.HALF_UP);
        }

        data.savings = calculateSavings(manager, date);

        return data;
    }

    public void printSummary(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        System.out.println(buildSummaryText(data, date));
        printZonePairCounts(data.zonePairCounts);
        printZoneCounts(data.zoneCounts);
    }

    public String buildSummaryText(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        return buildSummaryText(data, date);
    }

    private String buildSummaryText(SummaryData data, LocalDate date) {
        String result = "";

        result = result + "=== CityRide Lite Daily Summary ===" + "\n";
        result = result + "Date: " + date + "\n";
        result = result + "Total journeys: " + data.totalJourneys + "\n";
        result = result + "Total charged: GBP " + MoneyUtil.money(data.totalCharged) + "\n";
        result = result + "Average cost per journey: GBP " + MoneyUtil.money(data.average) + "\n";

        if (data.mostExpensiveId == -1) {
            result = result + "Most expensive journey: none" + "\n";
        }
        else {
            result = result + "Most expensive journey: ID " + data.mostExpensiveId
                    + " (GBP " + MoneyUtil.money(data.mostExpensiveFare) + ")" + "\n";
        }

        result = result + "Savings from cap: GBP " + MoneyUtil.money(data.savings) + "\n";

        if (data.savings.compareTo(BigDecimal.ZERO) > 0) {
            result = result + "Cap reached: Yes" + "\n";
        }
        else {
            result = result + "Cap reached: No" + "\n";
        }

        result = result + "Peak journeys: " + data.peakCount + "\n";
        result = result + "Off-peak journeys: " + data.offPeakCount + "\n";

        return result;
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