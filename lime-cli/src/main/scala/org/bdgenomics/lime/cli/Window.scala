/**
 * Licensed to Big Data Genomics (BDG) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The BDG licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.lime.cli

import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.feature.FeatureRDD
import org.bdgenomics.lime.op.ShuffleWindow
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.Argument

object Window extends BDGCommandCompanion {
  val commandName = "window"
  val commandDescription = "Compute nearby regions between two inputs"

  def apply(cmdLine: Array[String]) = {
    new Window(Args4j[WindowArgs](cmdLine))
  }

  class WindowArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
    @Argument(required = true,
      metaVar = "INPUT1",
      usage = "The first file for window",
      index = 0)
    var leftInput: String = null
    @Argument(required = true,
      metaVar = "INPUT2",
      usage = "The second file for window",
      index = 1)
    var rightInput: String = null

    override var sortFastqOutput: Boolean = false
    override var asSingleFile: Boolean = false
    override var deferMerging: Boolean = false
    override var outputPath: String = ""
    override var disableFastConcat: Boolean = false
  }

  class Window(protected val args: WindowArgs) extends BDGSparkCommand[WindowArgs] {
    val companion = Window

    def run(sc: SparkContext) {
      val leftGenomicRDD: FeatureRDD = sc.loadBed(args.leftInput)
      val rightGenomicRDD = sc.loadBed(args.rightInput)

      ShuffleWindow(leftGenomicRDD, rightGenomicRDD)
        .compute()
        .rdd
        .collect()
        .foreach(println)
    }
  }
}
