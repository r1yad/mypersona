package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.entity.MediaAssetEntity;

import java.util.List;

@Dao
public interface MediaAssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaAssetEntity asset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MediaAssetEntity> assets);

    @Delete
    void delete(MediaAssetEntity asset);

    @Query("SELECT * FROM media_assets WHERE id = :id LIMIT 1")
    MediaAssetEntity getByIdSync(String id);

    @Query("SELECT * FROM media_assets")
    List<MediaAssetEntity> getAllSync();

    @Query("DELETE FROM media_assets")
    void deleteAll();
}
