package de.feinstaubr.server.boundary;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class TraceServiceProducer {
	private static final Logger LOGGER = Logger.getLogger(TraceServiceProducer.class.getName());

	@PostConstruct
	public void setupTracerService() {
		
//	    try {			
//	    	TraceService traceService = TraceGrpcApiService.builder()
//		        .setProjectId("feinstaubr").build();
//	    	Trace.init(traceService);
//		} catch (IOException e) {
//			LOGGER.log(Level.SEVERE, "unable to initialize trace service", e);
//		}
	}
}
