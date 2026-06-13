package com.example.wheresxyz

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wheresxyz.ui.navigation.NavGraph
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WheresXYZTheme {
                val viewModel: com.example.wheresxyz.ui.viewmodel.AuthViewModel = hiltViewModel()
                NavGraph(viewModel = viewModel)
            }
        }
    }
}
