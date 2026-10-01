package eu.darken.butler.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.darken.butler.upgrade.UpgradeRepo

@Composable
fun ProGate(
    modifier: Modifier = Modifier,
    isPro: Boolean = rememberIsPro(),
    description: String? = null,
    onUpgrade: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }
}

fun UpgradeRepo.Info?.rendersAsPro(): Boolean = true

@Composable
fun rememberIsPro(): Boolean = true
