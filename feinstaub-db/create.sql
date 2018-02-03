CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE sensormeasurementvalues (
    id bigint NOT NULL,
    date timestamp without time zone,
    sensorid character varying(255),
    type character varying(255) not null,
    value numeric(19,2) not null
);

ALTER TABLE ONLY sensormeasurement
    ADD CONSTRAINT sensormeasurement_pkey PRIMARY KEY (id);

GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurement TO proxyuser;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO proxyuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurementvalues TO feinstaub;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO feinstaub;
