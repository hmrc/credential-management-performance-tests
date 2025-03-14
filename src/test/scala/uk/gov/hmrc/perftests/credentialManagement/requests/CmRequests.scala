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
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.http.Predef.{header, _}
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._

trait CmRequests extends BaseRequests {

  def postOneLoginAccountCreate: Seq[ActionBuilder] =
    exec(
      http("Create account in IPAC")
        .post(s"$ctxUrl/identity-provider-account-context/accounts")
        .body(StringBody(s"""|
        {
          "action": "create",
          "identityProviderId": "$${randomIdentityProviderId}",
          "identityProviderType": "ONE_LOGIN",
          "email": "$${randomEmail}"
        }
        """.stripMargin))
        .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "performance-tests"))
        .check(status.is(201), jsonPath("$..caUserId").saveAs("caUserId"))
        .check(jsonPath("$..contextId").saveAs("contextId"))
        .check(jsonPath("$..eacdUserId").saveAs("eacdUserId"))
        .check(jsonPath("$..email").saveAs("email"))
    ).feed(feeder).actionBuilders

  def postOneLoginAccountUpdate: Seq[ActionBuilder] = exec(
    http("Assign NINO to the account in IPAC")
      .post(s"$ctxUrl/identity-provider-account-context/contexts/individual")
      .body(StringBody(s"""|
        {
          "caUserId": "$${caUserId}",
          "nino": "$${randomNino}"
        }
        """.stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "performance-tests"))
      .check(status.is(201))
  ).actionBuilders

  def postEnrolmentStoreStubData: ActionBuilder =
    http("POST Enrolment store stub data")
      .post(s"$esStubDataUrl/enrolment-store-stub/data")
      .body(StringBody("""{
                         |  "groupId": "${contextId}",
                         |  "affinityGroup": "Individual",
                         |  "users": [
                         |    {
                         |      "credId": "${eacdUserId}",
                         |      "name": "Default User",
                         |      "email": "${email}",
                         |      "credentialRole": "Admin",
                         |      "description": "User Description"
                         |    }
                         |  ],
                         |  "enrolments": [
                         |    {
                         |      "serviceName": "IR-SA",
                         |      "identifiers": [
                         |        {
                         |          "key": "UTR",
                         |          "value": "123456"
                         |        }
                         |      ],
                         |      "enrolmentFriendlyName": "IR SA Enrolment",
                         |      "assignedUserCreds": [
                         |        "${eacdUserId}"
                         |      ],
                         |      "state": "Activated",
                         |      "enrolmentType": "principal",
                         |      "assignedToAll": false
                         |    },
                         |    {
                         |      "serviceName": "IR-SA",
                         |      "identifiers": [
                         |        {
                         |          "key": "UTR",
                         |          "value": "1234567891"
                         |        }
                         |      ],
                         |      "assignedUserCreds": [],
                         |      "state": "Activated",
                         |      "enrolmentType": "principal",
                         |      "assignedToAll": false
                         |    }
                         |  ]
                         |}""".stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "centralised-authorisation-server"))
      .check(
        status.is(204)
      )

  def getGuidancePageURL: ActionBuilder =
    if (runLocal) {
      http("GET the Guidance page")
        .get(s"$cmUrl/credential-management/guidance")
        .check(
          status.is(303),
          header("Location").saveAs("interactUrl")
        )
    } else {
      http("GET the Guidance page")
        .get("https://www.staging.tax.service.gov.uk" + s"/credential-management/guidance")
        .check(
          status.is(303),
          header("Location").saveAs("interactUrl")
        )
    }

  def getInteractURL: ActionBuilder =
    http("GET redirect to interact url")
      .get("${interactUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("identitySignInUrl")
      )

  def getIdentitySignInURL: ActionBuilder =
    http("GET Identity Sing-in url")
      .get("${identitySignInUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("olfgJourneyIDUrl")
      )

  def getOlfgJourneyIdURL: ActionBuilder =
    http("GET OLFG Journey ID url")
      .get("${olfgJourneyIDUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("authorizeResponseUrl")
      )

  def getAuthorizeResponseURL: ActionBuilder =
    http("GET Authorize Response url")
      .get("${authorizeResponseUrl}")
      .check(
        status.is(200),
        saveNonce,
        saveState,
        saveFormPostUrl,
        saveSimplifiedJourneyUrl
      )

  def postOneLoginStubAuthnPage(success: Boolean): HttpRequestBuilder =
    if (runLocal) {
      http("POST authorize url/one login stub for AUTHN journey")
        .post(s"$oneLoginStubBaseUrl/one-login-stub/authorize")
        .formParam("state", "${state}")
        .formParam("nonce", "${nonce}")
        .formParam("vtr", "[\"Cl.Cm\"]")
        .formParam("userInfo.success", s"$success")
        .formParam("userInfo.sub", "${randomIdentityProviderId}")
        .formParam("userInfo.email", "${email}")
        .formParam("submit", "submit")
        .check(status.is(303))
        .check(header("Location").saveAs("continueUrl"))
    } else {
      http("POST authorize url/one login stub for AUTHN journey")
        .post("https://www.staging.tax.service.gov.uk" + s"/one-login-stub/authorize")
        .formParam("state", "${state}")
        .formParam("nonce", "${nonce}")
        .formParam("vtr", "[\"Cl.Cm\"]")
        .formParam("userInfo.success", s"$success")
        .formParam("userInfo.sub", "${randomIdentityProviderId}")
        .formParam("userInfo.email", "${email}")
        .formParam("submit", "submit")
        .check(status.is(303))
        .check(header("Location").saveAs("continueUrl"))
    }

  def getAuthOneLogInContinueURL: ActionBuilder =
    http("GET IV authorize continue url")
      .get("${continueUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("authAuthorizeCompleteUrl")
      )

  def getAuthAuthorizeCompleteURL: ActionBuilder =
    http("GET Auth Authorize Complete url")
      .get("${authAuthorizeCompleteUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("authInteractUrl")
      )

  def getAuthInteractURL: ActionBuilder =
    http("GET Auth Interact url")
      .get("${authInteractUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("IvGuidanceHashUrl")
      )

  def getIvGuidanceHashURL: ActionBuilder =
    http("GET IV Guidance Hash url")
      .get("${IvGuidanceHashUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("guidancePageIVUrl")
      )

  def getGuidancePageIV: ActionBuilder =
    if (runLocal) {
      http("GET the Guidance page")
        .get(s"$cmUrl/$${guidancePageIVUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("guidancePageIvInteractUrl")
        )
    } else {
      http("GET the Guidance page")
        .get("https://www.staging.tax.service.gov.uk" + s"/$${guidancePageIVUrl}")
        .check(
          status.is(303),
          header("Location").saveAs("guidancePageIvInteractUrl")
        )
    }

  def getGuidancePageIvInteractURL: ActionBuilder =
    http("GET Guidance PAge IV Interact url")
      .get("${guidancePageIvInteractUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("identityAuthorizeVerificationUrl")
      )

  def getIdentityAuthorizeVerificationURL: ActionBuilder =
    http("GET Identity Authorize Verification url")
      .get("${identityAuthorizeVerificationUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("olfgUrl")
      )

  def getOlfgURL: ActionBuilder =
    http("GET OLFG url")
      .get("${olfgUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("olfgAthorizeResponseUrl")
      )

  def getOlfgAuthorizeResponseURL: ActionBuilder =
    http("GET Authorize Response url")
      .get("${olfgAthorizeResponseUrl}")
      .check(
        status.is(200),
        saveNonce,
        saveState,
        saveFormPostUrl,
        saveSimplifiedJourneyUrl
      )

  def postOneLoginStubIvPage(success: Boolean): HttpRequestBuilder =
    if (runLocal) {
      http("POST authorize url/one login stub for IV journey")
        .post(s"$oneLoginStubBaseUrl/one-login-stub/authorizeIv")
        .formParam("state", "${state}")
        .formParam("nonce", "${nonce}")
        .formParam("vtr", "[\"Cl.Cm.P2\"]")
        .formParam("userInfo.success", s"$success")
        .formParam("userInfo.sub", "${randomIdentityProviderId}")
        .formParam("userInfo.email", "${email}")
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
        .check(status.is(303))
        .check(header("Location").saveAs("oneLogInContinueUrl"))
    } else {
      http("POST authorize url/one login stub for IV journey")
        .post("https://www.staging.tax.service.gov.uk" + s"/one-login-stub/authorizeIv")
        .formParam("state", "${state}")
        .formParam("nonce", "${nonce}")
        .formParam("vtr", "[\"Cl.Cm.P2\"]")
        .formParam("userInfo.success", s"$success")
        .formParam("userInfo.sub", "${randomIdentityProviderId}")
        .formParam("userInfo.email", "${email}")
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
        .check(status.is(303))
        .check(header("Location").saveAs("oneLogInContinueUrl"))

    }

  def getOneLogInContinueURL: ActionBuilder =
    http("GET IV authorize continue url")
      .get("${oneLogInContinueUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("ivAuthorizeCompleteUrl")
      )

  def getIvAuthorizeCompleteURL: ActionBuilder =
    http("GET IV Authorize Complete url")
      .get("${ivAuthorizeCompleteUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("ivInteractUrl")
      )

  def getIvInteractURL: ActionBuilder =
    http("GET IV Interact url")
      .get("${ivInteractUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("guidanceHashUrl")
      )

  def getGuidanceHashURL: ActionBuilder =
    http("GET Guidance Hash url")
      .get("${guidanceHashUrl}")
      .check(
        status.is(303),
        header("Location").saveAs("guidancePageUrl")
      )

  def getGuidancePage: ActionBuilder =
    if (runLocal) {
      http("GET the Guidance page")
        .get(s"$cmUrl/$${guidancePageUrl}")
        .check(
          status.is(200)
        )
    } else {
      http("GET the Guidance page")
        .get("https://www.staging.tax.service.gov.uk" + s"/$${guidancePageUrl}")
        .check(
          status.is(200)
        )
    }

  // def getManageDetailsPageURL: ActionBuilder =
  //  http("GET Manage Details page")
  //    .get(s"$cmUrl/credential-management/manage-details")
  //    .check(
  //      status.is(200)
  //    )

  // def getGuidancePageURL1: ActionBuilder =
  //  http("GET the Guidance page")
  //    .get(s"$cmUrl/credential-management/guidance")
  //    .check(
  //      status.is(200)
  //    )

  def getRopcRegisterContinueUrl: ActionBuilder =
    if (runLocal) {
      http("GET ropc-register Continue URL")
        .get(s"$ropcRegisterContinueUrlLocal")
        .check(
          status.is(200)
        )
    } else {
      http("GET ropc-register Continue URL")
        .get(s"$ropRegisterContinueUrlStaging")
        .check(
          status.is(200)
        )
    }

  def postRopcRegisterUrl: ActionBuilder =
    if (runLocal) {
      http("POST ropc-register Url")
        .post(s"$oneLoginStubUrl/ropc-register")
        .formParam("redirectUrl", s"$cmUrl/credential-management/ropc-register-complete")
        .formParam("scpCredId", "${randomScpCredId}")
        .formParam("groupId", "${contextId}")
        .formParam("email", "${email}")
        .formParam("submit", "submit")
        .check(
          status.is(303),
          header("Location").saveAs("saveRopcCompleteUrl")
        )
    } else {
      http("POST ropc-register Url")
        .post("https://www.staging.tax.service.gov.uk" + s"/one-login-stub/ropc-register")
        .formParam(
          "redirectUrl",
          "https://www.staging.tax.service.gov.uk" + s"/credential-management/ropc-register-complete"
        )
        .formParam("scpCredId", "${randomScpCredId}")
        .formParam("groupId", "${contextId}")
        .formParam("email", "${email}")
        .formParam("submit", "submit")
        .check(
          status.is(303),
          header("Location").saveAs("saveRopcCompleteUrl")
        )
    }

  def getRopcRegisterCompleteUrl: ActionBuilder =
    if (runLocal) {
      http("GET ropc-register Complete URL")
        .get(s"$${saveRopcCompleteUrl}")
        .check(
          status.is(303)
        )
    } else {
      http("GET ropc-register Complete URL")
        .get(s"$${saveRopcCompleteUrl}")
        .check(
          status.is(303)
        )
    }

  def getCmGuidancePageUrl: ActionBuilder =
    if (runLocal) {
      http("GET the Guidance page")
        .get(s"$cmUrl/credential-management/guidance")
        .check(
          status.is(200)
        )
    } else {
      http("GET the Guidance page")
        .get("https://www.staging.tax.service.gov.uk" + s"/credential-management/guidance")
        .check(
          status.is(200)
        )
    }

// Data deletion requests
  def postAcfDelete: ActionBuilder = http("POST Delete ACF data")
    .post(s"$ctxUrl/identity-provider-account-context/test-only/delete-account-context/$${randomNino}")
    .check(
      status.is(200)
    )

  def deleteBasStubAcc(): ActionBuilder = http("DELETE bas-stub Account data")
    .delete(s"$basStubUrl/bas-stubs/account/$${randomScpCredId}")
    .check(
      status.is(204)
    )

}
