package eu.merklaafe.diaryui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDialog(
    dialogOpened: Boolean,
    onDismissed: () -> Unit,
    onOk: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState()



    if(dialogOpened) {
        DatePickerDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = onDismissed,
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        if(datePickerState.selectedDateMillis != null) {
                            val instant = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                            onOk(instant.atZone(ZoneId.systemDefault()).toLocalDate())
                        } else {
                            onOk(LocalDate.now())
                        }
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    onDismissed()
                }) {
                    Text(text = "Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
            )
        }
    }
}

@PreviewScreenSizes
@Preview()
@Composable
fun DateDialogPreview(

) {
    DateDialog(
        dialogOpened = true,
        onDismissed = {},
        onOk = {}
    )
}