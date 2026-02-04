public class ServiceProvider {
    public String id;
    public String name;
    public String category;
    public String pincode;
    public String phone;
    public boolean isApproved;
    public boolean isAvailable;
    public long approvalTime;

    public String password;
    public String experience;

    public String availableFrom;
    public String availableTo;

    // Required for Firestore
    public ServiceProvider() {}

    public ServiceProvider(String id, String name, String category, String pincode, String phone, String password,
            String experience) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.pincode = pincode;
        this.phone = phone;
        this.password = password;
        this.experience = experience;
        this.isApproved = false; // Default false
        this.isAvailable = true; // Default true
        this.approvalTime = 0;
        this.availableFrom = "09:00"; // Default
        this.availableTo = "18:00"; // Default
    }

    public ServiceProvider(String name, String category, String pincode, String phone, String password,
            String experience) {
        this(null, name, category, pincode, phone, password, experience);
    }
}
