package com.financialwhirlpool.cvs.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.financialwhirlpool.cvs.Class.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by an vo on 4/25/2016.
 */
public class StoreDataSource {
    SQLiteOpenHelper dbhelper;
    SQLiteDatabase database;
    private static final String[] allColumns ={"lat","lng","district","address"};
    public StoreDataSource(Context context){
        dbhelper = new StoreOpenHelper(context);
    }
    ArrayList<String[]> districtList;
    String[] dis1={"Quận 1","Quận 3"};
    String[] dis2={"Quận 2","Quận 1"};
    String[] dis3={"Quận 3","Quận 4"};
    String[] dis4={"Quận 4","Quận 7"};
    String[] dis5={"Quận 5","Quận 7"};
    String[] dis6={"Quận 6","Quận 5","Quận 11"};
    String[] dis7={"Quận 7","Quận 4","Quận 5"};
    String[] dis8={"Quận 8","Quận 5","Quận 6"};
    String[] dis9={"Quận 9"};
    String[] dis10={"Quận 10","Quận 5","Quận 11","Quận 3"};
    String[] dis11={"Quận 11","Quận 10","Quận 5"};
    String[] disBinhTan={"Quận Bình Tân","Quận 8","Quận Tân Phú"};
    String[] disBinhThanh={"Quận Bình Thạnh","Quận 2","Quận Phú Nhuận"};
    String[] disGoVap={"Quận Gò Vấp","Quận Phú Nhuận"};
    String[] disPhuNhuan={"Quận Gò Vấp","Quận Phú Nhuận"};
    String[] disTanBinh={"Tân Bình","Tân Phú","Phú Nhuận"};
    String[] disTanPhu={"Quận 11","Quận Tân Phú","Quận Tân Bình"};
    String[] disThuDuc={"Quận Thủ Đức"};

    public void open(){
        Log.i("trido -- ","Db opened");
        database=dbhelper.getWritableDatabase();
        districtList = new ArrayList<String[]>();
        districtList.add(dis1);
        districtList.add(dis2);
        districtList.add(dis3);
        districtList.add(dis4);districtList.add(dis5);districtList.add(dis6);districtList.add(dis7);districtList.add(dis8);districtList.add(dis9);
        districtList.add(dis10);districtList.add(dis11);
        districtList.add(disBinhTan);districtList.add(disBinhThanh);
        districtList.add(disGoVap);districtList.add(disPhuNhuan);
        districtList.add(disTanBinh);districtList.add(disTanPhu);districtList.add(disThuDuc);
    }
    public void close(){
        Log.i("trido -- ","Db closed");
        dbhelper.close();
    }
    public List<Store> findAll(String address){
        List<Store> stores = new ArrayList<Store>();
        String dis="district IN(?";
        int index= findArray(address);
        if(index != -1) {
            String[] tempDistrictList = districtList.get(index);
            for (int i = 0; i < tempDistrictList.length; i++) {
                dis += ",?";
            }
            dis += ")";

            Cursor cursor = database.query(StoreOpenHelper.TABLE_CIRCLEK, allColumns, dis, tempDistrictList, null, null, null);
            //Cursor cursor = database.rawQuery("SELECT * FROM circlek",null);
//        Cursor keyCursor = database.rawQuery("SELECT shopId FROM circlek WHERE name='"+n+"'",null);
            System.out.println(" trido --" + cursor.getCount() + "  rows");

            int latIndex = cursor.getColumnIndex("lat");
            int lngIndex = cursor.getColumnIndex("lng");
            int addressIndex = cursor.getColumnIndex("address");
            cursor.moveToFirst();
            try {
                while (cursor != null) {
                    Store store = new Store();
                    store.setLat(cursor.getDouble(latIndex));
                    store.setLng(cursor.getDouble(lngIndex));
                    store.setAddress(cursor.getString(addressIndex));
                    stores.add(store);
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stores;
        }else{
            return null;
        }
    }

    public int findArray(String address){
        for(int i =0;i< StoreOpenHelper.districtList.length;i++){
            if(address.contains(StoreOpenHelper.districtList[i])){
                if(address.contains("Quận 10")){
                    return 9;
                }else{
                    return i;
                }
            }
        }
        return -1;
    }
}
