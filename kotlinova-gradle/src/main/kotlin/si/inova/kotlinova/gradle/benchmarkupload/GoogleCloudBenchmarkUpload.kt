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

package si.inova.kotlinova.gradle.benchmarkupload

import com.google.api.Metric
import com.google.cloud.monitoring.v3.MetricServiceClient
import com.google.monitoring.v3.CreateTimeSeriesRequest
import com.google.monitoring.v3.Point
import com.google.monitoring.v3.ProjectName
import com.google.monitoring.v3.TimeInterval
import com.google.monitoring.v3.TimeSeries
import com.google.monitoring.v3.TypedValue
import com.google.protobuf.util.Timestamps
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

private typealias JsonObject = Map<String, Any?>

@Suppress("UNCHECKED_CAST", "unused")
abstract class GoogleCloudBenchmarkUpload : DefaultTask() {
   @get:InputFiles
   abstract val benchmarkResultFiles: ConfigurableFileCollection

   @get:Input
   abstract val googleCloudProjectId: Property<String>

   @get:Input
   abstract val metricMap: MapProperty<String, String>

   @TaskAction
   fun parseResults() {
      val timeSeriesList = ArrayList<TimeSeries>()

      for (resultFile in benchmarkResultFiles.files) {
         val parsedJson = JsonSlurper().parse(resultFile) as JsonObject
         val benchmarks = parsedJson.list<JsonObject>("benchmarks")

         for (benchmark in benchmarks) {
            val benchmarkName = camelToSnake(benchmark.str("name"))
            val metrics = benchmark.obj("metrics") + benchmark.obj("sampledMetrics")

            for ((metricName, metricValues) in metrics) {
               val metricKey = metricMap.getting(metricName).orNull ?: error("Metric '$metricName' not provided in metricMap")

               for (value in metricValues as JsonObject) {
                  if (value.key == "runs") {
                     continue
                  }

                  val interval: TimeInterval = TimeInterval.newBuilder()
                     .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
                     .build()
                  val typedValue = TypedValue.newBuilder()
                     .setDoubleValue((value.value as Number).toDouble()).build()
                  val point: Point =
                     Point.newBuilder().setInterval(interval).setValue(typedValue)
                        .build()

                  val gcloudMetricPath = "$benchmarkName/$metricKey/${value.key.lowercase()}"
                  val timeSeries: TimeSeries = TimeSeries.newBuilder()
                     .setMetric(
                        Metric.newBuilder()
                           .setType("custom.googleapis.com/android/$gcloudMetricPath")
                           .build()
                     )
                     .addAllPoints(listOf(point))
                     .build()

                  timeSeriesList.add(timeSeries)
               }
            }
         }
      }

      MetricServiceClient.create().use { metricsClient ->
         val request = CreateTimeSeriesRequest.newBuilder()
            .setName(ProjectName.of(googleCloudProjectId.orNull ?: error("googleCloudProjectId not set")).toString())
            .addAllTimeSeries(timeSeriesList)
            .build()

         metricsClient.createTimeSeries(request)
      }
   }
}

private fun camelToSnake(str: String): String {
   // Empty String
   var result = ""

   // Append first character(in lower case)
   // to result string
   val c = str[0]
   result += c.lowercaseChar()

   // Traverse the string from
   // ist index to last index
   for (i in 1 until str.length) {
      val ch = str[i]

      // Check if the character is upper case
      // then append '_' and such character
      // (in lower case) to result string
      if (Character.isUpperCase(ch)) {
         result += '_'
         result = (result + ch.lowercaseChar())
      } else {
         result += ch
      }
   }

   // return the result
   return result
}

@Suppress("UNCHECKED_CAST")
private fun JsonObject.obj(key: String): JsonObject {
   return getValue(key) as JsonObject
}

@Suppress("UNCHECKED_CAST")
private fun <T> JsonObject.list(key: String): List<T> {
   return getValue(key) as List<T>
}

private fun JsonObject.str(key: String): String {
   return getValue(key) as String
}
