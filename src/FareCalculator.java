import java.math.BigDecimal;

class FareCalculator {

    public BigDecimal discountedFare(SystemConfig config, int fromZone, int toZone,
                                     CityRideDataset.TimeBand band,
                                     CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountRate = config.getDiscount(type);

            if (discountRate != null) {
                BigDecimal discountAmount = baseFare.multiply(discountRate);
                BigDecimal discounted = baseFare.subtract(discountAmount);
                result = MoneyUtil.money(discounted);
            }
        }

        return result;
    }

    public BigDecimal applyCap(SystemConfig config, BigDecimal runningTotal,
                               BigDecimal discountedFare,
                               CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal cap = config.getDailyCap(type);

        if (cap != null) {
            if (runningTotal.compareTo(cap) >= 0) {
                result = MoneyUtil.money(BigDecimal.ZERO);
            }
            else {
                BigDecimal remaining = cap.subtract(runningTotal);

                if (discountedFare.compareTo(remaining) > 0) {
                    result = MoneyUtil.money(remaining);
                }
                else {
                    result = MoneyUtil.money(discountedFare);
                }
            }
        }

        return result;
    }
}