/*
 * Copyright 2023 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("OverridingDeprecatedMember", "DEPRECATION", "ExceptionRaisedInUnexpectedLocation")

package si.inova.kotlinova.core.test.fakes

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.app.Fragment
import android.app.FragmentManager
import android.app.LoaderManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.SharedElementCallback
import android.app.TaskStackBuilder
import android.app.VoiceInteractor
import android.app.assist.AssistContent
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.UserHandle
import android.transition.Scene
import android.transition.TransitionManager
import android.view.ActionMode
import android.view.ContextMenu
import android.view.Display
import android.view.DragAndDropPermissions
import android.view.DragEvent
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.SearchEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toolbar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.Executor

/**
 * Fake activity class that can be used as a test replacement.
 *
 * It allows providing resource (see [FakeResources]) and list of started intents ([startedActivities]]).
 *
 * To get this to work in regular unit tests, you need to use the Unmock plugin: https://github.com/bjoernQ/unmock-plugin
 */
@Suppress("OVERRIDE_DEPRECATION")
@SuppressLint("MissingSuperCall")
class FakeActivity : Activity() {
   val resources = FakeResources()

   private val _startedActivities = ArrayList<Intent>()
   private val _startedServices = ArrayList<ServiceStart>()
   private val _startedBroadcasts = ArrayList<Intent>()

   var finished: Boolean = false
      private set

   var providedPackageName = "com.testpackage"

   val startedActivities: List<Intent>
      get() = _startedActivities
   val startedServices: List<ServiceStart>
      get() = _startedServices
   val startedBroadcasts: List<Intent>
      get() = _startedBroadcasts

   fun reset() {
      _startedServices.clear()
      _startedBroadcasts.clear()
      _startedActivities.clear()
   }

   data class ServiceStart(val intent: Intent, val foreground: Boolean)

   // ========================== CONTEXT FAKES =============================

