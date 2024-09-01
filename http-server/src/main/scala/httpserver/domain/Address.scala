package httpserver.domain

case class Address(
    id: Int,
    street: String,
    city: String,
    state: String,
    coords: GpsCoords,
)
