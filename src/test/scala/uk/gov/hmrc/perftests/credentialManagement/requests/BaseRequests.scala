/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Copyright 2025 HM Revenue & Customs

package uk.gov.hmrc.perftests.credentialManagement.requests

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
import jodd.lagarto.dom.NodeSelector
import uk.gov.hmrc.performance.conf.ServicesConfiguration

import scala.util.Random

trait BaseRequests extends ServicesConfiguration {

  def saveCsrfToken: CheckBuilder[CssCheckType, NodeSelector, String]      = css("input[name='csrfToken']", "value").optional.saveAs("csrfToken")
   def saveNino: CheckBuilder[CssCheckType, NodeSelector, String]           = css("#ninoAccessChoice", "value").optional.saveAs("testOnlyNino")

  val feeder: Iterator[Map[String, String]] = Iterator.continually {
    Map(
      "randomIdentityProviderId" -> s"perf_${Random.alphanumeric.take(30).mkString}",
      "randomEmail"              -> s"perf_${Random.alphanumeric.take(30).mkString}@example.com",
      "randomScpCredId"          -> s"${Random.between(100000000000L, 1000000000000L).toString}"
    )
  }

}
