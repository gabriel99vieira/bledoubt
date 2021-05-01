package hawk.privacy.bledoubt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hawk.privacy.bledoubt.ui.main.RadarFragment

class MainFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RadarFragment.newInstance())
                    .commitNow()
        }
    }
}
