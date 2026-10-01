package eu.darken.butler.main.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.butler.common.BuildConfigWrap
import eu.darken.butler.common.coroutine.DispatcherProvider
import eu.darken.butler.common.debug.logging.log
import eu.darken.butler.common.debug.logging.logTag
import eu.darken.butler.common.navigation.Nav
import eu.darken.butler.common.ui.ViewModel4
import eu.darken.butler.main.core.GeneralSettings
import eu.darken.butler.workspace.ui.workspaces.workspaces
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    dispatchers: DispatcherProvider,
    private val generalSettings: GeneralSettings,
) : ViewModel4(dispatchers, logTag("Onboarding", "Screen", "VM")) {

    val state = generalSettings.isOnboardingCompleted.flow.map { isCompleted ->
        State()
    }.asStateFlow()

    fun completeOnboarding() = launch {
        log(tag) { "completeOnboarding()" }
        generalSettings.isOnboardingCompleted.value(true)
        navTo(
            Nav.Main.workspaces(),
            popUpTo = Nav.Main.workspaces(),
            inclusive = true
        )
    }

    data class State(
        val startPage: Page = Page.WELCOME,
        val isBeta: Boolean = BuildConfigWrap.BUILD_TYPE != BuildConfigWrap.BuildType.RELEASE,
    ) {

        enum class Page {
            WELCOME,
            WORKSPACES,
            BETA,
            PRIVACY,
            ;
        }
    }
}
