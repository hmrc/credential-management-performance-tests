
// Copyright 2025 HM Revenue & Customs

package uk.gov.hmrc.perftests.credentialManagement

import io.gatling.core.action.builder.ActionBuilder
import uk.gov.hmrc.perftests.credentialManagement.requests.{BaseRequests, CmRequests}

object CmParts extends BaseRequests with CmRequests {
  import io.gatling.http.request.builder.HttpRequestBuilder._

  def cmRopcRegisterJourney(): Seq[ActionBuilder] =
    postOneLoginAccountCreate ++
      postOneLoginAccountUpdate ++
      Seq(
        postEnrolmentStoreStubData,
        getGuidancePageURL,
        getInteractURL,
        getIdentitySignInURL,
        getOlfgJourneyIdURL,
        getAuthorizeResponseURL,
        postOneLoginStubAuthnPage(true),
        getAuthOneLogInContinueURL,
        getAuthAuthorizeCompleteURL,
        getAuthInteractURL,
        getIvGuidanceHashURL,
        getGuidancePageIV,
        getGuidancePageIvInteractURL,
        getIdentityAuthorizeVerificationURL,
        getOlfgURL,
        getOlfgAuthorizeResponseURL,
        postOneLoginStubIvPage(true),
        getOneLogInContinueURL,
        getIvAuthorizeCompleteURL,
        getIvInteractURL,
        getGuidanceHashURL,
        getGuidancePage,
        getRopcRegisterContinueUrl,
        postRopcRegisterUrl,
        getRopcRegisterCompleteUrl,
        getCmGuidancePageUrl,
        postAcfDelete,
        deleteBasStubAcc()
      )
}
