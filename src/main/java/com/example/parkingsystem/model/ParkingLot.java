package com.example.parkingsystem.model;
import jakarta.persistence.*;

@Entity
@Table(name="parking_lot")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String name;
    private String location;
    @Column(name="id_manager")
    private int managerId;
    @Column(name="total_number_spots")
    private int spots;
    @Column(name="free_number_spots")
    private int freeSpots;
    @Column(name="price_per_hour")
    private double price;

    public String getLocation() { return location; }

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

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getFreeSpots() {
        return freeSpots;
    }

    public void setFreeSpots(int freeSpots) {
        this.freeSpots = freeSpots;
    }
}
