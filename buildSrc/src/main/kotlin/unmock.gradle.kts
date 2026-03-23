/*
 * Copyright 2026 INOVA IT d.o.o.
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

import de.mobilej.unmock.UnMockExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

apply(plugin = "de.mobilej.unmock")

configure<UnMockExtension> {
   keepStartingWith("android.content.ComponentName")
   keepStartingWith("android.content.Intent")
   keepStartingWith("android.content.ContentProviderOperation")
   keepStartingWith("android.content.ContentProviderResult")
   keepStartingWith("android.content.ContentUris")
   keepStartingWith("android.content.ContentValues")
   keep("android.content.Context")
   keepStartingWith("android.content.res.Configuration")
   keepStartingWith("android.content.UriMatcher")
   keep("android.database.AbstractCursor")
   keep("android.database.CrossProcessCursor")
   keepStartingWith("android.database.MatrixCursor")
   keep("android.location.Location")
   keep("android.net.Uri")
   keep("android.net.UriCodec")
   keep("android.os.BaseBundle")
   keep("android.os.Bundle")
   keep("android.os.BadTypeParcelableException")
   keep("android.os.Parcel")
   keepStartingWith("android.text.")
   keepStartingWith("android.util.")
   keep("android.view.ContextThemeWrapper")
   keep("android.widget.BaseAdapter")
   keep("android.widget.ArrayAdapter")
   keepStartingWith("com.android.internal.R")
   keepStartingWith("com.android.internal.util.")
   keepStartingWith("org.")
   keepStartingWith("libcore.")
}

dependencies {
   add("unmock", libs.unmock.androidJar)
}
