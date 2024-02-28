package dev.medzik.android.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> ComboBoxDropdown(
    values: Array<T>,
    value: T,
    onValueChange: (T) -> Unit,
    label: (@Composable () -> Unit)? = null,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier
) {
    val (expanded, setExpanded) =
        remember {
            mutableStateOf(false)
        }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded
    ) {
        OutlinedTextField(
            modifier = modifier.menuAnchor(),
            value = value.name,
            onValueChange = {},
            singleLine = true,
            label = label,
            readOnly = true,
            trailingIcon = {
                val iconRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "")
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    modifier = Modifier.rotate(iconRotation),
                    contentDescription = null
                )
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            values.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        setExpanded(false)
                        onValueChange(it)
                    }
                )
            }
        }
    }
}
