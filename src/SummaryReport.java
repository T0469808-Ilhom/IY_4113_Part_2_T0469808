import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;

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