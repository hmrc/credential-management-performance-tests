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

package uk.gov.hmrc.perftests.credentialManagement

import io.gatling.core.action.builder.ActionBuilder
import uk.gov.hmrc.perftests.credentialManagement.requests.GNAPAuthRequests._
import uk.gov.hmrc.perftests.credentialManagement.requests.{BaseRequests, CmRequests}

object CmParts extends BaseRequests with CmRequests {
  import io.gatling.http.request.builder.HttpRequestBuilder._

  def cmRopcRegisterJourney(): Seq[ActionBuilder] = {
      // first create a (new) account with a random subject id
      postOneLoginAccountCreate :+
        // now log this user in (does this create the unverified context??)
        flushAllCookies :+
        toActionBuilder(navigateToCentralAuth) :+
        // Private Beta Pages Start
        toActionBuilder(redirectToConfirmYourGovUkOneLoginEmailAddressPage) :+
        toActionBuilder(postToConfirmYourGovUkOneLoginEmailAddressPage) :+
        toActionBuilder(getToConfirmYourGovUkOneLoginEmailAddressPage) :+
        toActionBuilder(postToConfirmYourGovUkOneLoginEmailAddressPageLocation) :+
        toActionBuilder(getStartOlfgJourney) :+
        // Private Beta Pages End
        toActionBuilder(redirectToSignInMethodPage) :+
        //toActionBuilder(postOneLoginSignInMethodPage) :+
        //toActionBuilder(getOneLoginGatewayStartEndpoint) :+
        //toActionBuilder(redirectToOneLoginGatewayStubPage) :+
        toActionBuilder(postOneLoginGatewayStubPage) :+
        toActionBuilder(redirectToOneLoginGatewayContinueEndpoint) :+
        toActionBuilder(navigateToCompleteFixerJourney) :+
        toActionBuilder(redirectToLocationEndpoint) :+
        toActionBuilder(redirectToCentralAuth) :+
        toActionBuilder(getCentralAuthCl200) :+
        toActionBuilder(getIdentityAuthorizeVerificationRedirect) :+
        toActionBuilder(getIvStartUrl) :+
        toActionBuilder(getIvAuthorizePage) :+
        toActionBuilder(postIvAuthorizePage) :+
        toActionBuilder(getIvContinueUrl) :+
        toActionBuilder(getIvAuthorizeCompleteUrl) :+
        toActionBuilder(redirectToLocationEndpoint) :+
        toActionBuilder(redirectToCentralAuth) :+
        toActionBuilder(getCentralAuthCl200) :+
        // now head to the ACF (IV) journey
        getAccountStartUrl :+
        getAccountLinkRecordsUrl :+
        getTestOnlyNinoPage :+
        postTestOnlyNinoPageForPostcode :+
        getNinoWarmerPage :+
        postNinoWarmerPage :+
        getEnterPostcodePage :+
        postEnterPostcodePage :+
        getIncorrectDetailsPage :+
        getAccountLinkRecordsUrl :+
        getTestOnlyNinoPage :+
        postTestOnlyNinoPage :+
        getEnterNinoPage :+
        postEnterNinoPage :+
        getNinoCheckPage :+
        postNinoCheckPage :+
        getOneLoginSetup :+
        postOneLoginSetup :+
        toActionBuilder(redirectToLocationEndpoint) :+
        toActionBuilder(redirectToCentralAuth) :+
        getFinalCentralAuthCl200 :+
        postEnrolmentStoreStubData :+
        getManageDetailsPage :+
        getGuidancePage :+
        getRopcRegisterContinueUrl :+
        postRopcRegisterContinueUrl :+
        getRopcRegisterCompleteUrl :+
        getRopcCredentialCreated :+
        getGuidancePage :+
        // data removal
        postAcfDelete :+
        deleteBasStubAcc()
  }
}

