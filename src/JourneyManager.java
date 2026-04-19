import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Stores and manages all journeys for the current session.
// It handles adding, editing and deleting journeys, and recalculates
// the daily cap across all journeys whenever one is changed or removed.
// FareCalculator is used here rather than in the menu classes
// to keep all fare logic in one place.

class JourneyManager {

    private List<Journey> journeys;
    private FareCalculator calc;
    private int nextID;

    public JourneyManager() {
        journeys = new ArrayList<>();
        calc = new FareCalculator();
        nextID = 1;
    }

    // Returns all journeys that match the given date
    public List<Journey> getJourneysForDate(LocalDate date) {
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < journeys.size()) {
            if (journeys.get(i).getDate().equals(date)) {
                result.add(journeys.get(i));
            }
            i++;
        }

        return result;
    }

    // Filters journeys by date and passenger type
    // Reuses getJourneysForDate to avoid duplicating the date filter loop
    private List<Journey> getJourneysForDateAndType(LocalDate date, CityRideDataset.PassengerType type) {
        List<Journey> byDate = getJourneysForDate(date);
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < byDate.size()) {
            if (byDate.get(i).getType() == type) {
                result.add(byDate.get(i));
            }
            i++;
        }

        return result;
    }

    // Reapplies the daily cap to all journeys for a given date and passenger type
    // Journeys are processed in list order so chronological sorting should happen before calling this
    public void recalculateChargedFaresForDay(LocalDate date, CityRideDataset.PassengerType type, SystemConfig config) {
        List<Journey> dayJourneys = getJourneysForDateAndType(date, type);
        BigDecimal runningTotal = BigDecimal.ZERO;

        int i = 0;
        while (i < dayJourneys.size()) {
            Journey j = dayJourneys.get(i);
            BigDecimal newCharged = calc.applyCap(config, runningTotal, j.getDiscountedFare(), j.getType());
            j.setChargedFare(newCharged);
            runningTotal = runningTotal.add(newCharged);
            i++;
        }
    }

    // Adds a new journey if the zone/band fare exists in the config
    public boolean addJourney(LocalDateTime dateTime, int fromZone, int toZone,
                              CityRideDataset.TimeBand band, CityRideDataset.PassengerType type,
                              SystemConfig config) {
        boolean added = false;
        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, fromZone, toZone, band, type);
            BigDecimal runningTotal = getTotalChargedForDay(dateTime.toLocalDate(), type);
            BigDecimal chargedFare = calc.applyCap(config, runningTotal, discountedFare, type);

            int id = nextID;
            nextID++;

            Journey newJourney = new Journey(dateTime, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare);

            journeys.add(newJourney);
            added = true;
        }

        return added;
    }

    // Removes a journey by ID and recalculates the day's caps after removal
    public boolean removeJourneyById(int id, SystemConfig config) {
        boolean removed = false;
        LocalDate removedDate = null;
        CityRideDataset.PassengerType removedType = null;

        int i = 0;
        while (i < journeys.size() && !removed) {
            Journey j = journeys.get(i);

            if (j.getId() == id) {
                removedDate = j.getDate();
                removedType = j.getType();
                journeys.remove(i);
                removed = true;
            }
            else {
                i++;
            }
        }

        if (removed) {
            recalculateChargedFaresForDay(removedDate, removedType, config);
        }

        return removed;
    }

    // Returns the total charged fare for a specific date and passenger type
    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {
        List<Journey> dayJourneys = getJourneysForDateAndType(date, type);
        BigDecimal total = BigDecimal.ZERO;

        int i = 0;
        while (i < dayJourneys.size()) {
            total = total.add(dayJourneys.get(i).getChargedFare());
            i++;
        }

        return total;
    }

    // Recalculates base fare, discount, and discount amount for a single journey
    public void recalculateFaresForJourney(Journey j, SystemConfig config) {
        BigDecimal baseFare = config.getBaseFare(j.getFromZone(), j.getToZone(), j.getBand());

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, j.getFromZone(), j.getToZone(), j.getBand(), j.getType());
            j.setBaseFare(baseFare);
            j.setDiscountedFare(discountedFare);
            j.setDiscountApplied(MoneyUtil.money(baseFare.subtract(discountedFare)));
        }
    }

    // Replaces all journeys with a new list and resets the ID counter
    public void setJourneys(List<Journey> newJourneys) {
        journeys.clear();
        nextID = 1;

        int i = 0;
        while (i < newJourneys.size()) {
            Journey journey = newJourneys.get(i);
            journeys.add(journey);

            if (journey.getId() >= nextID) {
                nextID = journey.getId() + 1;
            }

            i++;
        }
    }

    // Removes all journeys and resets the ID counter
    public void clearJourneys() {
        journeys.clear();
        nextID = 1;
    }
}