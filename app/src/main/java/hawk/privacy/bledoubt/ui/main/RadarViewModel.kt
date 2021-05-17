package hawk.privacy.bledoubt.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RadarViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val isRadarEnabled = MutableLiveData<Boolean>()

    fun getIsRadarEnabled(): LiveData<Boolean> {
        return isRadarEnabled
    }

    fun setIsRadarEnabled(isEnabled: Boolean) {
        isRadarEnabled.value = isEnabled
    }


}
