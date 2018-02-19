package service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import junit.framework.TestCase;

public class ImportData extends TestCase {
	public void testSave3() throws IOException, ParseException {
		SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		File f = new File("C:\\Users\\papend\\Downloads\\sensordaten");
		Client client = ClientBuilder.newClient();
		
		WebTarget target = client.target("http://localhost:8080/rest/sensor/save");
		SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		for (File csv : f.listFiles()) {
			try (Scanner scanner = new Scanner(csv)) {
				while (scanner.hasNextLine()) {
					String string = scanner.nextLine();
					String[] split = string.split(";");
					if (split[0].equals("sensor_id")) {
						continue;
					}
					JsonArrayBuilder values = Json.createArrayBuilder();
					if (split[1].equals("SDS011")) {
						values.add(Json.createObjectBuilder().add("value_type", "SDS_P1").add("value", split[6]));
						values.add(Json.createObjectBuilder().add("value_type", "SDS_P2").add("value", split[9]));
					} else if (split[1].equals("DHT22")) {
						values.add(Json.createObjectBuilder().add("value_type", "temperature").add("value", split[6]));
						values.add(Json.createObjectBuilder().add("value_type", "humidity").add("value", split[7]));
					}
					
					if (!values.build().isEmpty()) {
						JsonObject o = Json.createObjectBuilder()
								.add("esp8266id", "7620363")
								.add("sensordatavalues", values)
								.add("date", targetDateFormat.format(sourceDateFormat.parse(split[5])))
								.build();
						target.request().post(Entity.json(o));
					}
					

				}
			}
		}
		
		

	}
	public void testSave2() throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/rest/sensor/save");
		JsonObject o = Json.createObjectBuilder()
				.add("esp8266id", "7620363")
				.add("sensordatavalues", 
						Json.createArrayBuilder()
						.add(Json.createObjectBuilder().add("value_type", "humidity").add("value", "50"))
						.add(Json.createObjectBuilder().add("value_type", "temperature").add("value", "10"))
						.add(Json.createObjectBuilder().add("value_type", "SDS_P1").add("value", "5"))
						.add(Json.createObjectBuilder().add("value_type", "SDS_P2").add("value", "1"))
					)
				.add("date", "2018-02-03T10:00:00.000Z")
				.build();
		target.request().post(Entity.json(o));

	}
	public void testCo2() throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/rest/sensor/save");
		JsonObject o = Json.createObjectBuilder()
				.add("esp8266id", "30:ae:a4:22:ca:f4")
				.add("sensordatavalues", 
						Json.createArrayBuilder()
						.add(Json.createObjectBuilder().add("value_type", "co2").add("value", "10"))
					)
				.add("date", "2018-02-03T10:00:00.000Z")
				.build();
		target.request().post(Entity.json(o));

	}
	public void testSave() throws IOException {
		Map<String, String> sensorTypeName = new HashMap<>();
		sensorTypeName.put("p1", "SDS_P1");
		sensorTypeName.put("p2", "SDS_P2");
		sensorTypeName.put("temperature", "temperature");
		sensorTypeName.put("humidity", "humidity");

		URL url = new URL("http://feinstaub.dirkpapenberg.de/feinstaubr/rest/sensor/7620363/day");
		InputStream inputStream = url.openConnection().getInputStream();
        JsonReader reader = Json.createReader(inputStream);
        //FIXME this must be finalized
        JsonArray temp = reader.readObject().getJsonObject("charts").getJsonArray("temperature");
        System.out.println(temp.size());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://35.198.164.239/feinstaubr/rest/sensor/save");
		for (int day = 0; day <= 2; day++) {
	        for (int i = 0; i < temp.size(); i++) {
	        	JsonArray a = temp.getJsonArray(i);
	    		Date date2 = new Date(a.getJsonNumber(0).longValue() * 100000);
	    		Calendar cal = Calendar.getInstance();
	    		cal.setTime(date2);
	    		cal.add(Calendar.DATE, -day);
				String date = f.format(cal.getTime());
	    		JsonObject o = Json.createObjectBuilder()
	    				.add("esp8266id", "7620363")
	    				.add("sensordatavalues", 
	    						Json.createArrayBuilder().add(
	    								Json.createObjectBuilder().add("value_type", sensorTypeName.get("temperature")).add("value", a.getJsonNumber(1).bigDecimalValue().toString())
	    						)
	    					)
	    				.add("date", date)
	    				.build();
	    		target.request().post(Entity.json(o));
	        }
		}
	}
}
