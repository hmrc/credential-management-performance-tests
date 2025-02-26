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

object CmParts extends BaseRequests with CmRequests{
  import io.gatling.http.request.builder.HttpRequestBuilder._

  def cmRopcRegisterJourney(): Seq[ActionBuilder] = {
    postOneLoginAccountCreate ++
      postOneLoginAccountUpdate ++
      Seq(
        getAccount,
        toActionBuilder(navigateToOneLoginSignInPageNoToken),
        toActionBuilder(redirectToInteractPage),
        toActionBuilder(redirectToSignInMethodPage),
        toActionBuilder(postOneLoginSignInMethodPage),
        toActionBuilder(getOneLoginGatewayInitialiseEndpoint),
        toActionBuilder(redirectToOneLoginGatewayStubPage),
        toActionBuilder(postOneLoginGatewayStubPage),
        toActionBuilder(redirectToOneLoginGatewayContinueEndpoint),
        toActionBuilder(navigateToCompleteFixerJourney),
        toActionBuilder(redirectToLocationEndpoint),
        toActionBuilder(redirectToOneLoginSignInPage),
        toActionBuilder(getOneLoginSignInPage),
        postAcfInitialise,
        getNinoAccess,
        postContinueNinoAccess,
        getEnterNinoPage,
        postEnterNinoPage,
        getNinoCheckPage,
        postNinoCheckPage,
        getOneLoginSetUpPage,
        postOneLoginSetUpPage,
        postEnrolmentStoreStubData,
        getManageDetailsPageURL,
        getGuidancePageURL,
        getRopcRegisterContinueUrl,
        postRopcRegisterUrl,
        getRopcRegisterCompleteUrl,
        getCmGuidancePageUrl
      )
  }
}