package kz.petprojects.facerecognitionapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FaceContourViewModel() : ViewModel() {
    private val _showGlasses = MutableStateFlow(false)
    val showGlasses: StateFlow<Boolean> = _showGlasses

    fun toggleGlasses() {
        viewModelScope.launch {
            _showGlasses.value = !_showGlasses.value
        }
    }

}