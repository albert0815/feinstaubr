--version 0.1 beta
create sequence hibernate_sequence start 1 increment 1;

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
GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurement TO proxyuser;    
GRANT USAGE, SELECT ON SEQUENCE hibernate_sequence TO proxyuser;


--version 0.1.0 branch newdatamodel
create table sensormeasurementvalues (id int8 not null, date timestamp, sensorId varchar(255), value numeric(19, 2), sensorMeasurementType_type varchar(255), primary key (id));
GRANT SELECT, INSERT, UPDATE, DELETE ON sensormeasurementvalues TO proxyuser;    


insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'temperature', temperatur from sensormeasurement where temperatur is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'humidity', humidity from sensormeasurement where humidity is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'SDS_P1', p1 from sensormeasurement where p1 is not null;
insert into sensormeasurementvalues (id, date, sensorid, type, value) select nextval('hibernate_sequence'), date, sensorid, 'SDS_P2', p2 from sensormeasurement where p2 is not null;

--version 0.1.1 branch newdatamodel

create table SensorMeasurementType (type varchar(255) not null, epsilonForSimplify float8 not null, label varchar(255), logo varchar(255), minDiffBetweenTwoValues numeric(19, 2), sortOrder int4 not null, title varchar(255) not null, primary key (type))
GRANT SELECT ON SensorMeasurementType TO proxyuser;    

insert into SensorMeasurementType values ('temperature', 2, '°C', 'wb_sunny', 0.1, 1, 'Temperatur');
insert into SensorMeasurementType values ('humidity', 2, '%', 'grain', 2, 2, 'Luftfeuchtigkeit');
insert into SensorMeasurementType values ('SDS_P1', 2, 'PM10 in μg/m³', 'location_city', 2, 3, 'Feinstaub PM10');
insert into SensorMeasurementType values ('SDS_P2', 2, 'PM10 in μg/m³', 'location_city', 2, 4, 'Feinstaub PM2.5');

select * from sensormeasurementvalues where type not in ('temperature', 'humidity', 'SDS_P1', 'SDS_P2');
delete from sensormeasurementvalues where type not in ('temperature', 'humidity', 'SDS_P1', 'SDS_P2');
alter table sensormeasurementvalues add constraint FKtew64vyo2iwrbxpr5ohy0a4wd foreign key (sensorMeasurementType_type) references SensorMeasurementType;


--version 0.3
create table sensor (sensorId varchar(255), name varchar(255), primary key (sensorId));
insert into sensor values ('7620363', 'Außensensor');
insert into sensor values ('30:ae:a4:22:ca:f4', 'Innensensor');
alter table sensormeasurementvalues add constraint FKlif2ids4jsfhjk3grukjdfsb foreign key (sensorId) references sensor;
GRANT SELECT ON sensor TO proxyuser;    

insert into SensorMeasurementType values ('pressure', 2, ' hPa', 'wb_iridescent', 2, 4, 'Luftdruck');

--version 0.4
alter table sensormeasurementvalues add column calculatedValue numeric(19, 2) null;


--version 0.5
alter table sensormeasurementtype add column codepoint int4 not null default 0;

--version 0.6
create table mvgstation (stationId int8 not null, name varchar(255), destinationfilter varchar(255), primary key (stationId));
insert into mvgstation values (1170, 'Silberhornstraße', '.*(Feldmoching|Einkaufszentrum|Olympiazentrum|Sendlinger).*');
insert into mvgstation values (1190, 'Wettersteinplatz', '.*(Einkaufszentrum|Stiglmaierplatz|Maillingerstraße).*');
insert into mvgstation values (1146, 'Spixstr', '.*(Freiheit).*');
insert into mvgstation values (1115, 'Tegernseer Landstraße', '.*(Ostbahnhof|Laim|Max).*');
GRANT SELECT ON mvgstation TO proxyuser;    


--version 0.7
create table sensorlocation(id int8 not null, locationName varchar(255), primary key (id));
insert into sensorlocation values (nextval('hibernate_sequence'), 'München');
alter table sensor add column location int8 not null default 0;
update sensor set location = currval('hibernate_sequence');
alter table sensor add constraint fdkjdhrLIUHds327797 foreign key (location) references sensorlocation;
alter table sensorlocation add column poiId varchar(255) null;
update sensorlocation set poiId='10865';
create table dwdforecast(
	id int8 not null,
	location int8 not null,
    forecastDate timestamp without time zone not null,
    lastUpdate timestamp without time zone not null,
	temperature numeric(19, 2) null,
	pressure numeric(19, 2) null,
	weather numeric(19, 2) null,
	cloudCoverTotal numeric(19, 2) null,
	chanceOfRain numeric(19, 2) null,
	meanWindDirection numeric(19, 2) null,
	meanWindSpeed numeric(19, 2) null,
	primary key (id)
);
alter table dwdforecast add constraint fdwkuh33uhiufdshfudsgfuz foreign key (location) references sensorlocation;
