package me.aartikov.replica.sample.features.profile.ui

import me.aartikov.replica.sample.features.profile.domain.Profile
import me.aartikov.replica.simple.Loadable

interface ProfileComponent {

    val profileState: Loadable<Profile>

    fun onPullToRefresh()

    fun onRetryClick()
}