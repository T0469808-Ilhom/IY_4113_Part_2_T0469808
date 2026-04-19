
// Manages the rider's profile for the current session.
// It handles creating a new profile, loading an existing one,
// and saving it back to file when the rider is done.
// The current profile is stored here so other classes can access
// the rider's name, passenger type and payment option.

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
