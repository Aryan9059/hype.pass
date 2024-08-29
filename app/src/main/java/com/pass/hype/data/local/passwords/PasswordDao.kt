package com.pass.hype.data.local.passwords

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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
}