public class User {
    public String id;
    public String name;
    public String phone;
    public String pincode;
    public String password;

    // Required for Firestore
    public User() {}

    public User(String id, String name, String phone, String pincode, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.pincode = pincode;
        this.password = password;
    }

    public User(String name, String phone, String pincode, String password) {
        this.name = name;
        this.phone = phone;
        this.pincode = pincode;
        this.password = password;
    }
}
