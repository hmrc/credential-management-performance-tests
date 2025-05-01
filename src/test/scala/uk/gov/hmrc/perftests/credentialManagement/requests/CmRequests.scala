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
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._

trait CmRequests extends BaseRequests {

  def postOneLoginAccountCreate: List[ActionBuilder] = exec(
    http("Create account in IDP store")
      .post(s"$ctxUrl/identity-provider-account-context/accounts/create")
      .body(StringBody(
        s"""|
         |{
            |  "identityProviderId": "$${randomIdentityProviderId}",
            |  "identityProviderType": "ONE_LOGIN",
            |  "email": "$${randomEmail}"
            |}
            |""".stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "performance-tests"))
      .check(
        status.is(201),
        jsonPath("$..caUserId").saveAs("caUserId"),
        jsonPath("$..contextId").saveAs("contextId"),
        jsonPath("$..eacdUserId").saveAs("eacdUserId") ,
        jsonPath("$..email").saveAs("email")
      )
  ).feed(feeder).actionBuilders


  def getAccountStartUrl: ActionBuilder =
    if (runLocal) {
      http("GET account start url")
        .get("${acfStartUrl}")
        .check(
          status.is(303),
          currentLocationRegex(s"$acfFeUrl/sign-in-to-hmrc-online-services/account/start?(.*)"),
          header("Location").saveAs("linkRecordsUrl")
        )
    } else {
      http("GET account start url")
        .get(s"$acfFeUrl$${acfStartUrl}")
        .check(
          status.is(303),
          currentLocationRegex(s"$acfFeUrl/sign-in-to-hmrc-online-services/account/start?(.*)"),
          header("Location").saveAs("linkRecordsUrl")
        )
    }

  def getAccountLinkRecordsUrl: ActionBuilder = http("GET account link records url")
    .get(s"$acfFeUrl$${linkRecordsUrl}")
    .check(
      status.is(303),
      header("Location").saveAs("testOnlyNinoUrl")
    )

  def getTestOnlyNinoPage: ActionBuilder = http("GET Test only nino access page")
    .get(s"$acfFeUrl$${testOnlyNinoUrl}")
    .check(
      status.is(200),
      saveCsrfToken,
      saveNino
    )

