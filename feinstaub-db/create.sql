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

insert into SensorMeasurementType values ('temperature', 2, 'Â°C', 'wb_sunny', 0.1, 1, 'Temperatur');
insert into SensorMeasurementType values ('humidity', 2, '%', 'grain', 2, 2, 'Luftfeuchtigkeit');
insert into SensorMeasurementType values ('SDS_P1', 2, 'PM10 in Î¼g/mÂ³', 'location_city', 2, 3, 'Feinstaub PM10');
insert into SensorMeasurementType values ('SDS_P2', 2, 'PM10 in Î¼g/mÂ³', 'location_city', 2, 4, 'Feinstaub PM2.5');

select * from sensormeasurementvalues where type not in ('temperature', 'humidity', 'SDS_P1', 'SDS_P2');
delete from sensormeasurementvalues where type not in ('temperature', 'humidity', 'SDS_P1', 'SDS_P2');
alter table sensormeasurementvalues add constraint FKtew64vyo2iwrbxpr5ohy0a4wd foreign key (sensorMeasurementType_type) references SensorMeasurementType;


--version 0.3
create table sensor (sensorId varchar(255), name varchar(255), primary key (sensorId));
insert into sensor values ('7620363', 'AuÃŸensensor');
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
insert into mvgstation values (1170, 'SilberhornstraÃŸe', '.*(Feldmoching|Einkaufszentrum|Olympiazentrum|Sendlinger).*');
insert into mvgstation values (1190, 'Wettersteinplatz', '.*(Einkaufszentrum|Stiglmaierplatz|MaillingerstraÃŸe).*');
insert into mvgstation values (1146, 'Spixstr', '.*(Freiheit).*');
insert into mvgstation values (1115, 'Tegernseer LandstraÃŸe', '.*(Ostbahnhof|Laim|Max).*');
GRANT SELECT ON mvgstation TO proxyuser;    


--version 0.7
create table sensorlocation(id int8 not null, locationName varchar(255), primary key (id));
insert into sensorlocation values (nextval('hibernate_sequence'), 'MÃ¼nchen');
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


-- version 0.8
alter table mvgstation add column footway int4 null;
alter table mvgstation add column latitude float8 not null default;
alter table mvgstation add column longitude float8 not null default;

-- version 0.9
alter table DwdForecast rename to WeatherForecast;
alter table WeatherForecast add column forecastSource varchar(255) not null default 'DWD'; 
alter table WeatherForecast add column precipitation numeric(19, 2) null; 
alter table WeatherForecast add column humidity numeric(19, 2) null; 
alter table WeatherForecast ALTER COLUMN forecastSource DROP DEFAULT ;
alter table SensorLocation add column openWeatherId varchar(255) null;
update SensorLocation set openweatherid = '2867714';

create table FeinstaubrConfiguration (
	id int8 not null,
	category varchar(255) not null,
	key varchar(255) not null,
	value varchar(255) null,
	primary key (id)
);
GRANT SELECT ON FeinstaubrConfiguration TO proxyuser;    


--version 1.0
alter table WeatherForecast drop column weather;
alter table WeatherForecast add column weather varchar(255) null;
alter table SensorLocation add column latitude float8 not null default 0;
alter table SensorLocation add column longitude float8 not null default 0;
update sensorlocation set latitude=48.1093, longitude = 11.5804;
alter table SensorLocation ALTER column latitude drop default;
alter table SensorLocation ALTER column longitude drop default;
CREATE UNIQUE INDEX fdsuiorhziu3o2hfjdsbfk on FeinstaubrConfiguration (category, key);
alter table FeinstaubrConfiguration add column description varchar(255) null;
--as per https://www.dwd.de/DE/leistungen/opendata/help/schluessel_datenformate/poi_present_weather_zuordnung_pdf.pdf?__blob=publicationFile&v=2
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.1', 'CLEAR', 'wolkenlos');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.2', 'CLOUDY', 'heiter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.3', 'CLOUDY', 'bewölkt');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.4', 'COVERED', 'bedeckt');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.5', 'FOG', 'Nebel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.6', 'FOG', 'gefrierender Nebel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.7', 'RAIN', 'leichter Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.8', 'RAIN', 'Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.9', 'RAIN', 'kräftiger Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.10', 'HAIL', 'gefrierender Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.11', 'HAIL', 'kräftiger gefrierender Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.12', 'SLEET', 'Schneeregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.13', 'SLEET', 'kräftiger Schneeregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.14', 'SNOW', 'leichter Schneefall');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.15', 'SNOW', 'Schneefall');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.16', 'SNOW', 'kräftiger Schneefall');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.17', 'HAIL', 'Eiskörner');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.18', 'SHOWER', 'Regenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.19', 'SHOWER', 'kräftiger Regenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.20', 'SLEET', 'Schneeregenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.21', 'SLEET', 'kräftiger Schneeregenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.22', 'SNOW', 'Schneeschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.23', 'SNOW', 'kräftiger Schneeregenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.24', 'SLEET', 'Graupelschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.25', 'SLEET', 'kräftiger Graupelschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.26', 'THUNDERSTORM', 'Gewitter ohne Niederschlag');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.27', 'THUNDERSTORM', 'Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.28', 'THUNDERSTORM', 'kräftiges Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.29', 'THUNDERSTORM', 'Gewitter mit Hagel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.30', 'THUNDERSTORM', 'kräftiges Gewitter mit Hagel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.31', 'STORM', 'Böen');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'dwd', 'weather.61', 'RAIN', 'Regen');


