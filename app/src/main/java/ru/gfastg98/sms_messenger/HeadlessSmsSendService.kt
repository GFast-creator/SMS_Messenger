package ru.gfastg98.sms_messenger

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented, заглушка")
    }
}