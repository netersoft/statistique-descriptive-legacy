package com.neteru.tixtat.classes.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.neteru.tixtat.classes.models.Backups;

import java.util.List;

/**
 * Created by Ark Noam on 20/11/2018.
 */
@SuppressWarnings("unused")
public class DB_Manager extends OrmLiteSqliteOpenHelper {

    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "backups.db";
    private final static String TAG = "DB_MANAGER";
    private Dao<Backups, Integer> backupDao;

    public DB_Manager(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            
            TableUtils.createTable(connectionSource, Backups.class);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création des Tables - " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            
            TableUtils.dropTable(connectionSource, Backups.class, true);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour des Tables - " + e);
        }
    }

    public void db_insertBackup(Backups backup){
        try {

            getDao(Backups.class).create(backup);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de l'insertion de l'objet dans la Table - "+e);
        }
    }

    public List<Backups> db_readAllBackups(){
        try {

            backupDao = getDao(Backups.class);
            QueryBuilder<Backups, Integer> qb = backupDao.queryBuilder();
            qb.orderBy("id", true);

            return qb.query();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture de toute la Table - "+e);
            return null;
        }
    }

    public void db_removeBackup(Backups backup){
        try {

            backupDao = getDao(Backups.class);
            DeleteBuilder<Backups, Integer> ub = backupDao.deleteBuilder();

            ub.where().eq("id", backup.getId());
            ub.delete();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la suppression dans la Table - "+e);
        }
    }
}