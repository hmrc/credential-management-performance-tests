// Copyright 2025 HM Revenue & Customs


package uk.gov.hmrc.perftests.credentialManagement

import org.slf4j.{Logger, LoggerFactory}
import sttp.client3._
import sttp.model.Uri
import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.credentialManagement.CmParts
import uk.gov.hmrc.perftests.credentialManagement.common.AppConfig._


class BasicSimulation extends PerformanceTestRunner {

  private val logger: Logger = LoggerFactory.getLogger(classOf[BasicSimulation])

  private val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

  after {

    // cannot use Gatling DSL in before or after blocks
    // must be plain Scala, so we use STTP to make the final HTTP delete call

    Uri.parse(s"$ctxUrl/identity-provider-account-context/test-only/delete-account-context/AA000003D") match {
      case Left(error) => logger.error(s"Bad URL: $error")
      case Right(uri) =>
        val response = basicRequest.post(uri).send(backend)
        if (response.isSuccess) {
          logger.info("successfully deleted performance test data")
        } else {
          logger.error("unable to delete test data, delete endpoint returned: " + response.code)
        }
    }
  }

  setup("cm-ropc-journey", "CredentialManagement and ROPC register").withActions (CmParts.cmRopcRegisterJourney(): _*)

  runSimulation()

}