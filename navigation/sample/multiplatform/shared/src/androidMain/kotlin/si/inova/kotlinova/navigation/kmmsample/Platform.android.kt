package si.inova.kotlinova.navigation.kmmsample

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import si.inova.kotlinova.navigation.di.NavigationInjection

class AndroidPlatform : Platform {
   override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun loadNavigationInjectionFactory(): NavigationInjection.Factory {
   val context = LocalContext.current
   return remember(context) {
      (context.applicationContext as MultiplatformDemoApplication).appGraph.getNavigationInjectionFactory()
   }
}
