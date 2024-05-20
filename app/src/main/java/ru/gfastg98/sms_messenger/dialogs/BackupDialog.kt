package ru.gfastg98.sms_messenger.dialogs

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gfastg98.sms_messenger.Command
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.Repository
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.name

@Composable
fun BackupDialog(
    viewModel: MessengerViewModel,
    onDismissAction: () -> Unit,
    startDirectory: File,
) {
    val context = LocalContext.current

    var pickedFile by remember { mutableStateOf<Uri?>(null) }
    var pickedDirectory by remember {
        mutableStateOf(
            startDirectory
        )
    }

    var decision by remember { mutableStateOf(true) }

    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            pickedFile = imageUri
        }
    }

    var hasTriedToDismiss by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Archive, contentDescription = null)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = stringResource(R.string.archive))
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row {// RadioGroup (импорт/экспорт)
                    Row(
                        Modifier
                            .clickable {
                                decision = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.import_l))
                        RadioButton(selected = decision,
                            onClick = { decision = true }
                        )
                    }
                    Row(
                        Modifier.clickable {
                            decision = false
                            pickedDirectory =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.export_l))
                        RadioButton(
                            selected = !decision,
                            onClick = {
                                decision = false
                                pickedDirectory =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            }
                        )
                    }
                }

                if (decision) {
                    Box(
                        Modifier
                            .clip(ButtonDefaults.shape)
                    ) {
                        Box(
                            Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,

                                ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = pickedFile?.path?.let { Path(it).name } ?: stringResource(
                                        R.string.choose_file
                                    )
                                )
                                Button(onClick = {
                                    pickPictureLauncher.launch("text/comma-separated-values")
                                }
                                ) {
                                    Text(text = stringResource(R.string.choose))
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(start = 8.dp, top = 4.dp, bottom = 8.dp, end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)

                        ) {
                            Text(text = stringResource(R.string.choose_folder))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.save_here),
                                        style = TextStyle(
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = pickedDirectory.name,
                                        style = TextStyle(
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }
                            val options = pickedDirectory.listFiles { f -> f.isDirectory }
                            if (options != null) {
                                Log.i("BackupDialog", "BackupDialog: ${options.joinToString()}")
                            }

                            Button(
                                modifier = Modifier
                                    .height(28.dp),
                                onClick = {
                                    val newDestination =
                                        pickedDirectory.parent?.let { File(it) }
                                    if (newDestination?.list() != null) {
                                        pickedDirectory = newDestination
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null
                                )
                            }
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                options?.let { dirs ->
                                    items(dirs) { dir ->
                                        Text(
                                            modifier = Modifier
                                                .clickable {
                                                    pickedDirectory =
                                                        File("${pickedDirectory.absolutePath}/${dir.name}")

                                                },
                                            text = dir.name
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissAction,
        confirmButton = {
            Button(onClick = {
                hasTriedToDismiss = true
                if (decision) {
                    if (pickedFile != null) {
                        if (viewModel.onEvent<Int>(
                            Command.IMPORT_SMS(
                                context,
                                pickedFile!!
                            )
                        ) == Repository.RESULT_OK)
                            onDismissAction()
                    }
                    onDismissAction()
                } else {
                    if (viewModel.onEvent<Int>(
                        Command.EXPORT_SMS(
                            context,
                            pickedDirectory
                        )
                    ) == Repository.RESULT_OK)
                        onDismissAction()
                }
            }) {
                if (decision) {
                    Text(text = stringResource(R.string.load))
                } else {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    )
}

/*
@Preview
@Composable
private fun BackupDialogPreview() {
    BackupDialog(
        onDismissAction = {

        },
        File("/storage/0")
    )
}*/
