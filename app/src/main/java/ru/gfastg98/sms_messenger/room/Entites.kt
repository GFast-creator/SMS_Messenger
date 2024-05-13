package ru.gfastg98.sms_messenger.room

import android.provider.Telephony
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("threadId"),
            onDelete = ForeignKey.CASCADE
        )
    ])
class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var text: String,
    var datetime: Date,
    var check: Boolean = false,
    @ColumnInfo(index = true)
    val threadId: Int?,
    val type: Int = Telephony.Sms.MESSAGE_TYPE_ALL
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true)
    var id: Long = 0,
    var name: String = "test name",
    var num: String? = null,
    var color: Color = Color(0xFFFF9800)
)