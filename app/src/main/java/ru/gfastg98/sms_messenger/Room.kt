package ru.gfastg98.sms_messenger

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.ProvidedTypeConverter
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@ProvidedTypeConverter
class Converters {
    /// Color - String
    @TypeConverter
    fun colorFromString(value: String?): Color? {
        if (value == null) return Color(0xFF000000)
        //if (value.length > 8) throw IllegalArgumentException("color must be in HEX like: 00FF00FF")
        return Color(value.toULong())
    }

    @TypeConverter
    fun colorToString(color: Color?): String? {
        return color?.value?.toString()?:"0"
    }
    ///

    /// Date - Long
    @TypeConverter
    fun colorFromString(value: Long?): Date? {
        return value?.let { Date(value) }?: Date()
    }

    @TypeConverter
    fun colorToString(date: Date?): Long? {
        return date?.time?:0
    }
    ///
}


@Entity(
    tableName = "messages",

    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE
        )
    ])

class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    var datetime: Date,
    var check: Boolean = false,
    @ColumnInfo(index = true)
    val userId: Int,
    val fromId: Int? = null
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true)
    val id: Int = 0,
    val name: String = "Test Name",
    var color: Color = Color(0xFFFF9800)
)

/*class UserAndMessages{
    @Embedded
    var user: User? = null

    @Relation(parentColumn = "id", entityColumn = "userId")
    var messages: List<Message> = emptyList()
}*/


@Database(entities = [Message::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messengerDao(): MessageDao

    companion object{
        private var instance: AppDatabase? = null

        fun create(context : Context) : AppDatabase {
            if (instance == null){
                instance = Room
                    .databaseBuilder(context, AppDatabase::class.java, "database.db")
                    .addTypeConverter(Converters())
                    .build()
            }
            return instance as AppDatabase
        }
    }
}

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

    @Query("SELECT * FROM users left join messages on messages.userId = users.id")
    fun getTable() : Flow<Map<User, List<Message>>>

    @Query("SELECT * FROM users")
    fun getUsers() : Flow<List<User>>

    @Query("SELECT * FROM messages where userId = :userId")
    fun getMessages(userId : Int) : Flow<List<Message>>

    @Query("SELECT * FROM messages where userId = :userId ORDER BY datetime LIMIT 1 ")
    fun getLastMessages(userId : Int) : Flow<List<Message>>

    @Delete
    suspend fun deleteUser(vararg user: User)

    @Delete
    suspend fun deleteMessage(vararg message: Message)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

}
