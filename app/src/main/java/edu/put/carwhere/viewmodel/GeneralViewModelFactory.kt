import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.put.carwhere.viewmodel.GeneralViewModel

class GeneralViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeneralViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeneralViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
