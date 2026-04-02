import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;

class Journey {
    private LocalDate date;
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

    public Journey(LocalDate date, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.date = date;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - fromZone) + 1;

        this.band = band;
        this.type = type;

        this.baseFare = baseFare;
        this.discountedFare = discountedFare;
        this.discountApplied = baseFare.subtract(discountedFare).setScale(2, RoundingMode.HALF_UP);

        this.chargedFare = chargedFare;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
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
    public void setChargedFare(BigDecimal chargedFare) {
        this.chargedFare = chargedFare;
    }

    public String toString() {
        return "ID=" + id + " | " + date + " | " + type + " | " + band + " | "
                + fromZone + "->" + toZone
                + " | zonesCrossed=" + zonesCrossed
                + " | base=" + baseFare
                + " | discount=" + discountApplied
                + " | discounted=" + discountedFare
                + " | charged=" + chargedFare;
    }
}