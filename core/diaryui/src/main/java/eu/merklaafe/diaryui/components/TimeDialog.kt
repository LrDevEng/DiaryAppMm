package eu.merklaafe.diaryui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(
    dialogOpened: Boolean,
    onDismissed: () -> Unit,
    onOk: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute,
        is24Hour = false
    )
    val localDensity = LocalDensity.current
    var componentWidth by remember { mutableStateOf(0.dp) }

    if(dialogOpened) {
        AlertDialog(
            onDismissRequest = onDismissed,
        ) {
            Surface(
                modifier = Modifier.width(componentWidth),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        modifier = Modifier.onGloballyPositioned {
                            componentWidth = with(localDensity) { it.size.width.toDp() }
                        }
                    ) {
                        TimePicker(
                            modifier = Modifier.padding(8.dp),
                            state = timePickerState
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismissed()
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                onOk(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            }
                        ) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TimeDialogPreview(

) {
    TimeDialog(
        dialogOpened = true,
        onDismissed = {},
        onOk = {}
    )
}