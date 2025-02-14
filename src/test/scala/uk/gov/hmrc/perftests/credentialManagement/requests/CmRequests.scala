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
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ChainBuilder
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._
import uk.gov.hmrc.perftests.credentialManagement.common.RequestFunctions.saveOlfgJourneyId

trait CmRequests extends BaseRequests {
  // NOTE: browser cookies are managed by Gatling, along with state about the current location.
  // But headers must be provided on each call.

  def postOneLoginAccountCreate: List[ActionBuilder] = exec(
    http("Create account in IDP store")
      .post(s"$ctxUrl/identity-provider-account-context/accounts")
      .body(StringBody(
        s"""|
          |{
            |  "action": "create",
            |  "identityProviderId": "$${randomIdentityProviderId}",
            |  "identityProviderType": "ONE_LOGIN",
            |  "email": "$${randomEmail}"
            |}
            |""".stripMargin))
      .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "performance-tests"))
      .check(
        status.is(201),
        jsonPath("$..caUserId").saveAs("caUserId"))
      .check(jsonPath("$..contextId").saveAs("contextId"))
  ).feed(feeder).actionBuilders

  def getAccount: ActionBuilder = http("GET newly created account")
    .get(s"$ctxUrl/identity-provider-account-context/accounts?identityProviderId=$${randomIdentityProviderId}")
    .headers(Map("Content-Type" -> "application/json", "User-Agent" -> "centralised-authorisation-server"))
    .check(
      status.is(200)
    )

  def postAcfInitialise: ActionBuilder = http("POST initialise for a verified journey")
    .post(s"$acfBeUrl/account-context-fixer/initialise")
    .body(StringBody(
      """|
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
      jsonPath("$..startUrl").saveAs("initialiseStart")
    )

  // user must be logged in on order to access this FE page

  def getStartContextJourney: ActionBuilder = {
    if (runLocal) {
      http("GET the start of the context journey")
        .get(s"$${initialiseStart}")
        .check(
          status.is(303),
          header("Location").saveAs("linkRecordRedirect")
        )
    }
    else {
      http("GET the start of the context journey")
        .get("https://www.staging.tax.service.gov.uk" + s"$${initialiseStart}")
        .check(
          status.is(303),
          header("Location").saveAs("ninoWarmerRedirect")
        )
    }
  }

def getLinkRecord: ActionBuilder = http("GET link record for the test only nino access page")
    .get(s"$acfFeUrl$${linkRecordRedirect}")
    .check(
      status.is(303),
      header("Location").saveAs("ninoAccessRedirect"),
      currentLocationRegex("(.*)/account/link-records(.*)")
    )

  def getTestOnlyNinoAccessPage: ActionBuilder = http("GET test only nino access page")
    .get(s"$acfFeUrl$${ninoAccessRedirect}")
    .check(saveCsrfToken)
    .check(
      status.is(200),
      currentLocationRegex("(.*)/account/test-only/nino-access?(.*)")
    )

  def postTestOnlyNinoAccessPage: ActionBuilder = http("POST test only nino access page")
    .post(s"$acfFeUrl$${ninoAccessRedirect}")
    .formParam("""csrfToken""", """${csrfToken}""")
    .formParam("""ninoAccessChoice""", "custom")
    .check(saveOlfgJourneyId)
    .check(
      status.is(303),
      header("Location").saveAs("linkRecordRedirectforNinoWarmerPage"),
      currentLocationRegex("(.*)/account/test-only/nino-access?(.*)")
    )

  def getNinoWarmerPage: ActionBuilder = http("GET nino warmer page")
    .get(s"$acfFeUrl$${linkRecordRedirectforNinoWarmerPage}")
    .check(
      status.is(200),
      saveCsrfToken,
      saveJourneyId
    )

  def postNinoWarmerPage: ActionBuilder = http("POST nino warmer page")
    .post(s"$acfFeUrl$${journeyId}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("answer", "true")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("enterYourNinoPageRedirect")
    )

  def getEnterNinoPage: ActionBuilder = http("GET enter your nino page")
    .get(s"$acfFeUrl$${enterYourNinoPageRedirect}")
    .check(
      status.is(200),
      saveCsrfToken,
      saveJourneyId,
//      Location header doesn't exist'
//            currentLocationRegex("(.*)sign-in-to-hmrc-online-services/account/enter-nino?contextJourneyId=(.*)")
    )

  def postEnterNinoPage: ActionBuilder = http("POST enter your nino page")
    .post(s"$acfFeUrl$${enterYourNinoPageRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("nino", "AA000003D")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("ninoCheckRedirect")
    )

  def getNinoCheckPage: ActionBuilder = http("GET nino check page")
    .get(s"$acfFeUrl$${ninoCheckRedirect}")
    .check(
      status.is(200)
    )

  def postNinoCheckPage: ActionBuilder = http("POST nino check page")
    .post(s"$acfFeUrl$${ninoCheckRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("answer", "true")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("OLsetupCompleteRedirect")
    )

  def getOneLoginSetup: ActionBuilder = http("GET GOV.UK One Login set up complete")
    .get(s"$acfFeUrl$${OLsetupCompleteRedirect}")
    .check(
      status.is(200),
      currentLocationRegex("(.*)/account/one-login-set-up(.*)")
    )

  def postOneLoginSetup: ActionBuilder = http("GET GOV.UK One Login set up complete")
    .post(s"$acfFeUrl$${OLsetupCompleteRedirect}")
    .formParam("csrfToken", "${csrfToken}")
    .formParam("submit", "submit")
    .check(
      status.is(303),
      header("Location").saveAs("stubPageRedirect")
    )

  def getCompletionUrl: ActionBuilder = http("GET completion url/auth login stub page")
    .get(s"$${stubPageRedirect}")
    .check(
      status.is(200),
      currentLocationRegex("(.*)/auth-login-stub/gg-sign-in")
    )

//  def postEnrolmentStoreStubData: ActionBuilder = exec(
//  http("POST enrolment store stub data")
//    .post("http://localhost:9595/enrolment-store-stub/data")
//    .header("Content-Type", "application/json")
//    .body(StringBody(
//      """{
//        |  "groupId": "${contextId}",
//        |  "affinityGroup": "Individual",
//        |  "users": [
//        |    {
//        |      "credId": "${caUserId}",
//        |      "name": "Default User",
//        |      "email": "66666666email@email.com",
//        |      "credentialRole": "Admin",
//        |      "description": "User Description"
//        |    }
//        |  ],
//        |  "enrolments": [
//        |    {
//        |      "serviceName": "IR-SA",
//        |      "identifiers": [
//        |        {
//        |          "key": "UTR",
//        |          "value": "123456"
//        |        }
//        |      ],
//        |      "enrolmentFriendlyName": "IR SA Enrolment",
//        |      "assignedUserCreds": [
//        |        "${caUserId}"
//        |      ],
//        |      "state": "Activated",
//        |      "enrolmentType": "principal",
//        |      "assignedToAll": false
//        |    },
//        |    {
//        |      "serviceName": "IR-SA",
//        |      "identifiers": [
//        |        {
//        |          "key": "UTR",
//        |          "value": "1234567891"
//        |        }
//        |      ],
//        |      "assignedUserCreds": [],
//        |      "state": "Activated",
//        |      "enrolmentType": "principal",
//        |      "assignedToAll": false
//        |    }
//        |  ]
//        |}""".stripMargin))
//    .check(status.is(200))
//)
}