  def postTestOnlyNinoPage: ActionBuilder = http("POST Test only nino access page")
    .post(s"$acfFeUrl$${testOnlyNinoUrl}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("ninoAccessChoice", "${testOnlyNino}")
    .check(
      status.is(303),
      header("Location").saveAs("enterNinoPage")
    )

  def postTestOnlyNinoPageForPostcode: ActionBuilder = http("POST Test only nino access page for postcode")
    .post(s"$acfFeUrl$${testOnlyNinoUrl}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("ninoAccessChoice", "custom")
    .check(
      status.is(303),
      header("Location").saveAs("testOnlyNinoCustomRedirect")
    )

  def getNinoWarmerPage: ActionBuilder = http("GET Nino Warmer Page")
    .get(s"$acfFeUrl$${testOnlyNinoCustomRedirect}")
    .check(
      status.is(200),
      saveCsrfToken,
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/link-records?(.*)")
    )

  def postNinoWarmerPage: ActionBuilder = http("POST nino warmer page")
    .post(s"$acfFeUrl$${testOnlyNinoCustomRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("answer", "false")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("enterPostcodeRedirect")
    )

  def getEnterPostcodePage: ActionBuilder = http("GET enter your postcode page")
    .get(s"$acfFeUrl$${enterPostcodeRedirect}")
    .check(
      status.is(200),
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/enter-postcode(.*)")
    )

  def postEnterPostcodePage: ActionBuilder = http("POST enter your postcode page")
    .post(s"$acfFeUrl$${enterPostcodeRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("postcode", "SW1A 2AA")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("incorrectDetailsRedirect")
    )

  def getIncorrectDetailsPage: ActionBuilder = http("GET incorrect details page and goes to deskpro failure")
    .get(s"$acfFeUrl$${incorrectDetailsRedirect}")
    .check(
      status.is(200),
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/unable-to-link-gov-uk-one-login(.*)")
    )

  def getEnterNinoPage: ActionBuilder = http("GET enter your nino page")
    .get(s"$acfFeUrl$${enterNinoPage}")
    .check(
      status.is(200),
      saveCsrfToken,
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/enter-nino(.*)")
    )

  def postEnterNinoPage: ActionBuilder = http("POST enter your nino page")
    .post(s"$acfFeUrl$${enterNinoPage}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("nino", "${testOnlyNino}")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("ninoCheckRedirect")
    )

  def getNinoCheckPage: ActionBuilder = http("GET nino check page")
    .get(s"$acfFeUrl$${ninoCheckRedirect}")
    .check(
      status.is(200),
      saveCsrfToken,
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/nino-check(.*)")
    )

  def postNinoCheckPage: ActionBuilder = http("POST nino check page")
    .post(s"$acfFeUrl$${ninoCheckRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("answer", "true")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("oneLoginSetupCompleteRedirect")
    )

  def getOneLoginSetup: ActionBuilder = http("GET GOV.UK One Login set up complete")
    .get(s"$acfFeUrl$${oneLoginSetupCompleteRedirect}")
    .check(
      status.is(200),
      saveCsrfToken,
      currentLocationRegex("(.*)/sign-in-to-hmrc-online-services/account/one-login-set-up(.*)")
    )

  def postOneLoginSetup: ActionBuilder = http("POST GOV.UK One Login set up complete")
    .post(s"$acfFeUrl$${oneLoginSetupCompleteRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("interactLocationUrl")
    )

  def postEnrolmentStoreStubData: ActionBuilder =
    http("POST Enrolment store stub data")
      .post(s"$esStubDataUrl/enrolment-store-stub/data")
      .body(StringBody(
        """{
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
          |    }
          |  ]
          |}""".stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "centralised-authorisation-server"))
      .check(
        status.is(204)
      )
  def getFinalCentralAuthCl200: ActionBuilder =
    http("Get Central Auth CL200")
      .get(s"$caCanaryFeServiceUrl/centralised-authorisation-canary/CL_200")
      .check(currentLocationRegex("(.*)/centralised-authorisation-canary/CL_200"))
      .check(
        status.is(200)
      )
  def getManageDetailsPage: ActionBuilder =
    http("GET Manage Details page")
      .get(s"$cmUrl/credential-management/manage-details")
      .check(
        status.is(200),
        currentLocationRegex("(.*)/credential-management/manage-details")
      )

  def getGuidancePage: ActionBuilder =
    http("GET Guidance page")
      .get(s"$cmUrl/credential-management/guidance")
      .check(
        status.is(200),
        currentLocationRegex("(.*)/credential-management/guidance")
      )

  def getRopcRegisterContinueUrl: ActionBuilder =
    if (runLocal) {
      http("GET ropc-register stub")
        .get(s"$ropcRegisterContinueUrlLocal")
        .check(
          status.is(200)
        )
    } else {
      http("GET ropc-register stub")
        .get(s"$ropRegisterContinueUrlStaging")
        .check(
          status.is(200)
        )
    }

  def postRopcRegisterContinueUrl: ActionBuilder =
    if (runLocal) {
      http("POST ropc-register stub")
        .post(s"$oneLoginStubUrl/ropc-register")
        .formParam("redirectUrl", s"$cmUrl/credential-management/ropc-register-complete")
        .formParam("scpCredId", "${randomScpCredId}")
        .formParam("groupId", "${contextId}")
        .formParam("email", "${email}")
        .formParam("submit", "submit")
        .check(
          status.is(303),
          header("Location").saveAs("ropcRegisterComplete")
        )
    } else {
      http("POST ropc register stub")
        .post("https://www.staging.tax.service.gov.uk/one-login-stub/ropc-register")
        .formParam("redirectUrl", "/credential-management/ropc-register-complete")
        .formParam("scpCredId", "${randomScpCredId}")
        .formParam("groupId", "${contextId}")
        .formParam("email", "${email}")
        .formParam("submit", "submit")
        .check(
          status.is(303),
          header("Location").saveAs("ropcRegisterComplete")
        )
    }

  def getRopcRegisterCompleteUrl: ActionBuilder =
    if (runLocal) {
      http("GET ropc register complete")
        .get("${ropcRegisterComplete}")
        .check(
          status.is(303),
          header("Location").saveAs("ropCredentialCreated")
        )
    } else {
      http("GET ropc register complete")
        .get(s"$cmUrl$${ropcRegisterComplete}")
        .check(
          status.is(303),
          header("Location").saveAs("ropCredentialCreated")
        )
    }

  def getRopcCredentialCreated: ActionBuilder =
    http("GET ropc successfully linked GG cred page")
      .get(s"$cmUrl$${ropCredentialCreated}")
      .check(
        status.is(200),
        currentLocationRegex("(.*)/credential-management/govgateway-id-created")
      )
}
