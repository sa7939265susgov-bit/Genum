package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GeneratedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedNumberDao {
    @Query("SELECT * FROM generated_numbers ORDER BY createdAt DESC")
    fun getAllNumbersFlow(): Flow<List<GeneratedNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumber(number: GeneratedNumberEntity): Long

    @Update
    suspend fun updateNumber(number: GeneratedNumberEntity)

    @Delete
    suspend fun deleteNumber(number: GeneratedNumberEntity)

    @Query("DELETE FROM generated_numbers")
    suspend fun deleteAll()

    @Query("UPDATE generated_numbers SET deliveryStatus = :status, callAttempts = callAttempts + 1 WHERE id = :id")
    suspend fun updateDeliveryStatus(id: Long, status: String)
}
