package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import androidx.room.Upsert;

import com.example.data.entity.PersonaEntity;

import java.util.List;

@Dao
public interface PersonaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PersonaEntity persona);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PersonaEntity> personas);

    @Upsert
    void upsert(PersonaEntity persona);

    @Upsert
    void upsertAll(List<PersonaEntity> personas);

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

    @Query("UPDATE personas SET avatarPath = :newPath WHERE avatarPath = :oldPath")
    void updateAvatarPath(String oldPath, String newPath);

    @Query("UPDATE personas SET coverPath = :newPath WHERE coverPath = :oldPath")
    void updateCoverPath(String oldPath, String newPath);

    @Query("DELETE FROM personas")
    void deleteAll();
}
