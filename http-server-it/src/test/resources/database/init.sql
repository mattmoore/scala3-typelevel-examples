CREATE TABLE IF NOT EXISTS addresses(
  id SERIAL,
  street VARCHAR,
  city VARCHAR,
  state VARCHAR,
  lat NUMERIC(50, 20),
  lon NUMERIC(50, 20)
);

INSERT INTO addresses(id, street, city, state, lat, lon) VALUES
(1, '20 W 34th St.', 'New York', 'NY', 40.748643670602384, -73.98570731665924),
(2, '5908 US-31', 'Williamsburg', 'MI', 44.78319626355651, -85.50011694235425)
;

-- Sadly we can't use COPY command with testcontainers
--COPY public.addresses (id, street, city, state, lat, lon) FROM stdin;
--1	20 W 34th St.	New York	NY	40.74864367060238400000	-73.98570731665924000000
--2	5908 US-31	Williamsburg	MI	44.78319626355651	-85.50011694235425
--\.
