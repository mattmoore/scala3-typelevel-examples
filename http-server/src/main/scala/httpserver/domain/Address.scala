package httpserver.domain

case class Address(
    street: String,
    city: String,
    state: String,
    coords: GpsCoords,
)
