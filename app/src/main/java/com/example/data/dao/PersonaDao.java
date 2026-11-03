package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.entity.PersonaEntity;

import java.util.List;

@Dao
public interface PersonaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PersonaEntity persona);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PersonaEntity> personas);

    @Update
    void update(PersonaEntity persona);

    @Delete
    void delete(PersonaEntity persona);

    @Query("SELECT * FROM personas ORDER BY updatedAt DESC")
    LiveData<List<PersonaEntity>> getAllPersonasLive();

    @Query("SELECT * FROM personas ORDER BY updatedAt DESC")
    List<PersonaEntity> getAllPersonasSync();

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    PersonaEntity getByIdSync(String id);

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    LiveData<PersonaEntity> getByIdLive(String id);

    @Query("SELECT * FROM personas WHERE username = :username LIMIT 1")
    PersonaEntity getByUsernameSync(String username);

    @Query("SELECT COUNT(*) FROM personas")
    int getCount();

    @Query("DELETE FROM personas")
    void deleteAll();
}
