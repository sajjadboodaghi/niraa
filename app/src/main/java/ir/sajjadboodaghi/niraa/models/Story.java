package ir.sajjadboodaghi.niraa.models;

/**
 * Created by Sajjad on 10/09/2018.
 */

public class Story {
    private int id;
    private String phoneNumber;
    private String link;
    private String phone;
    public Story(int id, String phoneNumber, String link, String phone) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.link = link;
        this.phone = phone;
    }
    public int getId() {
        return id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getLink() {
        return link;
    }
    public String getPhone() {return phone; }

}
