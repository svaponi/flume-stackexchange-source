/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package it.miriade.flume.source.stackexchange;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestApiScheduler extends Assert {

	private static final Logger LOG = LoggerFactory.getLogger(TestApiScheduler.class);

	static ApiScheduler job;

	@BeforeClass
	public static void setUp() {
		job = new ApiScheduler();
		Map<String, String> map = new HashMap<>();
		map.put("poller.fixedrate", String.valueOf(10));
		map.put("poller.timeunit", "SECONDS");
		LOG.info("Configuration: {}", map);
		job.configure(map);
		job.setListener(new TestApiListener());
	}

	@Test
	public void getToken() throws Exception {
		job.start();
		Thread.sleep(10000);
		job.stop();
		Thread.sleep(10000);
	}

}
