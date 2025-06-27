/*
 * Copyright 2025 INOVA IT d.o.o.
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

import nl.littlerobots.vcu.plugin.resolver.ModuleVersionCandidate
import nl.littlerobots.vcu.plugin.versionSelector

/*
 * Copyright 2024 INOVA IT d.o.o.
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

// We should not use any allprojects {} calls, to conform with future project isolation
// See https://gradle.github.io/configuration-cache/#project_isolation

plugins {
   id("kotlinova")
   alias(libs.plugins.nexusPublish)
   alias(libs.plugins.versionCatalogUpdate)
}

if (properties.containsKey("ossrhUsername")) {
   nexusStaging {
      username = property("ossrhUsername") as String
      password = property("ossrhPassword") as String
      packageGroup = "si.inova"
      stagingRepositoryId = property("ossrhRepId") as String
   }
}

versionCatalogUpdate {
   catalogFile.set(file("config/libs.toml"))

   fun ModuleVersionCandidate.newlyContains(keyword: String): Boolean {
      return !currentVersion.contains(keyword, ignoreCase = true) && candidate.version.contains(keyword, ignoreCase = true)
   }

   versionSelector {
      !it.newlyContains("alpha") &&
         !it.newlyContains("beta") &&
         !it.newlyContains("RC") &&
         !it.newlyContains("M") &&
         !it.newlyContains("eap") &&
         !it.newlyContains("dev") &&
         !it.newlyContains("pre")
   }
}
