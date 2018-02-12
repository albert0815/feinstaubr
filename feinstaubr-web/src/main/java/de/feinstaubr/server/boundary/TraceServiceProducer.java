package de.feinstaubr.server.boundary;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.google.cloud.trace.Trace;
import com.google.cloud.trace.service.TraceGrpcApiService;
import com.google.cloud.trace.service.TraceService;

@Startup
@Singleton
public class TraceServiceProducer {
	private static final Logger LOGGER = Logger.getLogger(TraceServiceProducer.class.getName());

	@PostConstruct
	public void setupTracerService() {
	    try {			
	    	TraceService traceService = TraceGrpcApiService.builder()
		        .setProjectId("feinstaubr").build();
	    	Trace.init(traceService);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "unable to initialize trace service", e);
		}
	}
}
