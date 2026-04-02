import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

class JourneyManager {

    private List<Journey> journeys;
    private FareCalculator calc;

    private int nextID;

    public JourneyManager() {
        journeys = new ArrayList<>();
        calc = new FareCalculator();
        nextID = 1;
    }

    // return true if journey was added, false if not added
    public boolean addJourney(LocalDate date, int fromZone, int toZone, CityRideDataset.TimeBand band, CityRideDataset.PassengerType type) {

        boolean added = false;

        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {

            BigDecimal discountedFare = calc.discountedFare(fromZone, toZone, band, type);

            BigDecimal runningTotal = getTotalChargedForDay(date, type);

            BigDecimal chargedFare = calc.applyCap(runningTotal, discountedFare, type);

            int id = nextID; //generating a unique id for every journey here!
            nextID++;

            Journey newJourney = new Journey(date, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare); //adding the generated id to the journey

            journeys.add(newJourney);

            added = true;
        }

        return added;
    }

    public boolean removeJourneyById(int id) {
        // Removing the journey by id not by index as I did before.

        boolean removed = false;
        LocalDate affectedDate = null;

        int i = 0;
        while (i < journeys.size() && !removed) {

            Journey j = journeys.get(i);

            if (j.getId() == id) {
                affectedDate = j.getDate();
                journeys.remove(i);
                removed = true;
            } else {
                i++;
            }
        }

        if (removed && affectedDate != null) {
            recalculateChargedFaresForDay(affectedDate);
        }

        return removed;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                total = total.add(j.getChargedFare());
            }

            i++;
        }

        return total;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                if (j.getType() == type) {
                    total = total.add(j.getChargedFare());
                }
            }

            i++;
        }

        return total;
    }
    private void recalculateChargedFaresForDay(LocalDate date) {

        int i = 0;
        while (i < journeys.size()) {

            Journey currentJourney = journeys.get(i);

            if (currentJourney.getDate().equals(date)) {
                BigDecimal runningTotal = getRunningTotalBeforeId(date, currentJourney.getType(), currentJourney.getId());
                BigDecimal newChargedFare = calc.applyCap(runningTotal, currentJourney.getDiscountedFare(), currentJourney.getType());
                currentJourney.setChargedFare(newChargedFare);
            }

            i++;
        }
    }
    private BigDecimal getRunningTotalBeforeId(LocalDate date, CityRideDataset.PassengerType type, int currentId) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date) && j.getType() == type && j.getId() < currentId) {
                total = total.add(j.getChargedFare());
            }

            i++;
        }

        return total;
    }


}