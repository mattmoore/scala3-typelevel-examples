package httpserver.repositories

import cats.effect.kernel.Async
import httpserver.domain.Address

trait AddressRepository[F[_]] {
  def getByAddress(address: Address): F[Option[Address]]
}

object AddressRepository {
  def apply[F[_]: Async](): AddressRepository[F] = new AddressRepository[F] {
    override def getByAddress(address: Address): F[Option[Address]] = ???
  }
}
