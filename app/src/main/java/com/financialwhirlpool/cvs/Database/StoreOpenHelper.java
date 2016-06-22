package com.financialwhirlpool.cvs.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by an vo on 4/25/2016.
 */
public class StoreOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME ="convenience.db";
    private static final int DATABASE_VERSION =1;

    public static final String TABLE_CIRCLEK ="circlek";
    public static final String[] districtList={"Quận 1","Quận 2","Quận 3","Quận 4","Quận 5","Quận 6","Quận 7","Quận 8","Quận 9","Quận 10","Quận 11",
            "Quận Bình Tân","Quận Bình Thạnh","Quận Gò Vấp","Quận Phú Nhuận","Quận Tân Bình","Quận Tân Phú","Quận Thủ Đức"};
    Context context;
    public StoreOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS circlek (lat DOUBLE,lng DOUBLE,district VARCHAR, address VARCHAR, shopId INTEGER PRIMARY KEY)");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("circlekaddress.txt")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {

                String district="";
                for(int i = 0; i<districtList.length;i++){
                    if(mLine.contains(districtList[i])){
                        if(mLine.contains("Quận 10")){
                            district="Quận 10";
                            break;
                        }else{
                            district=districtList[i];
                            break;
                        }
                    }
                }
                if(mLine.contains("%%%")) {
                    String[] info= mLine.split("%%%");
                    String[] latlng=info[1].split(",");
                    double lat = Double.parseDouble(latlng[0]);
                    double lng = Double.parseDouble(latlng[1]);
                    db.execSQL("INSERT INTO circlek (lat,lng,district,address) VALUES (" + lat + "," + lng+ ",'"+district + "'" + ",'" + info[0] + "')");
                }else{
                    LatLng myLatLng = getLocationFromAddress(context, mLine);
                    double lat = myLatLng.latitude;
                    double lng = myLatLng.longitude;
                    db.execSQL("INSERT INTO circlek (lat,lng,district,address) VALUES (" + lat + "," + lng+",'"+district +"'" + ",'" + mLine + "')");
                }
                Log.i("trido - address",mLine +" dis: "+district);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return p1;
    }
}
