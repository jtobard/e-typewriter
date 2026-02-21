package com.etypewriter.ahc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.etypewriter.ahc.ui.editor.EditorScreen
import com.etypewriter.ahc.ui.editor.EditorViewModel
import com.etypewriter.ahc.ui.theme.TypewriterTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EditorViewModel

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.openFile(it)
        }
    }

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let { viewModel.saveFile(it) }
    }

    private val saveFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let { viewModel.saveFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[EditorViewModel::class.java]

        setContent {
            TypewriterTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    EditorScreen(
                        viewModel = viewModel,
                        onNewFile = { viewModel.newFile() },
                        onOpenFile = { openFileLauncher.launch(arrayOf("text/*")) },
                        onSaveFile = {
                            val uri = viewModel.currentUri
                            if (uri != null) {
                                viewModel.saveFile(uri)
                            } else {
                                saveFileLauncher.launch(viewModel.fileName)
                            }
                        },
                    )
                }
            }
        }
    }
}
