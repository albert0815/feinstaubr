CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE sensormeasurement (
    id bigint NOT NULL,
    date timestamp without time zone,
    humidity numeric(19,2),
    p1 numeric(19,2),
    p2 numeric(19,2),
    sensorid character varying(255),
    softwareversion character varying(255),
    temperatur numeric(19,2)
);

ALTER TABLE ONLY sensormeasurement
    ADD CONSTRAINT sensormeasurement_pkey PRIMARY KEY (id);

GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurement TO proxyuser;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO proxyuser;