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