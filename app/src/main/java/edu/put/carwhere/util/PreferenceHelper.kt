import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean, email: String?, name: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGED_IN", isLoggedIn)
        editor.putString("NAME", name)
        editor.putString("EMAIL", email)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false)
    }

    fun email(): String {
        return sharedPreferences.getString("EMAIL", "") ?: ""
    }

    fun name(): String {
        return sharedPreferences.getString("NAME", "") ?: ""
    }
}
