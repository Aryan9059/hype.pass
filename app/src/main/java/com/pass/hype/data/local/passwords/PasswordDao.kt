package com.pass.hype.data.local.passwords

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Upsert
    fun upsertPassword(passwords: Passwords)

    @Query("SELECT * FROM Passwords WHERE passwordId = :passwordId")
    fun getPasswordById(passwordId: Int): Passwords?

    @Query("DELETE FROM Passwords WHERE passwordId = :passwordId")
    fun deletePassword(passwordId: Int)

    @Query("SELECT * FROM Passwords")
    fun getAllPasswords(): LiveData<List<Passwords>>

    @Query("SELECT * FROM Passwords WHERE appName LIKE '%' || :searchQuery || '%'")
    fun searchUsersByName(searchQuery: String): Flow<List<Passwords>>

    @Query("SELECT * FROM Passwords ORDER BY editTime DESC")
    suspend fun getAllPasswordsSync(): List<Passwords>

    @RawQuery
    suspend fun executeQuery(query: SimpleSQLiteQuery): List<Any>
}