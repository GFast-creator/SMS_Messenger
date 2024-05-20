package ru.gfastg98.sms_messenger.activites

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class StartActivity : ComponentActivity() {

    companion object {
        private const val TAG = "StartActivity"
        private val permissions = listOf(
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS
        )
    }

    private fun checkPermission(): Boolean {
        var g = false
        permissions.forEach { permission ->
            g = (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED) || g
            Log.i(TAG, "checkPermission: $g")
        }
        return !g
    }

    private var isSMSDefaultDialog = false
    private var isCanceled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSMessengerTheme {
                var isDialog by remember { mutableStateOf(!checkPermission()) }

                var isRoleAvailable by remember { mutableStateOf(true) }

                var isCanceledCompose by remember { mutableStateOf(false) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Log.d(TAG, "PERMISSION GRANTED")
                    } else {
                        Log.d(TAG, "PERMISSION DENIED")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier
                            .fillMaxSize(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(0.8f),
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null
                        )
                        if (isCanceledCompose)
                            Text(
                                text = stringResource(R.string.error_SMS_default)
                            )
                    }
                }

                val scope = rememberCoroutineScope()
                val permissionFlow = flow {

                    while (true) {
                        val l = Telephony.Sms.getDefaultSmsPackage(applicationContext)
                        Log.i(TAG, "onCreate: sms default is $l")

                        if (permissions.all { permission ->
                                (ContextCompat.checkSelfPermission(
                                    this@StartActivity, permission
                                ) == PackageManager.PERMISSION_GRANTED)
                            } && l.equals(packageName)) break

                        if (l != null && !l.equals(packageName) && !isSMSDefaultDialog) {
                            isCanceledCompose = isCanceled
                            isSMSDefaultDialog = true

                            val roleManager = getSystemService(RoleManager::class.java)

                            isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS)

                            val roleRequestIntent = roleManager
                                .createRequestRoleIntent(RoleManager.ROLE_SMS)
                            startActivityForResult(roleRequestIntent, 12)
                        }

                        permissions.forEach { permission ->
                            if (ContextCompat.checkSelfPermission(
                                    this@StartActivity, permission
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                launcher.launch(permission)
                            }
                        }
                        delay(200.milliseconds)
                    }

                    delay(3.seconds)
                    emit(Unit)
                }.onCompletion {
                    if (it == null) {
                        startActivity(Intent(this@StartActivity, MainActivity::class.java))
                        finish()
                    }
                }.also {
                    if (!isDialog) {
                        it.launchIn(scope)
                    }
                }

                if (isDialog) {
                    AlertDialog(
                        title = { Text(text = stringResource(R.string.permissions)) },
                        text = { Text(text = stringResource(R.string.permission_dialog_text)) },
                        onDismissRequest = {},
                        confirmButton = {
                            Button(onClick = {
                                isDialog = false
                                permissionFlow.launchIn(scope)
                            }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult: $resultCode")
        if (requestCode == 12) {
            if (resultCode == RESULT_CANCELED)
                isCanceled = true
            isSMSDefaultDialog = false
        }
    }
}