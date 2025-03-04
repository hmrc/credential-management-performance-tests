// Copyright 2025 HM Revenue & Customs

package uk.gov.hmrc.perftests.credentialManagement

import io.gatling.core.action.builder.ActionBuilder
import uk.gov.hmrc.perftests.credentialManagement.requests.GNAPAuthRequests._
import uk.gov.hmrc.perftests.credentialManagement.requests.{BaseRequests, CmRequests}

object CmParts extends BaseRequests with CmRequests {
  import io.gatling.http.request.builder.HttpRequestBuilder._

  def cmRopcRegisterJourney(): Seq[ActionBuilder] =
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
        getCmGuidancePageUrl,
        postAcfDelete
      )
}
