package actor.starintel.wear.ui

import android.app.RemoteInput
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.wear.input.RemoteInputIntentHelper

const val INPUT_KEY = "starintel.input"

fun wearInputIntent(title: String, label: String): Intent {
    val remoteInput = RemoteInput.Builder(INPUT_KEY)
        .setLabel(label)
        .setAllowFreeFormInput(true)
        .build()

    return RemoteInputIntentHelper.createActionRemoteInputIntent().also { intent ->
        RemoteInputIntentHelper.putTitleExtra(intent, title)
        RemoteInputIntentHelper.putConfirmLabelExtra(intent, "Done")
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
    }
}

fun resultText(result: ActivityResult): String? {
    return RemoteInput.getResultsFromIntent(result.data)
        ?.getCharSequence(INPUT_KEY)
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}
