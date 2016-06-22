package com.financialwhirlpool.cvs.Class;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by an vo on 5/2/2016.
 */
public class ClassGoogleMap {
    @Expose
    @SerializedName("rows")
    private List<Rows> rows;
    @SerializedName("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public List<Rows> getRows() {
        return rows;
    }


    public static class Rows {
        private List<Elements> elements;
        // +getters+setter
        public List<Elements> getElements(){
            return elements;
        }

    }

    public static class Elements{
        @Expose
        @SerializedName("distance")
        private Distance distance;
        @SerializedName("status")
        private String status;
        // +getters+setters

        public Distance getDistance() {
            return distance;
        }
        public String getStatus(){
            return status;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }
    }

    public static class Distance {
        private String text;
        private int value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        // +getter+setter
    }
}
