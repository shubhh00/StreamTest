package com.app.streamtest.data.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO = "data access object": an interface of database operations. Room writes the
 * code for it from the annotations. A function returning Flow re-runs its query and
 * sends a new list every time the table changes.
 */
@Dao
interface TestResultDao {

    @Insert
    suspend fun insert(result: TestResultEntity): Long

    @Query("DELETE FROM test_results")
    suspend fun deleteAll()

    @Query("SELECT * FROM test_results ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<TestResultEntity>>

    // The newest row of each stream: the biggest id per streamId is the last one saved.
    @Query("SELECT * FROM test_results WHERE id IN (SELECT MAX(id) FROM test_results GROUP BY streamId)")
    fun observeLatestPerStream(): Flow<List<TestResultEntity>>
}
