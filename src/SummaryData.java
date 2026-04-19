import java.math.BigDecimal;

// A simple data holder used by SummaryReport to carry all summary values
// through the calculation process in one object.
// It exists as a separate class so that calculateSummaryData() can return
// multiple values cleanly without needing ten separate method calls.


class SummaryData {

    private int totalJourneys;
    private int mostExpensiveId;
    private int peakCount;
    private int offPeakCount;
    private int[][] zonePairCounts;
    private int[] zoneCounts;
    private BigDecimal average;
    private BigDecimal savings;
    private BigDecimal totalCharged;
    private BigDecimal mostExpensiveFare;

    SummaryData() {
        totalJourneys = 0;
        mostExpensiveId = -1;
        peakCount = 0;
        offPeakCount = 0;
        zonePairCounts = new int[CityRideDataset.MAX_ZONE + 1][CityRideDataset.MAX_ZONE + 1];
        zoneCounts = new int[CityRideDataset.MAX_ZONE + 1];
        average = BigDecimal.ZERO;
        savings = BigDecimal.ZERO;
        totalCharged = BigDecimal.ZERO;
        mostExpensiveFare = BigDecimal.ZERO;
    }

    public int getTotalJourneys() { return totalJourneys; }
    public int getMostExpensiveId() { return mostExpensiveId; }
    public int getPeakCount() { return peakCount; }
    public int getOffPeakCount() { return offPeakCount; }
    public int[][] getZonePairCounts() { return zonePairCounts; }
    public int[] getZoneCounts() { return zoneCounts; }
    public BigDecimal getAverage() { return average; }
    public BigDecimal getSavings() { return savings; }
    public BigDecimal getTotalCharged() { return totalCharged; }
    public BigDecimal getMostExpensiveFare() { return mostExpensiveFare; }

    public void setTotalJourneys(int totalJourneys) { this.totalJourneys = totalJourneys; }
    public void setMostExpensiveId(int mostExpensiveId) { this.mostExpensiveId = mostExpensiveId; }
    public void setPeakCount(int peakCount) { this.peakCount = peakCount; }
    public void setOffPeakCount(int offPeakCount) { this.offPeakCount = offPeakCount; }
    public void setAverage(BigDecimal average) { this.average = average; }
    public void setSavings(BigDecimal savings) { this.savings = savings; }
    public void setTotalCharged(BigDecimal totalCharged) { this.totalCharged = totalCharged; }
    public void setMostExpensiveFare(BigDecimal mostExpensiveFare) { this.mostExpensiveFare = mostExpensiveFare; }
}