class ProfileManager {

    private RiderProfile currentProfile;

    // Creates a new profile with a generated unique ID
    public RiderProfile createProfile(String name,
                                      CityRideDataset.PassengerType passengerType,
                                      RiderProfile.PaymentOption paymentOption,
                                      JsonFileHandler jsonFileHandler) {
        String profileId = generateProfileId(jsonFileHandler);
        currentProfile = new RiderProfile(profileId, name, passengerType, paymentOption);
        return currentProfile;
    }

    private String generateProfileId(JsonFileHandler jsonFileHandler) {
        int count = jsonFileHandler.loadProfileCount();
        count++;
        jsonFileHandler.saveProfileCount(count);
        return "R" + count;
    }

    public RiderProfile loadProfile(String profileId, JsonFileHandler jsonFileHandler) {
        RiderProfile profile = jsonFileHandler.loadProfile(profileId + ".json");

        if (profile != null) {
            currentProfile = profile;
        }

        return profile;
    }

    public boolean saveProfile(JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveProfile(currentProfile.getProfileId() + ".json", currentProfile);
    }

    public RiderProfile getCurrentProfile() {

        return currentProfile;
    }

    public boolean hasCurrentProfile() {
        boolean hasProfile = false;

        if (currentProfile != null) {
            hasProfile = true;
        }

        return hasProfile;
    }
}