package com.financialwhirlpool.cvs.Class;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by an vo on 5/7/2016.
 */
public class MapDirection {
    @SerializedName("routes")
    private List<Routes> routes;

    public List<Routes> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Routes> routes) {
        this.routes = routes;
    }
    public static class Routes{
        @SerializedName("overview_polyline")
        private Poly overview_polyline;

        public Poly getOverview_polyline() {
            return overview_polyline;
        }

        public void setOverview_polyline(Poly overview_polyline) {
            this.overview_polyline = overview_polyline;
        }
    }
    public static class Poly{
        private String points;

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }
}
