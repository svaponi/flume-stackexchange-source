package it.miriade.flume.source.stackexchange.api;

import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author svaponi
 *
 */
public class ApiConstants {

	/** The Constant PROPERTY_FILE. */
	public static final String PROPERTY_FILE = "application.properties";

	/** The Constant */
	private static final Properties props = new Properties();

	static {
		try {
			props.load(ClassLoader.getSystemResourceAsStream(PROPERTY_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** The Constant STACK_OVERFLOW_API_KEY. */
	public static final String STACK_OVERFLOW_API_KEY = props
			.getProperty("com.google.code.stackexchange.client.apiKey");

	/** The Constant STACK_OVERFLOW_ACCESS_TOKEN. */
	public static final String STACK_OVERFLOW_ACCESS_TOKEN = props
			.getProperty("com.google.code.stackexchange.client.accessToken");

	/** The Constant STACK_EXCHANGE_SITE. */
	public static final String STACK_EXCHANGE_SITE = props
			.getProperty("com.google.code.stackexchange.client.stackexchangesite");

	/** The Constant STACK_OVERFLOW_USER_ID. */
	public static final String STACK_OVERFLOW_USER_ID = props
			.getProperty("com.google.code.stackexchange.client.userId");

	/** The Constant STACK_OVERFLOW_PAGE_NO. */
	public static final String STACK_OVERFLOW_PAGE_NO = props
			.getProperty("com.google.code.stackexchange.client.pageNo");

	/** The Constant STACK_OVERFLOW_PAGE_SIZE. */
	public static final String STACK_OVERFLOW_PAGE_SIZE = props
			.getProperty("com.google.code.stackexchange.client.pageSize");

	/**
	 * Instantiates a new test constants.
	 */
	private ApiConstants() {
	}
}
