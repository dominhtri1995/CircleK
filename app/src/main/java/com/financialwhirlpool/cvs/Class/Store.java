package com.financialwhirlpool.cvs.Class;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by an vo on 4/25/2016.
 */
public class Store {
    private double lat;
    private double lng;
    private String address;
    private double shopId;
    private int distance;
    private PolylineOptions options;

    public PolylineOptions getOptions() {
        return options;
    }

    public void setOptions(PolylineOptions options) {
        this.options = options;
    }

    public double getShopId() {
        return shopId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getLat(){
        return lat;
    }
    public double getLng(){
        return lng;
    }
    public void setLat(double lat){
        this.lat =lat;
    }
    public void setLng(double lng){
        this.lng = lng;
    }
    public void setAddress(String address){
        this.address=address;
    }
    public String getAddress(){
        return address;
    }

    public Store(double lat, double lng){
        this.lat=lat;
        this.lng =lng;
    }
    public Store(){

    }
    public double getshopID(){
        return shopId;
    }
    public void setShopId(double id){
        shopId=id;
    }
}