-- as per https://openweathermap.org/weather-conditions
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.200', 'THUNDERSTORM', 'Gewitter mit leichtem Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.201', 'THUNDERSTORM', 'Gewitter mit Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.202', 'THUNDERSTORM', 'Gewitter mit starkem Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.210', 'THUNDERSTORM', 'leichtes Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.211', 'THUNDERSTORM', 'Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.212', 'THUNDERSTORM', 'schweres Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.221', 'THUNDERSTORM', 'sehr schweres Gewitter');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.230', 'THUNDERSTORM', 'Gewitter mit leichtem Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.231', 'THUNDERSTORM', 'Gewitter mit Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.232', 'THUNDERSTORM', 'Gewitter mit starkem Nieselregen');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.300', 'RAIN', 'leichter Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.301', 'RAIN', 'Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.302', 'RAIN', 'starker Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.310', 'RAIN', 'leichter Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.311', 'RAIN', 'Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.312', 'RAIN', 'starker Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.313', 'SHOWER', 'Regenschauer und Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.314', 'SHOWER', 'starke Regenschauer und Nieselregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.321', 'SHOWER', 'Nieselregenschauer');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.500', 'RAIN', 'leichter Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.501', 'RAIN', 'mäßiger Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.502', 'RAIN', 'starker Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.503', 'RAIN', 'sehr schwerer Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.504', 'RAIN', 'extremer Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.511', 'HAIL', 'gefrierender Regen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.520', 'SHOWER', 'leichter Regenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.521', 'SHOWER', 'Regenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.522', 'SHOWER', 'starker Regenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.531', 'SHOWER', 'starker Regenschauer');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.600', 'SNOW', 'leichter Schnee');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.601', 'SNOW', 'Schnee');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.602', 'SNOW', 'starker Schneefall');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.611', 'SLEET', 'Schneeregen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.612', 'SLEET', 'Schneeregenschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.615', 'SLEET', 'leichter Regen und Schnee');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.616', 'SLEET', 'Regen und Schnee');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.620', 'SNOW', 'leichter Schneeschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.621', 'SNOW', 'Schneeschauer');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.622', 'SNOW', 'starker Schneeschauer');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.701', 'FOG', 'Nebel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.741', 'FOG', 'Nebel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.771', 'STORM', 'Böen');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.781', 'STORM', 'Tornado');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.800', 'CLEAR', 'klarer Himmel');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.801', 'CLOUDY', 'ein paar Wolken');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.802', 'CLOUDY', 'aufgelockerte Bewölkung');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.803', 'CLOUDY', 'aufgelockerte Bewölkung');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.804', 'COVERED', 'bewölkt');

insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.900', 'STORM', 'Tornado');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.905', 'STORM', 'windig');
insert into FeinstaubrConfiguration values (nextval('hibernate_sequence'), 'openweather', 'weather.906', 'HAIL', 'Hagel');

-- mapping for the weather icons font as per http://erikflowers.github.io/weather-icons/
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'CLEAR.day', '61453');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'CLEAR.night', '61486');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'CLOUDY.neutral', '61459');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'CLOUDY.day', '61442');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'CLOUDY.night', '61574');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'COVERED.neutral', '61459');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SHOWER.neutral', '61466');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SHOWER.day', '61449');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SHOWER.night', '61481');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SLEET.neutral', '61621');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SLEET.day', '61618');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SLEET.night', '61620');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'RAIN.neutral', '61465');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'RAIN.day', '61448');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'RAIN.night', '61494');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SNOW.neutral', 'f01b');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SNOW.day', '61450');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'SNOW.night', '61482');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'HAIL.neutral', '61461');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'HAIL.day', '61444');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'HAIL.night', '61476');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'THUNDERSTORM.neutral', '61470');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'THUNDERSTORM.day', '61456');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'THUNDERSTORM.night', '61485');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'FOG.neutral', '61460');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'FOG.day', '61443');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'FOG.night', '61514');

insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'STORM.neutral', '61457');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'STORM.day', '61440');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'weathericons', 'STORM.night', '61474');


-- 1.1
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'mvg', 'maxDepartures', '4');
insert into FeinstaubrConfiguration (id, category, key, value) values (nextval('hibernate_sequence'), 'mvg', 'departureInterval', '20');

create table DisplayConfiguration (
	id int8 not null,
	displayType varchar(255) not null,
	sensorLocationId int8 null,
	primary key (id)
);
alter table DisplayConfiguration add constraint felkfjueiew3948dd foreign key (sensorLocationId) references SensorLocation;
GRANT SELECT ON DisplayConfiguration TO proxyuser;    

insert into DisplayConfiguration values (nextval('hibernate_sequence'), '7.5', 149998);

alter table sensorlocation add column externalId varchar(255) not null default '';
CREATE UNIQUE INDEX liiufzztretzfjhgi86 on sensorlocation (externalId);
update sensorlocation set externalId = 'home';
alter table sensorlocation alter column externalId drop default;
