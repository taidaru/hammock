/*
 * Copyright 2022, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright 2026, Taidaru
 */
// hammock: about screen without Lawnchair team, community links, privacy policy and updater

package app.lawnchair.ui.preferences.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.layout.PreferenceDivider
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupHeading
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupItem
import app.lawnchair.ui.preferences.components.layout.PreferenceLayoutLazyColumn
import app.lawnchair.ui.preferences.navigation.AboutLicenses
import com.android.launcher3.BuildConfig
import com.android.launcher3.R

@Composable
fun About(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    PreferenceLayoutLazyColumn(
        label = stringResource(id = R.string.about_label),
        modifier = modifier,
        backArrowVisible = !LocalIsExpandedScreen.current,
    ) {
        item {
            Spacer(Modifier.padding(top = 8.dp))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_home_comp),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Text(
                text = stringResource(id = R.string.derived_app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = BuildConfig.VERSION_DISPLAY_NAME,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val commitUrl = "$REPOSITORY_URL/commit/${BuildConfig.COMMIT_HASH}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, commitUrl.toUri()))
                            },
                        ),
                )
            }
        }
        item {
            Spacer(modifier = Modifier.requiredHeight(16.dp))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                LawnchairLink(
                    iconResId = R.drawable.ic_github,
                    label = stringResource(id = R.string.github),
                    modifier = Modifier.weight(weight = 1f),
                    url = REPOSITORY_URL,
                )
            }
        }
        item {
            PreferenceGroupHeading(
                stringResource(R.string.legal),
            )
        }
        item {
            PreferenceGroupItem(
                cutTop = false,
                cutBottom = true,
            ) {
                NavigationActionPreference(
                    label = stringResource(id = R.string.acknowledgements),
                    destination = AboutLicenses,
                )
            }
        }
        item {
            PreferenceGroupItem(
                cutTop = true,
                cutBottom = true,
            ) {
                PreferenceDivider()
                ClickablePreference(
                    label = stringResource(id = R.string.based_on_lawnchair),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, LAWNCHAIR_URL.toUri())
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}

private const val REPOSITORY_URL = "https://github.com/taidaru/hammock"
private const val LAWNCHAIR_URL = "https://github.com/LawnchairLauncher/lawnchair"
