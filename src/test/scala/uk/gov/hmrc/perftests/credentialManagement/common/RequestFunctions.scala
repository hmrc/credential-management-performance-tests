/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.credentialManagement.common

import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.http.Predef.{Response, headerRegex}
import io.gatling.http.check.header.HttpHeaderRegexCheckType


object RequestFunctions {

  val CsrfPattern = """<input type="hidden" name="csrfToken" value="([^"]+)""""
  val interactRefPattern: String = """/interact/([^"]+)"""
  val initialiseKeyPattern: String = """/sign-in-to-hmrc-online-services/identity/sign-in/([^"]+)"""
  val hashInteractRefPattern: String = """hash=([^"]+)"""
  val olfgJourneyIdPattern: String = """Id=([^"]+)"""
  val olfgSignedJWTPattern: String = """request=([^"]+)"""
  val olfgContinueCodePattern: String = """code=([^"]+)&state="""
  val bearerPattern: String = """^GNAP.*?(Bearer .*?)$|^(Bearer .*?)GNAP.*?$|^(Bearer .*?)$"""

  def saveCsrfToken: CheckBuilder[RegexCheckType, String, String] = regex(
    _ => CsrfPattern
  ).saveAs("csrfToken")

  def saveInteractRef: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", interactRefPattern
  ).saveAs("interactRef")

  def saveInitialiseKey: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", initialiseKeyPattern
  ).saveAs("initialiseKey")

  def saveHashInteractRef: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", hashInteractRefPattern
  ).saveAs("hashInteractRef")

  def saveOlfgJourneyId: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", olfgJourneyIdPattern
  ).saveAs("olfgJourneyId")

  def saveOlfgSignedJWT: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", olfgSignedJWTPattern
  ).saveAs("olfgSignedJWT")

  def saveOlfgNonce: CheckBuilder[HttpHeaderRegexCheckType, Response, String] =
    headerRegex("Location", olfgSignedJWTPattern)
      .transform(string => jwtClaims(string).getClaim("nonce") match {
        case s: String => s
        case x => throw new RuntimeException(s"got unexpected class: ${x.getClass}")
      })
      .saveAs("olfgNonce")

  def jwtClaims(jwt: String): JWTClaimsSet = SignedJWT.parse(jwt).getJWTClaimsSet

  def saveOlfgContinueCode: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Location", olfgContinueCodePattern
  ).saveAs("olfgContinueCode")

  def saveBearerToken: CheckBuilder[HttpHeaderRegexCheckType, Response, String] = headerRegex(
    "Authorization", bearerPattern
  ).saveAs("bearerToken")

}
