import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Journey {
    private LocalDateTime dateTime;
    private int fromZone;
    private int toZone;
    private int zonesCrossed;

    private CityRideDataset.TimeBand band;
    private CityRideDataset.PassengerType type;

    private BigDecimal baseFare;
    private BigDecimal discountedFare;
    private BigDecimal discountApplied;
    private BigDecimal chargedFare;

    private int id;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Journey(LocalDateTime dateTime, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.dateTime = dateTime;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - fromZone) + 1;
        this.band = band;
        this.type = type;
        this.baseFare = baseFare;
        this.discountedFare = discountedFare;
        this.discountApplied = MoneyUtil.money(baseFare.subtract(discountedFare));
        this.chargedFare = chargedFare;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public CityRideDataset.PassengerType getType() {
        return type;
    }

    public CityRideDataset.TimeBand getBand() {
        return band;
    }

    public int getFromZone() {
        return fromZone;
    }

    public int getToZone() {
        return toZone;
    }

    public int getZonesCrossed() {
        return zonesCrossed;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDiscountedFare() {
        return discountedFare;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public BigDecimal getChargedFare() {
        return chargedFare;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setFromZone(int fromZone) {
        this.fromZone = fromZone;
        this.zonesCrossed = Math.abs(this.toZone - fromZone) + 1;
    }

    public void setToZone(int toZone) {
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - this.fromZone) + 1;
    }

    public void setBand(CityRideDataset.TimeBand band) {
        this.band = band;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public void setDiscountedFare(BigDecimal discountedFare) {
        this.discountedFare = discountedFare;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public void setChargedFare(BigDecimal chargedFare) {
        this.chargedFare = chargedFare;
    }

    public String toString() {
        String capApplied = "No";
        if (chargedFare.compareTo(discountedFare) < 0) {
            capApplied = "Yes";
        }

        return "ID: " + id
                + " | " + dateTime.format(DATE_TIME_FORMAT)
                + " | " + type
                + " | " + band
                + " | " + fromZone + "->" + toZone
                + " | Zones crossed: " + zonesCrossed
                + " | Base: GBP " + baseFare
                + " | Discount: GBP " + discountApplied
                + " | Discounted: GBP " + discountedFare
                + " | Charged: GBP " + chargedFare
                + " | Cap applied: " + capApplied;
    }
}