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

// Copyright 2024 HM Revenue & Customs


package uk.gov.hmrc.perftests.credentialManagement.requests

import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._
import uk.gov.hmrc.perftests.credentialManagement.common.RequestFunctions._

object GNAPAuthRequests {

  val flushAllCookies: ActionBuilder =
    exec(flushCookieJar).actionBuilders.head

  val navigateToCentralAuth: HttpRequestBuilder =
    http("Get Navigate to one login journey")
      .get(s"$caCanaryFeServiceUrl/centralised-authorisation-canary/CL_200")
      .check(
        currentLocationRegex("(.*)/CL_200"),
        status.is(303),
        header("Location").saveAs("confirmYourEmailAddressPage")
      )

  // Private Beta Pages Start

  val redirectToConfirmYourGovUkOneLoginEmailAddressPage: HttpRequestBuilder =
    if (runLocal) {
      http("Get Redirect to Confirm your GOV.UK OneLogin email address page")
        .get("${confirmYourEmailAddressPage}")
        .check(saveCsrfToken)
        .check(
          status.is(200)
        )
    } else {
      http("Get Redirect to Confirm your GOV.UK OneLogin email address page")
        .get(s"$identityProviderGatewayFrontendUrl$${confirmYourEmailAddressPage}")
        .check(saveCsrfToken)
        .check(
          status.is(200)
        )
    }

  val postToConfirmYourGovUkOneLoginEmailAddressPage: HttpRequestBuilder =
    if (runLocal) {
      http("Post to Confirm your GOV.UK OneLogin email address page")
        .post("${confirmYourEmailAddressPage}")
        .formParam("""csrfToken""", """${csrfToken}""")
        .formParam("""signInType""", "oneLogin")
        .check(status.is(303), header("Location").saveAs("confirmYourEmailAddressPageLocation"))
    } else {
      http("Post to Confirm your GOV.UK OneLogin email address page")
        .post(s"$identityProviderGatewayFrontendUrl$${confirmYourEmailAddressPage}")
        .formParam("""csrfToken""", """${csrfToken}""")
        .formParam("""signInType""", "oneLogin")
        .check(status.is(303), header("Location").saveAs("confirmYourEmailAddressPageLocation"))
    }

  val getToConfirmYourGovUkOneLoginEmailAddressPage: HttpRequestBuilder =
    http("Get to Confirm your GOV.UK OneLogin email address page")
      .get(s"$identityProviderGatewayFrontendUrl$${confirmYourEmailAddressPageLocation}")
      .check(saveCsrfToken)
      .check(
        status.is(200)
      )

  val postToConfirmYourGovUkOneLoginEmailAddressPageLocation: HttpRequestBuilder =
    http("Post to Confirm your GOV.UK OneLogin email address Location page")
      .post(s"$identityProviderGatewayFrontendUrl$${confirmYourEmailAddressPageLocation}")
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("""profile""", "")
      .formParam("""email-address""", StringBody("${randomEmail}"))
      .check(saveOlfgJourneyId, status.is(303), header("Location").saveAs("startOlfgJourney"))

  val getStartOlfgJourney: HttpRequestBuilder =
    http("Get to start olfg JourneyID")
      .get(s"$${startOlfgJourney}")
      .check(
        saveOlfgNonce,
        status.is(303),
        header("Location").saveAs("signInPage")
      )

  // Private Beta Pages End

  val redirectToSignInMethodPage: HttpRequestBuilder = {
    if (runLocal) {
      http("Get Redirect to Sign In Method page")
        .get("${signInPage}")
        .check(
          status.is(200)
        )
    } else {
      http("Get Redirect to Sign In Method page")
        .get("${signInPage}")
        .check(
          status.is(200)
        )
    }
  }

