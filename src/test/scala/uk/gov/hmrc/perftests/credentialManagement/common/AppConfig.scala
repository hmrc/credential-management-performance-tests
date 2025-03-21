
// Copyright 2025 HM Revenue & Customs


package uk.gov.hmrc.perftests.credentialManagement.common

import uk.gov.hmrc.performance.conf.ServicesConfiguration

object AppConfig extends ServicesConfiguration {

  private val baseUrl: String = baseUrlFor("centralised-authorisation-canary-frontend")
  val caCanaryFeServiceUrl: String = s"$baseUrl/centralised-authorisation-canary/"

  private val authorisationServerFeBaseUrl: String = baseUrlFor("centralised-authorisation-server-frontend")
  val authorisationServerFeUrl: String = authorisationServerFeBaseUrl + "/centralised-authorisation-server"

  private val identityProviderGatewayFrontendBaseUrl: String = baseUrlFor("identity-provider-gateway-frontend")
  val identityProviderGatewayFrontendUrl: String = s"$identityProviderGatewayFrontendBaseUrl"

  val olgUrl: String = baseUrlFor("one-login-gateway")
  private val oneLoginGatewayFeBaseUrl: String = baseUrlFor("one-login-gateway-frontend")
  val oneLoginGatewayFeUrl: String = s"$oneLoginGatewayFeBaseUrl/sign-in-to-hmrc-online-services/one-login"

  val oneLoginStubBaseUrl: String = baseUrlFor("one-login-stub")
  val oneLoginStubUrl: String = s"$oneLoginStubBaseUrl/one-login-stub"
  val ropcRegisterContinueUrlLocal: String =s"$oneLoginStubUrl/ropc-register?continueUrl=http%3A%2F%2Flocalhost%3A12010%2Fcredential-management%2Fropc-register-complete&origin=credential-management-frontend&accountType=individual"
  val ropRegisterContinueUrlStaging: String =s"$oneLoginStubUrl/ropc-register?continueUrl=https%3A%2F%2Fstaging.tax.service.gov.uk%2Fcredential-management%2Fropc-register-complete&origin=credential-management-frontend&accountType=individual"

  val ctxUrl: String = baseUrlFor("identity-provider-account-context")
  val acfFeUrl: String = baseUrlFor("account-context-fixer-frontend")
  val acfBeUrl: String = baseUrlFor("account-context-fixer")
  val esStubDataUrl: String = baseUrlFor("enrolment-store-stub")
  val basStubUrl: String = baseUrlFor("bas-stubs")
  val cmUrl: String = baseUrlFor("credential-management-frontend")
  val ropcRegisterCompleteUrl: String =s"$cmUrl/credential-management/ropc-register-complete"
}

