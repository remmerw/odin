package io.github.remmerw.odin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.connections
import io.github.remmerw.odin.generated.resources.relays
import io.github.remmerw.odin.generated.resources.server_network
import io.github.remmerw.odin.generated.resources.untitled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ConnectionsView(addresses: List<String>) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 32.dp)
    ) {
        Text(
            text = stringResource(Res.string.connections),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )


        LazyColumn {
            items(items = addresses) { item ->
                AddressItem(item)
            }
        }
    }
}


@Composable
fun AddressItem(address: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painterResource(Res.drawable.server_network),
                contentDescription = stringResource(Res.string.untitled),
                modifier = Modifier
                    .size(24.dp)
                    .align(
                        Alignment.CenterVertically
                    )
            )

            Text(
                modifier = Modifier
                    .padding(16.dp, 0.dp, 0.dp, 0.dp)
                    .weight(1.0f, true)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                text = address,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall, softWrap = false
            )

        }
    }
}

@Composable
fun RelaysView(list: List<Peeraddr>) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 32.dp)
    ) {
        Text(
            text = stringResource(Res.string.relays),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            items(items = list) { item ->
                PeeraddrItem(item)
            }
        }
    }
}


@Composable
fun PeeraddrItem(peeraddr: Peeraddr) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painterResource(Res.drawable.server_network),
                contentDescription = stringResource(Res.string.untitled),
                modifier = Modifier
                    .size(24.dp)
                    .align(
                        Alignment.CenterVertically
                    )
            )

            Column(
                modifier = Modifier
                    .padding(16.dp, 0.dp, 0.dp, 0.dp)
                    .weight(1.0f, true)
            ) {
                Text(
                    text = peeraddr.peerId.toBase58(), maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall, softWrap = false
                )
                Text(
                    text = peeraddr.address() + ":" + peeraddr.port, maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall, softWrap = false
                )
            }
        }
    }
}