  val postOneLoginSignInMethodPage: HttpRequestBuilder =
    if (runLocal) {
      http("Post Sign In Method Page with One Login Selected")
        .post("${signInPage}")
        .formParam("""csrfToken""", """${csrfToken}""")
        .formParam("""signInType""", "oneLogin")
        .check(
          saveOlfgJourneyId,
          status.is(303),
          header("Location").saveAs("authnStartUrl"))
    } else {
      http("Post Sign In Method Page with One Login Selected")
        .post(s"$oneLoginGatewayFeUrl$${signInPage}")
        .formParam("""csrfToken""", """${csrfToken}""")
        .formParam("""signInType""", "oneLogin")
        .check(
          saveOlfgJourneyId,
          status.is(303),
          header("Location").saveAs("authnStartUrl"))
    }

  val getOneLoginGatewayStartEndpoint: HttpRequestBuilder =
    http("Get One Login Gateway Initialise Endpoint")
      .get("${authnStartUrl}")
      .check(
        saveOlfgSignedJWT,
        saveOlfgNonce,
        status.is(303),
        header("Location").saveAs("authnAuthorizeUrl"))

  val redirectToOneLoginGatewayStubPage: HttpRequestBuilder =
    http("Redirect to One Login Gateway Stub page")
      .get("${authnAuthorizeUrl}")
      .check(status.is(200))


  val postOneLoginGatewayStubPage: HttpRequestBuilder =
    http("Post One Login Gateway Stub Page")
      .post(s"$oneLoginStubUrl/authorize")
      .formParam("""state""", """${olfgJourneyId}""")
      .formParam("""nonce""", """${olfgNonce}""")
      .formParam("""vtr""", """["Cl.Cm"]""")
      .formParam("""isReauth""", """false""")
      .formParam("""userInfo.success""", """true""")
      .formParam("""userInfo.sub""", StringBody("${randomIdentityProviderId}"))
      .formParam("""userInfo.email""", StringBody("${randomEmail}"))
      .formParam("""submit""", """submit""")
      .check(
        saveOlfgContinueCode,
        status.is(303),
        header("Location").saveAs("oneLoginGatewayContinueUrl")
      )

  val redirectToOneLoginGatewayContinueEndpoint: HttpRequestBuilder =
    http("Redirect to One Login Gateway Continue Endpoint")
      .get("${oneLoginGatewayContinueUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("AuthorizeCompleteUrl")
      )


  val navigateToCompleteFixerJourney: HttpRequestBuilder =
    if (runLocal) {
      http("Navigate to complete fixer journey")
        .get("${AuthorizeCompleteUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("interactLocationUrl")
        )
    } else {
      http("Navigate to complete fixer journey")
        .get(s"$identityProviderGatewayFrontendUrl$${AuthorizeCompleteUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("interactLocationUrl")
        )
    }

  val redirectToLocationEndpoint: HttpRequestBuilder =
    if (runLocal) {
      http("Redirect to Location/Interaction Complete Endpoint")
        .get("${interactLocationUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("centralAuthHashUrl"),
          saveHashInteractRef,
          currentLocationRegex("(.*)/location")
        )
    } else {
      http("Redirect to Location/Interaction Complete Endpoint")
        .get(s"$authorisationServerFeUrl$${interactLocationUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("centralAuthHashUrl"),
          saveHashInteractRef,
          currentLocationRegex("(.*)/location")
        )
    }


  val redirectToCentralAuth: HttpRequestBuilder =
    if (runLocal) {
      http("Redirect to back to Central Auth Canary Hash")
        .get("${centralAuthHashUrl}")
        .check(
          status.is(303)
        )
    } else {
      http("Redirect to back to Central Auth Canary Hash")
        .get(s"$caCanaryFeServiceUrl$${centralAuthHashUrl}")
        .check(
          status.is(303)
        )
    }

  val getCentralAuthCl200: HttpRequestBuilder =
    http("Get Central Auth CL200")
      .get(s"$caCanaryFeServiceUrl/centralised-authorisation-canary/CL_200")
      .check(currentLocationRegex("(.*)/centralised-authorisation-canary/CL_200"))
      .check(
        status.is(303),
        header("Location").saveAs("cL200Redirect")
      )

