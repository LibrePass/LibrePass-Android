package dev.medzik.android.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** A composable function with text with secondary text color. */
@Composable
fun SecondaryText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

/**
 * A composable function to display a preference group title.
 *
 * @param title The title to display for the preference group.
 * @param modifier The modifier for customizing the appearance and behavior of the preference group title.
 */
@Composable
fun PreferenceGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    SecondaryText(
        text = title,
        modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

/**
 * A composable function to display a preference entry.
 *
 * @param modifier The modifier for customizing the appearance and behavior of the preference entry.
 * @param title The title of the preference entry.
 * @param description An optional description of the preference entry.
 * @param content An optional composable content to display within the preference entry.
 * @param icon An optional icon to display with the preference entry.
 * @param trailingContent An optional composable content to display at the end of the preference entry.
 * @param onClick The callback to execute when the preference entry is clicked.
 * @param isEnabled A flag indicating whether the preference entry is enabled or disabled.
 */
@Composable
fun PreferenceEntry(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    content: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick
                )
                .alpha(if (isEnabled) 1f else 0.5f)
                .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                icon()
            }

            Spacer(Modifier.width(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            content?.invoke()
        }

        if (trailingContent != null) {
            Spacer(Modifier.width(12.dp))

            trailingContent()
        }
    }
}

/**
 * A composable function to display a switcher preference.
 *
 * @param modifier The modifier for customizing the appearance and behavior of the switcher preference.
 * @param title The title of the switcher preference.
 * @param description An optional description of the switcher preference.
 * @param icon An optional icon to display with the switcher preference.
 * @param checked The current state of the switch (checked or unchecked).
 * @param onCheckedChange The callback to execute when the switch state changes.
 * @param isEnabled A flag indicating whether the switcher preference is enabled or disabled.
 */
@Composable
fun SwitcherPreference(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    PreferenceEntry(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        onClick = { onCheckedChange(!checked) },
        isEnabled = isEnabled
    )
}

/**
 * A composable function to display a property preference.
 *
 * @param modifier The modifier for customizing the appearance and behavior of the property preference.
 * @param title The title of the property preference.
 * @param description An optional description of the property preference.
 * @param icon An optional icon to display with the property preference.
 * @param currentValue The current value of the property.
 * @param onClick The callback to execute when the property preference is clicked.
 * @param isEnabled A flag indicating whether the property preference is enabled or disabled.
 */
@Composable
fun PropertyPreference(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    currentValue: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    PreferenceEntry(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        trailingContent = {
            Text(
                text = currentValue,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        onClick = { onClick() },
        isEnabled = isEnabled
    )
}

@Preview
@Composable
fun PreferencesPreview() {
    Surface {
        Column {
            PreferenceGroupTitle(
                title = "Group title"
            )

            SwitcherPreference(
                title = "First Switcher title",
                checked = true,
                onCheckedChange = {}
            )

            SwitcherPreference(
                title = "Second Switcher title",
                description = "Second Switcher description",
                checked = false,
                onCheckedChange = {}
            )

            PropertyPreference(
                title = "Property title",
                currentValue = "Value",
                onClick = {}
            )
        }
    }
}
