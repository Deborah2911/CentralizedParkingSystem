package model;

public class ParkingLot {

    private int id;
    private String name;
    private String location;
    private int spots;
    private double price;

    public ParkingLot(int id, String name, String location, int spots, double price) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.spots = spots;
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpots() {
        return spots;
    }

    public void setSpots(int spots) {
        this.spots = spots;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
