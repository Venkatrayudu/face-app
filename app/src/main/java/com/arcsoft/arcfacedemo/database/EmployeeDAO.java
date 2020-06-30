package com.arcsoft.arcfacedemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.arcsoft.arcfacedemo.model.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO extends DatabaseHandler {

    static SQLiteDatabase db;

    public EmployeeDAO(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
         db = this.getWritableDatabase();
        Log.i("AAA", "BBB");
    }

    public static boolean create(Employee emp) {
        ContentValues values = new ContentValues();
        values.put("firstname", emp.getFirstName());
        values.put("lastname", emp.getFirstName());
        values.put("id_number", emp.getId());
        values.put("password", emp.getPassword());

        boolean createSuccessful = db.insert("employees", null, values) > 0;
//        db.close();
        Log.i("create()", "" + createSuccessful);
        return createSuccessful;
    }

    public static int count() {
        Log.i("count()", "Inside count()");
        String sql = "SELECT * FROM employees";
        int recordCount = db.rawQuery(sql, null).getCount();
//        db.close();
        Log.i("count()", "" + recordCount);
        return recordCount;
    }

    public static List<Employee> read() {
        List<Employee> recordsList = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY id DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
                String firstName = cursor.getString(cursor.getColumnIndex("firstname"));
                String lastName = cursor.getString(cursor.getColumnIndex("lastname"));
                String idNumber = cursor.getString(cursor.getColumnIndex("id_number"));
                String pwd = cursor.getString(cursor.getColumnIndex("password"));
                Employee emp = new Employee(firstName, lastName, idNumber, pwd);
                recordsList.add(emp);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        return recordsList;
    }
}
