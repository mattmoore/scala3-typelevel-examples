import cats.effect.IO
import cats.effect.kernel.Ref
import retry.*
import retry.ResultHandler.retryOnAllErrors
import retry.RetryDetails.NextStep.*
import weaver.*

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.*

object CatsRetrySuite extends SimpleIOSuite {
  test("retry 5 times, successful on 5th retry") {
    val NumRetries = 5

    val logMessages = collection.mutable.ArrayBuffer.empty[String]

    def logError(err: Throwable, details: RetryDetails): IO[Unit] =
      details.nextStepIfUnsuccessful match {
        case DelayAndRetry(nextDelay) =>
          IO(logMessages.append(s"Failed to download. So far we have retried ${details.retriesSoFar} times."))
        case GiveUp =>
          IO(logMessages.append(s"Giving up after ${details.retriesSoFar} retries."))
      }

    def flakyRequest(requestCounter: Ref[IO, Int]): IO[String] = for {
      _     <- requestCounter.update(_ + 1)
      count <- requestCounter.get
      result <-
        if (count < NumRetries) IO.raiseError(new Throwable("Failed"))
        else IO.pure("Hello!")
    } yield result

    val retryPolicy = RetryPolicies.limitRetries[IO](NumRetries)

    def flakyRequestWithRetry(requestCounter: Ref[IO, Int]): IO[String] =
      retryingOnErrors(flakyRequest(requestCounter))(
        policy = retryPolicy,
        errorHandler = retryOnAllErrors(logError),
      )

    for {
      requestCounter <- Ref.of(0)
      result         <- flakyRequestWithRetry(requestCounter).attempt
      requestCount   <- requestCounter.get
    } yield expect.all(
      result == Right("Hello!"),
      requestCount == 5,
    )
  }

  test("retry 5 times, fails all attempts") {
    val NumRetries = 5

    val logMessages = collection.mutable.ArrayBuffer.empty[String]

    def logError(err: Throwable, details: RetryDetails): IO[Unit] =
      details.nextStepIfUnsuccessful match {
        case DelayAndRetry(nextDelay) =>
          IO(logMessages.append(s"Failed to download. So far we have retried ${details.retriesSoFar} times."))
        case GiveUp =>
          IO(logMessages.append(s"Giving up after ${details.retriesSoFar} retries."))
      }

    def flakyRequest(requestCounter: Ref[IO, Int]): IO[String] = for {
      _     <- requestCounter.update(_ + 1)
      count <- requestCounter.get
      result <-
        if (count < NumRetries) IO.raiseError(new Throwable("Failed"))
        else IO.raiseError(new Throwable("Failed"))
    } yield result

    val retryPolicy = RetryPolicies.limitRetries[IO](NumRetries)

    def flakyRequestWithRetry(requestCounter: Ref[IO, Int]): IO[String] =
      retryingOnErrors(flakyRequest(requestCounter))(
        policy = retryPolicy,
        errorHandler = retryOnAllErrors(logError),
      )

    for {
      requestCounter <- Ref.of(0)
      result         <- flakyRequestWithRetry(requestCounter).attempt
      requestCount   <- requestCounter.get
    } yield expect.all(
      result.isLeft,
      logMessages == ArrayBuffer(
        "Failed to download. So far we have retried 0 times.",
        "Failed to download. So far we have retried 1 times.",
        "Failed to download. So far we have retried 2 times.",
        "Failed to download. So far we have retried 3 times.",
        "Failed to download. So far we have retried 4 times.",
        "Giving up after 5 retries.",
      ),
      requestCount == 6,
    )
  }

  test("retry 5 times with exponential backoff") {
    val NumRetries = 5

    val logMessages = collection.mutable.ArrayBuffer.empty[String]

    def logError(err: Throwable, details: RetryDetails): IO[Unit] =
      details.nextStepIfUnsuccessful match {
        case DelayAndRetry(nextDelay) =>
          IO(logMessages.append(s"Failed to download. So far we have retried ${details.retriesSoFar} times."))
        case GiveUp =>
          IO(logMessages.append(s"Giving up after ${details.retriesSoFar} retries."))
      }

    def flakyRequest(requestCounter: Ref[IO, Int]): IO[String] = for {
      _     <- requestCounter.update(_ + 1)
      count <- requestCounter.get
      result <-
        if (count < NumRetries) IO.raiseError(new Throwable("Failed"))
        else IO.raiseError(new Throwable("Failed"))
    } yield result

    val retryPolicy = RetryPolicies.limitRetries[IO](NumRetries) join
      RetryPolicies.exponentialBackoff[IO](10.milliseconds)

    def flakyRequestWithRetry(requestCounter: Ref[IO, Int]): IO[String] =
      retryingOnErrors(flakyRequest(requestCounter))(
        policy = retryPolicy,
        errorHandler = retryOnAllErrors(logError),
      )

    for {
      requestCounter <- Ref.of(0)
      result         <- flakyRequestWithRetry(requestCounter).attempt
      requestCount   <- requestCounter.get
    } yield expect.all(
      result.isLeft,
      logMessages == ArrayBuffer(
        "Failed to download. So far we have retried 0 times.",
        "Failed to download. So far we have retried 1 times.",
        "Failed to download. So far we have retried 2 times.",
        "Failed to download. So far we have retried 3 times.",
        "Failed to download. So far we have retried 4 times.",
        "Giving up after 5 retries.",
      ),
      requestCount == 6,
    )
  }
}
