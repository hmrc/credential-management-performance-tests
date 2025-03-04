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
import uk.gov.hmrc.perftests.credentialManagement.common.RequestFunctions._

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
    http("Update account in IPAC")
      .post(s"$ctxUrl/identity-provider-account-context/accounts")
      .body(StringBody(s"""|
        {
          "action": "update",
          "caUserId": "$${caUserId}",
          "dateOfBirth": "1948-04-23",
          "firstName": "Jim",
          "lastName": "Ferguson",
          "confidenceLevel": 250
        }
        """.stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "performance-tests"))
      .check(status.is(200))
  ).actionBuilders

  def getAccount: ActionBuilder = http("GET newly created account")
    .get(s"$ctxUrl/identity-provider-account-context/accounts?identityProviderId=$${randomIdentityProviderId}")
    .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "centralised-authorisation-server"))
    .check(
      status.is(200)
    )

  def postAcfInitialise: ActionBuilder = http("POST initialise for a verified journey")
    .post(s"$acfBeUrl/account-context-fixer/initialise")
    .body(StringBody("""|
         |{
                        | "action": "VERIFIED_CONTEXT",
                        | "completionUrl":"https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in",
                        | "initialiseParameters":
                        |   {
                        |      "caUserId": "${caUserId}",
                        |      "firstName": "Jim",
                        |      "lastName": "Ferguson",
                        |      "birthdate": "1948-04-23"
                        |   }
                        |}
                        |""".stripMargin))
    .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "centralised-authorisation-server"))
    .check(
      status.is(200),
      bodyString.transform(extractContextJourneyId).saveAs("contextJourneyId")
    )

  def getNinoAccess: ActionBuilder =
    http("GET the start of the NINO access page")
      .get(
        s"$acfFeUrl/sign-in-to-hmrc-online-services/account/test-only/nino-access?contextJourneyId=$${contextJourneyId}"
      )
      .check(
        status.is(200),
        saveCsrfToken,
        saveNino
      )

  def postContinueNinoAccess: ActionBuilder =
    http("POST continue NINO access page")
      .post(
        s"$acfFeUrl/sign-in-to-hmrc-online-services/account/test-only/nino-access?contextJourneyId=$${contextJourneyId}"
      )
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("ninoAccessChoice", "${testOnlyNino}")
      .check(
        status.is(303)
      )

  def getEnterNinoPage: ActionBuilder =
    http("GET enter NINO page")
      .get(s"$acfFeUrl/sign-in-to-hmrc-online-services/account/enter-nino?contextJourneyId=$${contextJourneyId}")
      .check(
        status.is(200)
      )

  def postEnterNinoPage: ActionBuilder =
    http("POST enter NINO page")
      .post(s"$acfFeUrl/sign-in-to-hmrc-online-services/account/enter-nino?contextJourneyId=$${contextJourneyId}")
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("nino", "${testOnlyNino}")
      .formParam("submit", "submit")
      .check(
        status.is(303),
        header("Location").saveAs("saveNinoCheckUrl")
      )

  def getNinoCheckPage: ActionBuilder =
    http("GET NINO check page")
      .get(s"$acfFeUrl/$${saveNinoCheckUrl}")
      .check(
        status.is(200)
      )

  def postNinoCheckPage: ActionBuilder =
    http("POST NINO check page")
      .post(s"$acfFeUrl/$${saveNinoCheckUrl}")
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("answer", "true")
      .formParam("submit", "submit")
      .check(
        status.is(303),
        header("Location").saveAs("saveOneLogInSetupUrl")
      )

  def getOneLoginSetUpPage: ActionBuilder =
    http("GET One log in set up page")
      .get(s"$acfFeUrl/$${saveOneLogInSetupUrl}")
      .check(
        status.is(200)
      )

  def postOneLoginSetUpPage: ActionBuilder =
    http("POST One log in set up page")
      .post(s"$acfFeUrl/$${saveOneLogInSetupUrl}")
      .formParam("""csrfToken""", """${csrfToken}""")
      .formParam("submit", "submit")
      .check(
        status.is(303)
      )

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

  def getManageDetailsPageURL: ActionBuilder =
    http("GET Manage Details page")
      .get(s"$cmUrl/credential-management/manage-details")
      .check(
        status.is(200)
      )

  def getGuidancePageURL: ActionBuilder =
    http("GET the Guidance page")
      .get(s"$cmUrl/credential-management/guidance")
      .check(
        status.is(200)
      )

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
    .post(s"$ctxUrl/identity-provider-account-context/test-only/delete-account-context/$${testOnlyNino}")
    .check(
      status.is(200)
    )

  def deleteBasStubAcc(): ActionBuilder = http("DELETE bas-stub Account data")
    .delete(s"$basStubUrl/bas-stubs/account/$${randomScpCredId}")
    .check(
      status.is(204)
    )

}