   override fun getApplicationContext(): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setWallpaper(bitmap: Bitmap?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setWallpaper(data: InputStream?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun removeStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkCallingOrSelfPermission(permission: String): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getClassLoader(): ClassLoader {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getObbDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkUriPermission(
      uri: Uri?,
      readPermission: String?,
      writePermission: String?,
      pid: Int,
      uid: Int,
      modeFlags: Int
   ): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getExternalFilesDirs(type: String?): Array<File> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getPackageResourcePath(): String {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun deleteSharedPreferences(name: String?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSender(
      intent: IntentSender?,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSender(
      intent: IntentSender?,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int,
      options: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getDataDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getWallpaper(): Drawable {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isDeviceProtectedStorage(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getExternalFilesDir(type: String?): File? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendBroadcastAsUser(
      intent: Intent?,
      user: UserHandle?,
      receiverPermission: String?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getExternalCacheDir(): File? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getDatabasePath(name: String?): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getFileStreamPath(name: String?): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun stopService(service: Intent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkSelfPermission(permission: String): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerReceiver(
      receiver: BroadcastReceiver?,
      filter: IntentFilter?,
      flags: Int
   ): Intent? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerReceiver(
      receiver: BroadcastReceiver?,
      filter: IntentFilter?,
      broadcastPermission: String?,
      scheduler: Handler?
   ): Intent? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerReceiver(
      receiver: BroadcastReceiver?,
      filter: IntentFilter?,
      broadcastPermission: String?,
      scheduler: Handler?,
      flags: Int
   ): Intent? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getSystemServiceName(serviceClass: Class<*>): String? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getMainLooper(): Looper {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceCallingOrSelfPermission(permission: String, message: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getPackageCodePath(): String {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkCallingUriPermission(uri: Uri?, modeFlags: Int): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getWallpaperDesiredMinimumWidth(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createDeviceProtectedStorageContext(): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openFileInput(name: String?): FileInputStream {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getCodeCacheDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun deleteDatabase(name: String?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getAssets(): AssetManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getNoBackupFilesDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivities(intents: Array<out Intent>?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getResources(): Resources {
      return resources
   }

   override fun fileList(): Array<String> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTheme(resid: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun unregisterReceiver(receiver: BroadcastReceiver?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openFileOutput(name: String?, mode: Int): FileOutputStream {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendStickyOrderedBroadcast(
      intent: Intent?,
      resultReceiver: BroadcastReceiver?,
      scheduler: Handler?,
      initialCode: Int,
      initialData: String?,
      initialExtras: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getFilesDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendBroadcast(intent: Intent) {
      _startedBroadcasts.add(intent)
   }

   override fun sendBroadcast(intent: Intent, receiverPermission: String?) {
      _startedBroadcasts.add(intent)
   }

   override fun sendOrderedBroadcastAsUser(
      intent: Intent?,
      user: UserHandle?,
      receiverPermission: String?,
      resultReceiver: BroadcastReceiver?,
      scheduler: Handler?,
      initialCode: Int,
      initialData: String?,
      initialExtras: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun grantUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceCallingUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getCacheDir(): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun clearWallpaper() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendStickyOrderedBroadcastAsUser(
      intent: Intent?,
      user: UserHandle?,
      resultReceiver: BroadcastReceiver?,
      scheduler: Handler?,
      initialCode: Int,
      initialData: String?,
      initialExtras: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivity(intent: Intent) {
      _startedActivities.add(intent)
   }

   override fun startActivity(intent: Intent, options: Bundle?) {
      _startedActivities.add(intent)
   }

   override fun getPackageManager(): PackageManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openOrCreateDatabase(
      name: String?,
      mode: Int,
      factory: SQLiteDatabase.CursorFactory?
   ): SQLiteDatabase {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openOrCreateDatabase(
      name: String?,
      mode: Int,
      factory: SQLiteDatabase.CursorFactory?,
      errorHandler: DatabaseErrorHandler?
   ): SQLiteDatabase {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun deleteFile(name: String?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startService(service: Intent): ComponentName? {
      _startedServices.add(
         ServiceStart(
            service,
            false
         )
      )

      return null
   }

   override fun revokeUriPermission(uri: Uri?, modeFlags: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun revokeUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun moveDatabaseFrom(sourceContext: Context?, name: String?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startInstrumentation(
      className: ComponentName,
      profileFile: String?,
      arguments: Bundle?
   ): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendOrderedBroadcast(
      intent: Intent,
      receiverPermission: String?,
      resultReceiver: BroadcastReceiver?,
      scheduler: Handler?,
      initialCode: Int,
      initialData: String?,
      initialExtras: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun unbindService(conn: ServiceConnection) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getApplicationInfo(): ApplicationInfo {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getWallpaperDesiredMinimumHeight(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createDisplayContext(display: Display): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createContextForSplit(splitName: String?): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getTheme(): Resources.Theme {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getPackageName(): String {
      return providedPackageName
   }

   override fun getContentResolver(): ContentResolver {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getObbDirs(): Array<File> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getExternalMediaDirs(): Array<File> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun checkCallingPermission(permission: String): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getExternalCacheDirs(): Array<File> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun sendStickyBroadcast(intent: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceCallingPermission(permission: String, message: String?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun peekWallpaper(): Drawable {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getSystemService(name: String): Any {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startForegroundService(service: Intent): ComponentName? {
      _startedServices.add(
         ServiceStart(
            service,
            true
         )
      )

      return null
   }

   override fun getDir(name: String?, mode: Int): File {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun databaseList(): Array<String> {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createPackageContext(packageName: String?, flags: Int): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceUriPermission(
      uri: Uri?,
      pid: Int,
      uid: Int,
      modeFlags: Int,
      message: String?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enforceUriPermission(
      uri: Uri?,
      readPermission: String?,
      writePermission: String?,
      pid: Int,
      uid: Int,
      modeFlags: Int,
      message: String?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun removeStickyBroadcast(intent: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setExitSharedElementCallback(callback: SharedElementCallback?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onAttachedToWindow() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getWindowManager(): WindowManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startNextMatchingActivity(intent: Intent): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startNextMatchingActivity(intent: Intent, options: Bundle?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun <T : View?> findViewById(id: Int): T {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSenderForResult(
      intent: IntentSender?,
      requestCode: Int,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSenderForResult(
      intent: IntentSender?,
      requestCode: Int,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int,
      options: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun hasWindowFocus(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setPictureInPictureParams(params: PictureInPictureParams) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isImmersive(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isVoiceInteraction(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onProvideAssistData(data: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPause() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getIntent(): Intent {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isFinishing(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getMenuInflater(): MenuInflater {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onTouchEvent(event: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun takeKeyEvents(get: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setEnterSharedElementCallback(callback: SharedElementCallback?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityFromFragment(fragment: Fragment, intent: Intent?, requestCode: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityFromFragment(
      fragment: Fragment,
      intent: Intent?,
      requestCode: Int,
      options: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onActionModeFinished(mode: ActionMode?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getLayoutInflater(): LayoutInflater {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getLastNonConfigurationInstance(): Any? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onEnterAnimationComplete() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onKeyShortcut(keyCode: Int, event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun unregisterComponentCallbacks(callback: ComponentCallbacks?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setShowWhenLocked(showWhenLocked: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateNavigateUpTaskStack(builder: TaskStackBuilder?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun equals(other: Any?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerForContextMenu(view: View?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getTaskId(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getCallingActivity(): ComponentName? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPointerCaptureChanged(hasCapture: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun registerComponentCallbacks(callback: ComponentCallbacks?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun postponeEnterTransition() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateDescription(): CharSequence? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isTaskRoot(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPostResume() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun stopManagingCursor(c: Cursor?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getMaxNumPictureInPictureActions(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun addContentView(view: View?, params: ViewGroup.LayoutParams?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun triggerSearch(query: String?, appSearchData: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTaskDescription(taskDescription: ActivityManager.TaskDescription?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isLocalVoiceInteractionSupported(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onTrimMemory(level: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startLockTask() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTurnScreenOn(turnScreenOn: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onProvideKeyboardShortcuts(
      data: MutableList<KeyboardShortcutGroup>?,
      menu: Menu?,
      deviceId: Int
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setContentTransitionManager(tm: TransitionManager?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onAttachFragment(fragment: Fragment?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setVisible(visible: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isChangingConfigurations(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finishActivity(requestCode: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onActionModeStarted(mode: ActionMode?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isInMultiWindowMode(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTitleColor(textColor: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActionMode(callback: ActionMode.Callback?): ActionMode? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setContentView(layoutResID: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setContentView(view: View?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun stopLockTask() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onUserInteraction() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setImmersive(i: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getChangingConfigurations(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun shouldUpRecreateTask(targetIntent: Intent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun navigateUpTo(upIntent: Intent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isInPictureInPictureMode(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finish() {
      finished = true
   }

   override fun finishAffinity() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateDialog(id: Int): Dialog {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityFromChild(child: Activity, intent: Intent?, requestCode: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityFromChild(
      child: Activity,
      intent: Intent?,
      requestCode: Int,
      options: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enterPictureInPictureMode() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun enterPictureInPictureMode(params: PictureInPictureParams): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finishFromChild(child: Activity?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onWindowFocusChanged(hasFocus: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun moveTaskToBack(nonRoot: Boolean): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finishActivityFromChild(child: Activity, requestCode: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onDestroy() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getActionBar(): ActionBar? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateContextMenu(
      menu: ContextMenu?,
      v: View?,
      menuInfo: ContextMenu.ContextMenuInfo?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getContentTransitionManager(): TransitionManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onActivityReenter(resultCode: Int, data: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onBackPressed() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onLocalVoiceInteractionStopped() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun navigateUpToFromChild(child: Activity?, upIntent: Intent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setActionBar(toolbar: Toolbar?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onVisibleBehindCanceled() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openContextMenu(view: View?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getParentActivityIntent(): Intent? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun closeContextMenu() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPrepareDialog(id: Int, dialog: Dialog?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPrepareDialog(id: Int, dialog: Dialog?, args: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onNewIntent(intent: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getRequestedOrientation(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getVoiceInteractor(): VoiceInteractor {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onContentChanged() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun releaseInstance(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onSearchRequested(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getBaseContext(): Context {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onNavigateUpFromChild(child: Activity?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getReferrer(): Uri? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startLocalVoiceInteraction(privateOptions: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setRequestedOrientation(requestedOrientation: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onLowMemory() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun reportFullyDrawn() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onRetainNonConfigurationInstance(): Any {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startPostponedEnterTransition() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getLoaderManager(): LoaderManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isActivityTransitionRunning(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getMainExecutor(): Executor {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onStart() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun unregisterForContextMenu(view: View?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onProvideAssistContent(outContent: AssistContent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun stopLocalVoiceInteraction() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun recreate() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onOptionsMenuClosed(menu: Menu?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun createPendingResult(requestCode: Int, data: Intent, flags: Int): PendingIntent {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onUserLeaveHint() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startManagingCursor(c: Cursor?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getComponentName(): ComponentName {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onResume() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onTitleChanged(title: CharSequence?, color: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onProvideReferrer(): Uri {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onChildTitleChanged(childActivity: Activity?, title: CharSequence?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onStateNotSaved() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchGenericMotionEvent(ev: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun openOptionsMenu() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isRestricted(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchTrackballEvent(ev: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTitle(title: CharSequence?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setTitle(titleId: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isVoiceInteractionRoot(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun requestDragAndDropPermissions(event: DragEvent?): DragAndDropPermissions {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun hashCode(): Int {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun isDestroyed(): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setVrModeEnabled(enabled: Boolean, requestedComponent: ComponentName) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun toString(): String {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPostCreate(savedInstanceState: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSenderFromChild(
      child: Activity?,
      intent: IntentSender?,
      requestCode: Int,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startIntentSenderFromChild(
      child: Activity?,
      intent: IntentSender?,
      requestCode: Int,
      fillInIntent: Intent?,
      flagsMask: Int,
      flagsValues: Int,
      extraFlags: Int,
      options: Bundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onLocalVoiceInteractionStarted() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onTrackballEvent(event: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onDetachedFromWindow() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun closeOptionsMenu() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finishAndRemoveTask() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun requestVisibleBehind(visible: Boolean): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreatePanelView(featureId: Int): View? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPrepareNavigateUpTaskStack(builder: TaskStackBuilder?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityForResult(intent: Intent?, requestCode: Int) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onWindowStartingActionMode(
      callback: ActionMode.Callback?,
      type: Int
   ): ActionMode? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getLocalClassName(): String {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getPreferences(mode: Int): SharedPreferences {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onStop() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getCurrentFocus(): View? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onRestart() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityIfNeeded(intent: Intent, requestCode: Int): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startActivityIfNeeded(
      intent: Intent,
      requestCode: Int,
      options: Bundle?
   ): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setIntent(newIntent: Intent?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getFragmentManager(): FragmentManager {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getCallingPackage(): String? {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun showAssist(args: Bundle?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun startSearch(
      initialQuery: String?,
      selectInitialQuery: Boolean,
      appSearchData: Bundle?,
      globalSearch: Boolean
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun finishAfterTransition() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onRestoreInstanceState(
      savedInstanceState: Bundle?,
      persistentState: PersistableBundle?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPictureInPictureModeChanged(
      isInPictureInPictureMode: Boolean,
      newConfig: Configuration?
   ) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getContentScene(): Scene {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onCreateThumbnail(outBitmap: Bitmap?, canvas: Canvas?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun getWindow(): Window {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun invalidateOptionsMenu() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun attachBaseContext(newBase: Context?) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun showLockTaskEscapeMessage() {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }

   override fun setFinishOnTouchOutside(finish: Boolean) {
      throw UnsupportedOperationException("Operation not supported by FakeActivity")
   }
}
