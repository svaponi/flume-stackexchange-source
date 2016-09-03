package it.miriade.flume.source.stackexchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.code.stackexchange.schema.SchemaEntity;

public class TestApiListener implements Listener {

	Logger logger = LoggerFactory.getLogger(getClass());
	int counter;

	public TestApiListener() {
		super();
		logger.info("Initialize {}", TestApiListener.class.getName());
	}

	@Override
	public void send(SchemaEntity e) {
		Assert.notNull(e, e.getClass().getSimpleName() + " should be not null");
		String type = e.getClass().getSimpleName().toLowerCase();
		logger.info(" {} > {} {}", ++counter, type, e.toString());
	}
}