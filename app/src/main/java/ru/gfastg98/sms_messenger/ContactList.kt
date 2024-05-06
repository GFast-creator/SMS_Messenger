package ru.gfastg98.sms_messenger

import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Sms
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme


class StartActivity : ComponentActivity() {

    private var activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
        }
    }

    companion object{
        private const val TAG = "StartActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SMSMessengerTheme {
                Log.e(TAG, "onCreate: no")
                if (Sms.getDefaultSmsPackage(this) != packageName) {
                    Log.e(TAG, "onCreate: no")
                    val roleManager = getSystemService(RoleManager::class.java)
                    val roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    activityResultLauncher.launch(roleRequestIntent)
                } else {
                    //val lst: List<Sms> = getAllSms()
                }
            }
        }
    }
}