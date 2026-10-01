package eu.darken.butler.upgrade

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun UpgradeRepo.isPro(): Boolean = true

suspend fun UpgradeRepo.isProSettled(timeout: Duration = 5.seconds): Boolean = true

suspend fun UpgradeRepo.isProForUi(timeout: Duration = 3.seconds): Boolean = true
