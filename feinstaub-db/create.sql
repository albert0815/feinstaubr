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

ALTER TABLE ONLY sensormeasurementvalues
    ADD CONSTRAINT sensormeasurementvalues_pkey PRIMARY KEY (id);

GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurementvalues TO proxyuser;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO proxyuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurementvalues TO feinstaub;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO feinstaub;



insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'temperature', temperatur from sensormeasurement where temperatur is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'humidity', humidity from sensormeasurement where humidity is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'SDS_P1', p1 from sensormeasurement where p1 is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'SDS_P2', p2 from sensormeasurement where p2 is not null;

  
insert into biz_term(
  biz_term_id, 
  biz_term_name, 
) 
values(
 nextval('idsequence'),
 'temp'
);
