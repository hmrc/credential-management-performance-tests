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

package uk.gov.hmrc.perftests.credentialManagement.common

import uk.gov.hmrc.performance.conf.ServicesConfiguration

object AppConfig extends ServicesConfiguration {

  private val baseUrl: String = baseUrlFor("centralised-authorisation-canary-frontend")
  val caCanaryFeServiceUrl: String = s"$baseUrl/centralised-authorisation-canary/"

  private val authorisationServerFeBaseUrl: String = baseUrlFor("centralised-authorisation-server-frontend")
  val authorisationServerFeUrl: String = authorisationServerFeBaseUrl + "/centralised-authorisation-server"

  private val identityProviderGatewayFrontendBaseUrl: String = baseUrlFor("identity-provider-gateway-frontend")
  val identityProviderGatewayFrontendUrl: String = s"$identityProviderGatewayFrontendBaseUrl"

  private val oneLoginGatewayFeBaseUrl: String = baseUrlFor("one-login-gateway-frontend")
  val oneLoginGatewayFeUrl: String = s"$oneLoginGatewayFeBaseUrl/sign-in-to-hmrc-online-services/one-login"

  private val oneLoginStubBaseUrl: String = baseUrlFor("one-login-stub")
  val oneLoginStubUrl: String = s"$oneLoginStubBaseUrl/one-login-stub"

  val ctxUrl: String = baseUrlFor("identity-provider-account-context")

  val acfFeUrl: String = baseUrlFor("account-context-fixer-frontend")
  val acfBeUrl: String = baseUrlFor("account-context-fixer")

  val camBeUrl: String = baseUrlFor("credential-management")

}

