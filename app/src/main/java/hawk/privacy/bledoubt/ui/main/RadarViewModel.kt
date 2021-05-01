package hawk.privacy.bledoubt.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RadarViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val isRadarEnabled = MutableLiveData<Boolean>()


    override fun onCleared() {
        super.onCleared()
        // Dispose of subscriptions
    }
}
