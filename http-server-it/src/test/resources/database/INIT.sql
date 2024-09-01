CREATE TABLE addresses(
  id SERIAL,
  street VARCHAR,
  city VARCHAR,
  state VARCHAR,
  lat NUMERIC(50, 20),
  lon NUMERIC(50, 20)
);

INSERT INTO addresses(street, city, state, lat, lon) VALUES ('20 W 34th St.', 'New York', 'NY', 40.748643670602384, -73.98570731665924);