  val getIdentityAuthorizeVerificationRedirect: HttpRequestBuilder =
    if (runLocal) {
      http("Redirect to One login authorize verification url")
        .get("${cL200Redirect}")
        .check(
          status.is(303),
          saveOlfgJourneyId,
          header("Location").saveAs("ivStartUrl")
        )
    } else {
      http("Redirect to One login authorize verification url")
        .get(s"$identityProviderGatewayFrontendUrl$${cL200Redirect}")
        .check(
          status.is(303),
          saveOlfgJourneyId,
          header("Location").saveAs("ivStartUrl")
        )
    }

  val getIvStartUrl: HttpRequestBuilder =
    http("GET One Login IV start url")
      .get("${ivStartUrl}")
      .check(
        status.is(303),
        saveOlfgSignedJWT,
        saveOlfgNonce,
        header("Location").saveAs("ivAuthorizeUrl")
      )

  val getIvAuthorizePage: HttpRequestBuilder =
    http("GET One Login Authorize url")
      .get("${ivAuthorizeUrl}")
      .check(
        status.is(200),
        currentLocationRegex(s"$oneLoginStubUrl/authorize?(.*)")
      )

  val postIvAuthorizePage: HttpRequestBuilder =
    http("Post IV Authorize Stub Page")
      .post(s"$oneLoginStubUrl/authorizeIv")
      .formParam("""state""", """${olfgJourneyId}""")
      .formParam("""nonce""", """${olfgNonce}""")
      .formParam("""vtr""", """["Cl.Cm.P2"]""")
      .formParam("""userInfo.success""", """true""")
      .formParam("""userInfo.sub""", StringBody("${randomIdentityProviderId}"))
      .formParam("""userInfo.email""", StringBody("${randomEmail}"))
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[0].nameParts[0].type", "GivenName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[0].nameParts[0].value", "Jim")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[0].nameParts[1].type", "FamilyName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[0].nameParts[1].value", "Ferguson")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[0].validUntil", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[1].nameParts[2].type", "GivenName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[1].nameParts[2].value", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[1].nameParts[3].type", "FamilyName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[1].nameParts[3].value", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[1].validUntil", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[2].nameParts[4].type", "GivenName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[2].nameParts[4].value", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[2].nameParts[5].type", "FamilyName")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[2].nameParts[5].value", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.name[2].validUntil", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.birthDate[0].value", "1948-04-23")
      .formParam("userInfo.verifiableCredentials.credentialSubject.birthDate[1].value", "")
      .formParam("userInfo.verifiableCredentials.credentialSubject.birthDate[2].value", "")
      .formParam("userInfo.failureReason", "")
      .formParam("userInfo.otherFailureReason", "")
      .formParam("userInfo.failureDescription", "")
      .formParam("userInfo.returnCode", "")
      .formParam("submit", "submit")
      .check(
        saveOlfgContinueCode,
        status.is(303),
        header("Location").saveAs("oneLoginGatewayIVContinueUrl")
      )


  val getIvContinueUrl: HttpRequestBuilder =
    http("GET One Login Continue url")
      .get("${oneLoginGatewayIVContinueUrl}")
      .check(
        status.is(303),
        currentLocationRegex(s"$oneLoginGatewayFeUrl/sign-in-to-hmrc-online-services/one-login/continue?(.*)"),
        header("Location").saveAs("getAuthorizeCompleteUrl")
      )

  val getIvAuthorizeCompleteUrl: HttpRequestBuilder =
    if (runLocal) {
      http("GET One Login authorize complete url")
        .get("${getAuthorizeCompleteUrl}")
        .check(
          status.is(303),
          currentLocationRegex(s"$identityProviderGatewayFrontendUrl/sign-in-to-hmrc-online-services/identity/authorize/complete/(.*)"),
          header("Location").saveAs("interactLocationUrl")
        )
    } else {
      http("GET One Login authorize complete url")
        .get(s"$identityProviderGatewayFrontendUrl$${getAuthorizeCompleteUrl}")
        .check(
          status.is(303),
          currentLocationRegex(s"$identityProviderGatewayFrontendUrl/sign-in-to-hmrc-online-services/identity/authorize/complete/(.*)"),
          header("Location").saveAs("interactLocationUrl")
        )
    }
}

