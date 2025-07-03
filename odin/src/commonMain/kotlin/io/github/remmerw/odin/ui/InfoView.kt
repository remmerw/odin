package io.github.remmerw.odin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.remmerw.odin.ODIN_PORT
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.app_name
import io.github.remmerw.odin.generated.resources.application
import io.github.remmerw.odin.generated.resources.information
import io.github.remmerw.odin.generated.resources.ipv
import io.github.remmerw.odin.generated.resources.limitation
import io.github.remmerw.odin.generated.resources.port
import io.github.remmerw.odin.generated.resources.port_forwarding
import io.github.remmerw.odin.generated.resources.port_forwarding_info
import io.github.remmerw.odin.generated.resources.protocol
import io.github.remmerw.odin.generated.resources.untitled
import io.github.remmerw.odin.generated.resources.uri_qrcode
import io.github.remmerw.odin.generated.resources.view
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoView(stateModel: StateModel) {


    val homepage by remember { mutableStateOf(stateModel.pageUri()) }
    val observedAddress by remember { mutableStateOf(stateModel.observedAddress())}

    Column(
        modifier = Modifier
            .padding(0.dp, 0.dp, 0.dp, 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(Res.string.information),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(Res.string.limitation),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp)
        )


        if(observedAddress != null) {
            if(observedAddress!!.inet4()) {
                Text(
                    text = observedAddress!!.address(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Image(
            painter = rememberQrCodePainter(homepage),
            contentDescription = stringResource(Res.string.uri_qrcode),
            modifier = Modifier
                .size(240.dp)
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(Res.drawable.view),
                contentDescription = stringResource(Res.string.untitled)
            )
            Text(
                text = homepage,
                style = MaterialTheme.typography.labelMedium,
                softWrap = false,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1.0f, true)
                    .align(Alignment.CenterVertically)
                    .offset(8.dp)
            )
        }

        Spacer(modifier = Modifier.padding(32.dp))


        Text(
            text = stringResource(Res.string.port_forwarding),
            style = MaterialTheme.typography.titleMedium,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )


        Text(
            text = stringResource(Res.string.port_forwarding_info),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(Res.string.ipv),
                style = MaterialTheme.typography.labelLarge,
                softWrap = false,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1.0f, true)
                    .align(Alignment.CenterVertically)
                    .padding(32.dp, 0.dp, 0.dp, 0.dp)
            )
            Text(
                text = "IPv6",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp, 0.dp, 32.dp, 0.dp)
            )
        }

        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(Res.string.port),
                style = MaterialTheme.typography.labelLarge,
                softWrap = false,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1.0f, true)
                    .align(Alignment.CenterVertically)
                    .padding(32.dp, 0.dp, 0.dp, 0.dp)
            )
            Text(
                text = ODIN_PORT.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp, 0.dp, 32.dp, 0.dp)
            )
        }

        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(Res.string.protocol),
                style = MaterialTheme.typography.labelLarge,
                softWrap = false,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1.0f, true)
                    .align(Alignment.CenterVertically)
                    .padding(32.dp, 0.dp, 0.dp, 0.dp)
            )
            Text(
                text = "TCP",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp, 0.dp, 32.dp, 0.dp)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(Res.string.application),
                style = MaterialTheme.typography.labelLarge,
                softWrap = false,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1.0f, true)
                    .align(Alignment.CenterVertically)
                    .padding(32.dp, 0.dp, 0.dp, 0.dp)
            )
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp, 0.dp, 32.dp, 0.dp)
            )
        }
    }

    Spacer(modifier = Modifier.padding(32.dp))

}