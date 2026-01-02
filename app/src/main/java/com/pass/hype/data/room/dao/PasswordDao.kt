package com.pass.hype.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pass.hype.data.room.model.Passwords
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Upsert
    suspend fun upsertPassword(passwords: Passwords)

    @Query("SELECT * FROM Passwords WHERE passwordId = :passwordId")
    suspend fun getPasswordById(passwordId: Int): Passwords?

    @Query("DELETE FROM Passwords WHERE passwordId = :passwordId")
    suspend fun deletePassword(passwordId: Int)

    @Query("DELETE FROM Passwords")
    suspend fun deleteAllPasswords()

    @Query("SELECT * FROM Passwords")
    fun getAllPasswords(): LiveData<List<Passwords>>

    @Query("SELECT * FROM Passwords WHERE appName LIKE '%' || :searchQuery || '%'")
    fun searchUsersByName(searchQuery: String): Flow<List<Passwords>>

    @Query("SELECT * FROM Passwords ORDER BY editTime DESC")
    suspend fun getAllPasswordsSync(): List<Passwords>

    @Query("SELECT * FROM Passwords WHERE appName = :appName")
    suspend fun getPasswordsByAppName(appName: String): List<Passwords>
}