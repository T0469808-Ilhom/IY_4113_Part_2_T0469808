// Represents a single rider's profile with their personal details.
// It stores the profile ID, name, passenger type and default payment option.
// The profile ID is generated once on creation and never changes,
// so the system can always find the right files for that rider.

class RiderProfile {

    enum PaymentOption {
        CARD,
        CASH
    }

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private PaymentOption defaultPaymentOption;
    private String profileId;

    public RiderProfile(String profileId, String name,
                        CityRideDataset.PassengerType passengerType,
                        PaymentOption defaultPaymentOption) {
        this.profileId = profileId;
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPaymentOption = defaultPaymentOption;
    }

    public String getName() {
        return name;
    }

    public CityRideDataset.PassengerType getPassengerType() {
        return passengerType;
    }

    public PaymentOption getDefaultPaymentOption() {
        return defaultPaymentOption;
    }

    public String getProfileId() {
        return profileId;
    }

}