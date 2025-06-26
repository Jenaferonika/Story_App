package com.example.storyapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeyDao {
    @Query("SELECT * FROM remote_key WHERE id = :id")
    suspend fun getRemoteKeyId(id: String): RemoteKey?

    @Query("DELETE FROM remote_key")
    suspend fun deleteRemoteKey()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: List<RemoteKey>)
}
