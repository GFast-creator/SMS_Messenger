package ru.gfastg98.sms_messenger.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(vararg user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(vararg message: Message)

    @Update
    suspend fun updateUsers(vararg user: User)

    @Update
    suspend fun updateMessage(vararg message: Message)

    /*    @Query("SELECT * FROM users left join messages on messages.userId = users.id")
        fun getTable() : Flow<Map<User, List<Message>>>*/

    @Query("SELECT * FROM users")
    fun getUsers() : Flow<List<User>>

    /*    @Query("SELECT * FROM messages where userId = :userId")
        fun getMessages(userId : Int) : Flow<List<Message>>*/

    //@Query("SELECT * FROM users LEFT JOIN messages on (users.id = messages.userId) where  ORDER BY datetime LIMIT 1 ")
    /*    @Query("select messages.* " +
                "from messages, (select userId, max(datetime) as d from messages group by userId) as max_user " +
                "where messages.userId=max_user.userId " +
                "and messages.datetime = max_user.d;")
        fun getLastMessages() : Flow<List<Message>>*/

    @Delete
    suspend fun deleteUser(vararg user: User)

    @Delete
    suspend fun deleteMessage(vararg message: Message)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

}