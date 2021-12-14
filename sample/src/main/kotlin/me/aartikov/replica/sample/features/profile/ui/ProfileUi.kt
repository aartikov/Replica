package me.aartikov.replica.sample.features.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.widget.LceWidget
import me.aartikov.replica.sample.features.profile.domain.Profile
import me.aartikov.replica.simple.Loadable

@Composable
fun ProfileUi(
    component: ProfileComponent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        LceWidget(
            state = component.profileState,
            onRetryClick = component::onRetryClick
        ) { profile, refreshing ->
            ProfileContent(
                profile = profile,
                refreshing = refreshing,
                onRefresh = { component.onPullToRefresh() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ProfileContent(
    profile: Profile,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(refreshing),
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = profile.name,
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 32.dp)
            )
            Image(
                painter = rememberImagePainter(profile.avatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.surface)
            )
        }
    }
}

@Preview
@Composable
fun ProfileUiPreview() {
    AppTheme {
        ProfileUi(FakeProfileComponent())
    }
}


class FakeProfileComponent : ProfileComponent {

    override val profileState = Loadable<Profile>(loading = true)

    override fun onPullToRefresh() {}

    override fun onRetryClick() {}
}
