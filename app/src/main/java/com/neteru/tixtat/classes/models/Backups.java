package com.neteru.tixtat.classes.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ark Noam on 20/11/2018.
 */

@SuppressWarnings("unused")
@DatabaseTable(tableName = "backups")
public class Backups {
    @DatabaseField(columnName = "name", canBeNull = false)
    private String name;
    @DatabaseField(columnName = "resolution", canBeNull = false)
    private String resolution;
    @DatabaseField(columnName = "xi", canBeNull = false)
    private String xi;
    @DatabaseField(columnName = "ni", canBeNull = false)
    private String ni;
    @DatabaseField(columnName = "date", canBeNull = false)
    private String date;
    @DatabaseField(generatedId = true)
    private int id;

    public Backups(){}

    public Backups(String n, String res, String xi_recup, String ni_recup){
        Date thisDate = new Date();
        SimpleDateFormat formatted = new SimpleDateFormat("E dd.MM.yyyy '-' HH:mm", Locale.US);

        date = formatted.format(thisDate);
        resolution = res;
        xi = xi_recup;
        ni = ni_recup;
        name = n;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getResolution() {
        return resolution;
    }

    public String getXi() {
        return xi;
    }

    public String getNi() {
        return ni;
    }

    public String getDate() {
        return date;
    }
}
