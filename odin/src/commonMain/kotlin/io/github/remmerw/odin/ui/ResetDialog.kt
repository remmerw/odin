package io.github.remmerw.odin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.cancel
import io.github.remmerw.odin.generated.resources.ok
import io.github.remmerw.odin.generated.resources.reset_application_data
import io.github.remmerw.odin.generated.resources.warning
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDialog(stateModel: StateModel, onDismissRequest: () -> Unit) {
    BasicAlertDialog(onDismissRequest = { onDismissRequest.invoke() }) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.warning),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(Res.string.reset_application_data),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp)
                ) {
                    Button(onClick = { onDismissRequest.invoke() }) {
                        Text(
                            text = stringResource(Res.string.cancel),
                        )
                    }
                    Box(modifier = Modifier.weight(1.0f, true))
                    Button(onClick = {
                        onDismissRequest.invoke()
                        stateModel.reset()
                    }
                    ) {
                        Text(
                            text = stringResource(Res.string.ok),
                        )
                    }
                }
            }
        }
    }
}