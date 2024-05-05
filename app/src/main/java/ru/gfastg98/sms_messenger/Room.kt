package ru.gfastg98.sms_messenger

import android.content.Context
import android.util.Log
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

val String.color: Color
    get() =
        try {
            Color(android.graphics.Color.parseColor("#$this"))
        } catch (e: Exception) {
            e.message?.let { Log.e("parser", it) }
            Color(0)
        }

val Color.str: String
    get() =
        try {
            val red = this.red * 255
            val green = this.green * 255
            val blue = this.blue * 255
            String.format("%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
        } catch (e: Exception) {
            e.message?.let { Log.e("parser", it) }
            "0"
        }




@ProvidedTypeConverter
class Converters {
    val TAG = "conv"
    /// Color - String
    @TypeConverter
    fun colorFromString(value: String?): Color? {
        if (value == null || value == "0") return Color(0xFF3F51B5)
        Log.i (TAG, "color parsed: ${value.color}")
        return value.color
        /*if (value.length > 8) throw IllegalArgumentException("color must be in HEX like: 00FF00FF")
        val v = value.toLongOrNull()
        if (v == null) {
            Log.e("conv", "null Long")
            return Color(0)
        }
        return Color(v)*/
    }

    @TypeConverter
    fun colorToString(color: Color?): String? {
        if (color == null) return "0"
        val c = color.str
        Log.i(TAG, "colorToString: $c")
        return c
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

    //@Query("SELECT * FROM users LEFT JOIN messages on (users.id = messages.userId) where  ORDER BY datetime LIMIT 1 ")
    @Query("select messages.* " +
            "from messages, (select userId, max(datetime) as d from messages group by userId) as max_user " +
            "where messages.userId=max_user.userId " +
            "and messages.datetime = max_user.d;")
    fun getLastMessages() : Flow<List<Message>>

    @Delete
    suspend fun deleteUser(vararg user: User)

    @Delete
    suspend fun deleteMessage(vararg message: Message)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

}
