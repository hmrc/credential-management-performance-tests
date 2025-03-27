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
  def saveJourneyId: CheckBuilder[CssCheckType, NodeSelector, String]      = css("form[method='POST']", "action".takeRight(36)).optional.saveAs("journeyId")
  def saveRetryJourneyId: CheckBuilder[CssCheckType, NodeSelector, String] = css("a[href*=link-records]", "href").optional.saveAs("retryJourneyId")
  def saveNino: CheckBuilder[CssCheckType, NodeSelector, String]           = css("#ninoAccessChoice", "value").optional.saveAs("testOnlyNino")
  def saveFormPostUrl: CheckBuilder[CssCheckType, NodeSelector, String] = css("form[method='POST']", "action").optional.saveAs("ivStubPostUrl")
  def saveSimplifiedJourneyUrl: CheckBuilder[CssCheckType, NodeSelector, String] = css("a[role='button']", "href").optional.saveAs("simplifiedStubUrl")
  def saveState: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='state']", "value").optional.saveAs("state")
  def saveNonce: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='nonce']", "value").optional.saveAs("nonce")
  def saveIvGuidanceHashUrl: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='IvGuidanceHashUrl']", "value").optional.saveAs("ivGuidanceHashUrl")

  val feeder: Iterator[Map[String, String]] = Iterator.continually {
    def generateRandomString: String = {
      val firstAlphabet = "ABCEHJKL"
      val digits = "123456"
      val lastAlphabet = "D"
      val random = new Random

      val firstTwoChars = (1 to 2).map(_ => firstAlphabet(random.nextInt(firstAlphabet.length))).mkString
      val middleFiveDigits = (1 to 6).map(_ => digits(random.nextInt(digits.length))).mkString
      val lastChar = lastAlphabet(random.nextInt(lastAlphabet.length))

      s"$firstTwoChars$middleFiveDigits$lastChar"
    }

    Map(
      "randomIdentityProviderId" -> s"perf_${Random.alphanumeric.take(30).mkString}",
      "randomEmail"              -> s"perf_${Random.alphanumeric.take(30).mkString}@example.com",
      "randomScpCredId"          -> s"${Random.between(100000000000L, 1000000000000L).toString}",
      "randomNino"               -> generateRandomString
    )
  }


}
