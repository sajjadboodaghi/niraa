package ir.sajjadboodaghi.niraa.models;

/**
 * Created by Sajjad on 02/05/2018.
 */

public class Item {
    private int id;
    private String phoneNumber;
    private String telegramId;
    private String title;
    private String description;
    private String price;
    private String place;
    private String subcatName;
    private int subcatId;
    private String shamsi;
    private String timestamp;
    private int imageCount;
    private int verified;

    public Item(int id, String phoneNumber, String telegramId, String title, String description, String price, String place, String subcatName, int subcatId, String shamsi, String timestamp, int imageCount, int verified) {
        this.id          = id;
        this.phoneNumber = phoneNumber;
        this.telegramId  = telegramId;
        this.title       = title;
        this.description = description;
        this.price       = price;
        this.place       = place;
        this.subcatName  = subcatName;
        this.subcatId    = subcatId;
        this.shamsi      = shamsi;
        this.timestamp   = timestamp;
        this.imageCount  = imageCount;
        this.verified    = verified;
    }

    public int getId() { return this.id; }
    public String getPhoneNumber() { return this.phoneNumber; }
    public String getTelegramId() { return this.telegramId; }
    public String getTitle() {
        return this.title;
    }
    public String getDescription() {
        return this.description;
    }
    public String getPrice() {
        return this.price;
    }
    public String getPlace() {
        return this.place;
    }
    public String getSubcatName() { return this.subcatName; }
    public int getSubcatId() { return this.subcatId; }
    public String getShamsi() {
        return this.shamsi;
    }
    public String getTimestamp() {
        return this.timestamp;
    }
    public int getImageCount() {
        return this.imageCount;
    }
    public int getVerified() { return this.verified; }
}
