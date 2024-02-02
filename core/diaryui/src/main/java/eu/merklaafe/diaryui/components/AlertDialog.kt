package eu.merklaafe.diaryui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DisplayAlertDialog(
    title: String,
    message: String,
    dialogOpened: Boolean,
    onClosedDialog: () -> Unit,
    onYesClicked: () -> Unit,
) {
    if (dialogOpened) {
        AlertDialog(
            title = {
                Text(
                    text = title,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Normal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onYesClicked()
                        onClosedDialog()
                    })
                {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onClosedDialog)
                {
                    Text(text = "No")
                }
            },
            onDismissRequest = onClosedDialog
        )
    }
}