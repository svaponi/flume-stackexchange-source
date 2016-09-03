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

import java.util.Collections;
import java.util.Comparator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.Tag;

import it.miriade.commons.utils.DateHandler;
import it.miriade.commons.utils.StringHandler;
import it.miriade.flume.source.stackexchange.api.ApiClient;

public class TestApiClient extends Assert {

	private static final Logger LOG = LoggerFactory.getLogger(TestApiClient.class);
	private static final String format = "yyyy/MM/dd HH:mm";

	static ApiClient client;

	@BeforeClass
	public static void setUp() {
		client = new ApiClient();
		LOG.info("Client initialized!");
	}

	@Test
	public void getToken() throws Exception {
		LOG.info("Getting AccessToken...");
		String token = client.getQueryFactory().newPostApiQuery().getAccessToken();
		Assert.assertNotNull("AccessToken shouldn't be null", token);
		LOG.info(" - AccessToken: {}", token);
	}

	@Test
	public void getQuestionsTest() throws Exception {
		try {
			LOG.info("Getting questions...");
			PagedList<Question> questions = client.getQuestionsPaging();
			Assert.assertNotNull("Questions shouldn't be null", questions);

			LOG.info("Questions: ");
			for (Question item : questions) {
				Assert.assertNotNull("Question shouldn't be null", item);
				LOG.info("[{}] {}\n- {} ({} answers)", DateHandler.formatDate(item.getCreationDate(), format),
						item.getOwner().getDisplayName(), item.getTitle(), item.getAnswerCount());

				for (Answer answer : client.getAnswersByQuestion(item.getQuestionId())) {
					LOG.info("\t{}\n\t- {} {} {} {} {} {} ({} comments)",
							DateHandler.formatDate(answer.getCreationDate(), format), answer.getAnswerId(),
							answer.getAnswerCommentsUrl(), answer.getBody(), answer.getOwner().getDisplayName(),
							answer.getTitle(), answer.getUpVoteCount(), answer.getComments().size());
				}
			}

		} catch (Exception e) {
			LOG.error("Exception: " + e.getMessage());
		}
	}

	@Test
	public void getTagesTest() throws Exception {
		try {
			LOG.info("Getting tags...");
			PagedList<Tag> tags = client.getTags();
			Assert.assertNotNull("Tags shouldn't be null", tags);
			LOG.info("Tags: ");
			Comparator<? super Tag> c = new Comparator<Tag>() {
				public int compare(Tag o1, Tag o2) {
					if (o1 == null && o2 == null)
						return 0;
					else if (o1 == null)
						return 1;
					else if (o2 == null)
						return -1;
					return o1.getName().compareTo(o2.getName());
				}
			};
			Collections.sort(tags, c);
			for (Tag tag : tags)
				LOG.info("- {}{}", StringHandler.right(tag.getName(), 20, ' '), tag.getCount());

		} catch (Exception e) {
			LOG.error("Exception: " + e.getMessage());
		}
	}

}
