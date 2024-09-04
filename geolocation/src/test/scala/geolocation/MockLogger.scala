package geolocation

import cats.effect.*
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel

object MockLogger {
  case class LogMessage(level: LogLevel, message: String)

  def apply[F[_]](state: AtomicCell[F, List[LogMessage]]): Logger[F] =
    new Logger[F] {
      override def error(t: Throwable)(message: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Error, message) +: messages)

      override def warn(t: Throwable)(message: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Warn, message) +: messages)

      override def info(t: Throwable)(message: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Info, message) +: messages)

      override def debug(t: Throwable)(message: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Debug, message) +: messages)

      override def trace(t: Throwable)(message: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Trace, message) +: messages)

      override def error(message: => String): F[Unit] =
        error(Throwable(message))(message)

      override def warn(message: => String): F[Unit] =
        warn(Throwable(message))(message)

      override def info(message: => String): F[Unit] =
        info(Throwable(message))(message)

      override def debug(message: => String): F[Unit] =
        debug(Throwable(message))(message)

      override def trace(message: => String): F[Unit] =
        trace(Throwable(message))(message)
    }
}
