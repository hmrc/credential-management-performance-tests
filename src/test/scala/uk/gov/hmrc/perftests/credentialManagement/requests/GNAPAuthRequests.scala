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

package uk.gov.hmrc.perftests.credentialManagement.requests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._
import uk.gov.hmrc.perftests.credentialManagement.common.RequestFunctions._

object GNAPAuthRequests {

  val redirectToInteractPage: HttpRequestBuilder =
    http("Redirect to Interact Page")
      .get(s"$authorisationServerFeUrl/interact/$${interactRef}")
      .check(saveInitialiseKey)
      .check(status.is(303))

  val navigateToOneLoginSignInPageNoToken: HttpRequestBuilder =
    http("Navigate to One Login Sign In Page Without a Gnap Token")
      .get(s"$caCanaryFeServiceUrl/test")
      .check(currentLocationRegex("(.*)/test"))
      .check(saveInteractRef)
      .check(status.is(303))

  val redirectToSignInMethodPage: HttpRequestBuilder = {
    http("Redirect to Sign In Method page")
      .get(s"""$identityProviderGatewayFrontendUrl/sign-in-to-hmrc-online-services/identity/sign-in/$${initialiseKey}""")
      .check(saveCsrfToken)
      .check(status.is(200))
  }

  // At this point, IDP-GW will call OL-GW (BE) to initialise the journey
  val postOneLoginSignInMethodPage: HttpRequestBuilder =
    http("Post Sign In Method Page with One Login Selected")
      .post(s"$identityProviderGatewayFrontendUrl/sign-in-to-hmrc-online-services/identity/sign-in/$${initialiseKey}")
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("""signInType""", "oneLogin")
      .check(saveOlfgJourneyId)
      .check(status.is(303))

  val getOneLoginGatewayInitialiseEndpoint: HttpRequestBuilder =
    http("Get One Login Gateway Initialise Endpoint")
      .get(s"$oneLoginGatewayFeUrl/start?olfgJourneyId=$${olfgJourneyId}")
      .check(saveOlfgSignedJWT)
      .check(saveOlfgNonce)
      .check(status.is(303))

  val redirectToOneLoginGatewayStubPage: HttpRequestBuilder = {
    http("Redirect to One Login Gateway Stub page")
      .get(s"$oneLoginStubUrl/authorize?response_type=code&scope=openid,email&client_id=one-login-gateway&request=$${olfgSignedJWT}")
      .check(status.is(200))
  }

  val postOneLoginGatewayStubPage: HttpRequestBuilder = {
    http("Post One Login Gateway Stub Page")
      .post(s"$oneLoginStubUrl/authorize")
      .formParam("""state""", """${olfgJourneyId}""")
      .formParam("""nonce""", """${olfgNonce}""")
      .formParam("""vtr""", """["Cl.Cm"]""")
      .formParam("""isReauth""", """false""")
      .formParam("""userInfo.success""", """true""")
      .formParam("""userInfo.sub""", StringBody("${randomIdentityProviderId}"))
      .formParam("""userInfo.email""", """66666666email@email.com""")
      .formParam("""submit""", """submit""")
      .check(saveOlfgContinueCode)
      .check(status.is(303))
  }

  val redirectToOneLoginGatewayContinueEndpoint: HttpRequestBuilder = {
    http("Redirect to One Login Gateway Continue Endpoint")
      .get(s"$oneLoginGatewayFeUrl/continue?code=$${olfgContinueCode}&state=$${olfgJourneyId}")
      .check(status.is(303))
  }

  val navigateToCompleteFixerJourney: HttpRequestBuilder =
    http("Navigate to complete fixer journey")
      .get(s"$identityProviderGatewayFrontendUrl/sign-in-to-hmrc-online-services/identity/authorize/complete/$${initialiseKey}")
      .check(status.is(303))

  val redirectToLocationEndpoint: HttpRequestBuilder =
    http("Redirect to Location/Interaction Complete Endpoint")
      .get(s"$authorisationServerFeUrl/interact/$${interactRef}/location")
      .check(currentLocationRegex(s"$authorisationServerFeUrl/interact/$${interactRef}/location"))
      .check(saveHashInteractRef)
      .disableFollowRedirect
      .check(status.is(303))

  val redirectToOneLoginSignInPage: HttpRequestBuilder =
    http("Redirect to One Login Sign In Page with a Gnap Token")
      .get(s"$caCanaryFeServiceUrl/test?hash=$${hashInteractRef}")
      .check(currentLocationRegex(s"/centralised-authorisation-canary/test"))
      .check(status.is(303))

  val getOneLoginSignInPage: HttpRequestBuilder =
    http(s"Get One Login Sign In Page with a Gnap Token")
      .get(s"$caCanaryFeServiceUrl/test")
      .check(currentLocationRegex(s"/centralised-authorisation-canary/test"))
      .check(status.is(200))

}

