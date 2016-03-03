package cl.tidchile.antennagpstracker.models;

import java.util.ArrayList;

import io.realm.RealmObject;

/**
 * Created by benjamin on 3/3/16.
 */
public class Movement{
    private String phone;
    private double lat;
    private double lon;
    private int location_accuracy;
    private long timestamp;
    private ArrayList<CellConnection> cell_connections= new ArrayList<>();

    public Movement(String phone, double lat, double lon, int location_accuracy, long timestamp, ArrayList<CellConnection> cell_connections) {
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
        this.location_accuracy = location_accuracy;
        this.timestamp = timestamp;
        this.cell_connections = cell_connections;

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getLocation_accuracy() {
        return location_accuracy;
    }

    public void setLocation_accuracy(int location_accuracy) {
        this.location_accuracy = location_accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<CellConnection> getCell_connections() {
        return cell_connections;
    }

    public void setCell_connections(ArrayList<CellConnection> cell_connections) {
        this.cell_connections = cell_connections;
    }
}
