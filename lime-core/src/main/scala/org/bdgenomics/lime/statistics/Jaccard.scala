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
package org.bdgenomics.lime.statistics

import org.bdgenomics.adam.rdd.GenomicRDD
import org.bdgenomics.lime.op.{ ShuffleIntersection, ShuffleMerge }

import scala.reflect.ClassTag

class JaccardDistance[T, U <: GenomicRDD[T, U], X, Y <: GenomicRDD[X, Y]](leftRdd: GenomicRDD[T, U],
                                                                          rightRdd: GenomicRDD[X, Y]) extends Statistic[T, X] {

  def compute()(implicit tTag: ClassTag[T], xTag: ClassTag[X]): StatisticResult = {

    val leftMergedRDD = ShuffleMerge(leftRdd).compute()
    val rightMergedRDD = ShuffleMerge(rightRdd).compute()

    val intersectedRdd = ShuffleIntersection(leftMergedRDD, rightMergedRDD)
      .compute()

    val intersectLength = intersectedRdd.rdd.flatMap(f => intersectedRdd.regionFn(f).map(_.length)).sum()
    val unionLength = leftMergedRDD.rdd.flatMap(f => leftMergedRDD.regionFn(f).map(_.length)).sum() + rightMergedRDD.rdd.flatMap(f => rightMergedRDD.regionFn(f).map(_.length)).sum()
    val jaccardDist = intersectLength / (unionLength - intersectLength)
    val nIntersections = intersectedRdd.rdd.count()

    JaccardStatistic(intersectLength.toLong, (unionLength - intersectLength).toLong, jaccardDist, nIntersections)

  }

}

private case class JaccardStatistic(intersectLength: Long,
                                    unionLength: Long,
                                    jaccardDist: Double,
                                    nIntersections: Long) extends StatisticResult {

  override def toString(): String = {

    "intersection\tunion-intersection\tjaccard\tn_intersections\n" +
      s"$intersectLength\t$unionLength\t$jaccardDist\t$nIntersections"
  }
}
