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

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.Question;

import it.miriade.flume.source.stackexchange.api.ApiClient;

public class TestApiClient2 extends Assert {

	private static final Logger LOG = LoggerFactory.getLogger(TestApiClient2.class);

	static ApiClient client;

	@BeforeClass
	public static void setUp() {
		client = new ApiClient();
		LOG.info("Client initialized!");
	}

	@Test
	public void getCommentsTest() throws Exception {
		try {
			LOG.info("Getting questions...");
			PagedList<Question> qs = client.getQuestionsPaging();
			Assert.assertNotNull("PagedList<Question> shouldn't be null", qs);
			Assert.assertTrue("PagedList<Question> shouldn't be empty", !qs.isEmpty());
			LOG.info("Questions: {}", qs.size());
			for (Question q : qs) {
				List<Comment> cs = q.getComments();
				Assert.assertNotNull("List<Comment> shouldn't be null", cs);
				// Assert.assertTrue("List<Comment> shouldn't be empty",
				// !cs.isEmpty());
				LOG.info("Question {} has {} comments", q.getQuestionId(), cs.size());
				for (Comment c : cs) {
					Comment comment = client.getComment(c.getCommentId());
					Assert.assertNotNull("Comment shouldn't be null", comment);
					LOG.info("Comment {} parent is => {} {}", c.getCommentId(), c.getPostId(), c.getPostType());
				}

				PagedList<Answer> answers = client.getAnswersByQuestion(q.getQuestionId());
				Assert.assertNotNull("PagedList<Answer> shouldn't be null", answers);
				// Assert.assertTrue("PagedList<Answer> shouldn't be empty",
				// !answers.isEmpty());
				LOG.info("Question {} has {} answers", q.getQuestionId(), answers.size());
				for (Answer a : answers) {
					List<Comment> cs2 = a.getComments();
					LOG.info("Answer {} has {} comments", a.getQuestionId(), cs2.size());
					for (Comment c : cs2) {
						Comment comment = client.getComment(c.getCommentId());
						Assert.assertNotNull("Comment shouldn't be null", comment);
						LOG.info("Comment {} parent is => {} {}", c.getCommentId(), c.getPostId(), c.getPostType());
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Exception: " + e.getMessage());
			Assert.assertTrue("Exception shouldn't happen", false);
		}
	}
}
