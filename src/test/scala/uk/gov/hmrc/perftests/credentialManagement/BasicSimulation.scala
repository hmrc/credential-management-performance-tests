// Copyright 2025 HM Revenue & Customs

package uk.gov.hmrc.perftests.credentialManagement

import org.slf4j.{Logger, LoggerFactory}
import sttp.client3._
import uk.gov.hmrc.performance.simulation.PerformanceTestRunner

class BasicSimulation extends PerformanceTestRunner {

  private val logger: Logger = LoggerFactory.getLogger(classOf[BasicSimulation])

  private val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

  setup("cm-ropc-journey", "CredentialManagement and ROPC register").withActions(CmParts.cmRopcRegisterJourney(): _*)

  runSimulation()

}
