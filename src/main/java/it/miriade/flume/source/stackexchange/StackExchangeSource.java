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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.annotations.InterfaceAudience;
import org.apache.flume.annotations.InterfaceStability;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.PostType;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.SchemaEntity;
import com.google.common.collect.ImmutableMap;

import it.miriade.commons.utils.DateHandler;
import it.miriade.commons.utils.StringHandler;

/**
 * Demo Flume source that connects via Streaming API to the 1% sample
 * StackExchange firehose, continously downloads tweets, converts them to Avro
 * format and sends Avro events to a downstream Flume sink.
 *
 * Requires the consumer and access tokens and secrets of a StackExchange
 * developer account
 */

@InterfaceAudience.Private
@InterfaceStability.Unstable
public class StackExchangeSource extends AbstractSource implements EventDrivenSource, Configurable, Listener {

	public static final String STACKEXCHANGE_API_PREFIX = "stackexchange.api.";

	protected ApiScheduler poller;

	private Schema avroSchema;

	private long docCount = 0;
	private long startTime = 0;
	private long exceptionCount = 0;
	private long totalTextIndexed = 0;
	private long skippedDocs = 0;
	private long batchEndTime = 0;
	private final List<Record> docs = new ArrayList<Record>();
	private final ByteArrayOutputStream serializationBuffer = new ByteArrayOutputStream();
	private DataFileWriter<GenericRecord> dataFileWriter;

	private String name;
	private int maxBatchSize = 1000;
	private int maxBatchDurationMillis = 1000;

	// Fri May 14 02:52:55 +0000 2010
	private String formatTo = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private DecimalFormat numFormatter = new DecimalFormat("###,###.###");

	private static int REPORT_INTERVAL = 100;
	private static int STATS_INTERVAL = REPORT_INTERVAL * 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeSource.class);

	public StackExchangeSource() {
		super();
		this.poller = new ApiScheduler();
		this.poller.setListener(this);
	}

	@Override
	public void configure(Context context) {

		name = context.getString("name", StackExchangeSource.class.getSimpleName());
		avroSchema = createAvroSchema();
		dataFileWriter = new DataFileWriter<GenericRecord>(new GenericDatumWriter<GenericRecord>(avroSchema));

		maxBatchSize = context.getInteger("maxBatchSize", maxBatchSize);
		maxBatchDurationMillis = context.getInteger("maxBatchDurationMillis", maxBatchDurationMillis);

		ImmutableMap<String, String> map = context.getSubProperties(STACKEXCHANGE_API_PREFIX);
		poller.configure(map);
	}

	@Override
	public synchronized String getName() {
		return name;
	}

	@Override
	public synchronized void start() {
		LOGGER.info("Starting StackExchange source {} ...", this);
		poller.start();
		docCount = 0;
		startTime = System.currentTimeMillis();
		exceptionCount = 0;
		totalTextIndexed = 0;
		skippedDocs = 0;
		batchEndTime = System.currentTimeMillis() + maxBatchDurationMillis;
		LOGGER.info("StackExchange source {} started.", getName());
		// This should happen at the end of the start method, since this will
		// change the lifecycle status of the component to tell the Flume
		// framework that this component has started. Doing this any earlier
		// tells the framework that the component started successfully, even
		// if the method actually fails later.
		super.start();
	}

	@Override
	public synchronized void stop() {
		LOGGER.info("StackExchange source {} stopping...", getName());
		poller.stop();
		super.stop();
		LOGGER.info("StackExchange source {} stopped.", getName());
	}

	@Override
	public void send(SchemaEntity entity) {
		Record doc = extractRecord(avroSchema, entity);
		if (doc == null) {
			return; // skip
		}
		docs.add(doc);
		if (docs.size() >= maxBatchSize || System.currentTimeMillis() >= batchEndTime) {
			batchEndTime = System.currentTimeMillis() + maxBatchDurationMillis;
			byte[] bytes;
			try {
				bytes = serializeToAvro(avroSchema, docs);
			} catch (IOException e) {
				LOGGER.error("Exception while serializing tweet", e);
				return; // skip
			}
			Event event = EventBuilder.withBody(bytes);
			getChannelProcessor().processEvent(event); // send event to the
														// flume sink
			docs.clear();
		}
		docCount++;
		if ((docCount % REPORT_INTERVAL) == 0) {
			LOGGER.info(String.format("Processed %s docs", numFormatter.format(docCount)));
		}
		if ((docCount % STATS_INTERVAL) == 0) {
			logStats();
		}
	}

	private Schema createAvroSchema() {
		Schema avroSchema = Schema.createRecord("Doc", "adoc", null, false);
		List<Field> fields = new ArrayList<Field>();
		fields.add(new Field("type", Schema.create(Type.STRING), null, null));
		fields.add(new Field("id", Schema.create(Type.LONG), null, null));
		fields.add(new Field("user_id", Schema.create(Type.LONG), null, null));
		fields.add(new Field("title", createOptional(Schema.create(Type.STRING)), null, null));
		fields.add(new Field("body", createOptional(Schema.create(Type.STRING)), null, null));
		fields.add(new Field("tags", createOptional(Schema.create(Type.STRING)), null, null));
		fields.add(new Field("url", createOptional(Schema.create(Type.STRING)), null, null));
		// (required) data di creazione
		fields.add(new Field("created_date", Schema.create(Type.STRING), null, null));
		// (optional) data ultima modifica, data chiusura e motivo chiusura
		fields.add(new Field("last_activity_date", createOptional(Schema.create(Type.STRING)), null, null));
		fields.add(new Field("closed_date", createOptional(Schema.create(Type.STRING)), null, null));
		fields.add(new Field("closed_reason", createOptional(Schema.create(Type.STRING)), null, null));
		// (optional) link al post padre
		fields.add(new Field("parent_id", Schema.create(Type.LONG), null, null));
		fields.add(new Field("parent_type", createOptional(Schema.create(Type.STRING)), null, null));
		avroSchema.setFields(fields);
		return avroSchema;
	}

	private Record extractRecord(Schema avroSchema, SchemaEntity entity) {
		Record doc = new Record(avroSchema);
		if (entity instanceof Question) {
			Question e = (Question) entity;
			addString(doc, "type", PostType.QUESTION.value());
			doc.put("id", e.getQuestionId());
			doc.put("user_id", e.getOwner().getUserId());
			addString(doc, "title", e.getTitle());
			addString(doc, "body", e.getBody());
			addString(doc, "tags", StringHandler.join(",", e.getTags()));
			addString(doc, "url", e.getQuestionUrl());

			// Date di creazione, ultima modifica e chiusura del post
			addString(doc, "created_date", DateHandler.formatDate(e.getCreationDate(), formatTo));
			addString(doc, "last_activity_date", DateHandler.formatDate(e.getLastActivityDate(), formatTo));
			addString(doc, "closed_date", DateHandler.formatDate(e.getClosedDate(), formatTo));
			addString(doc, "closed_reason", e.getClosedReason());

			// question non hanno padre dunque metto a 0
			doc.put("parent_id", (long) 0);

		} else if (entity instanceof Answer) {
			Answer e = (Answer) entity;
			addString(doc, "type", PostType.ANSWER.value());
			doc.put("id", e.getAnswerId());
			doc.put("user_id", e.getOwner().getUserId());
			addString(doc, "title", e.getTitle());
			addString(doc, "body", e.getBody());

			// Date di creazione e ultima modifica
			addString(doc, "created_date", DateHandler.formatDate(e.getCreationDate(), formatTo));
			addString(doc, "last_activity_date", DateHandler.formatDate(e.getLastActivityDate(), formatTo));

			// link alla relativa domanda
			doc.put("parent_id", e.getQuestionId());
			addString(doc, "parent_type", PostType.QUESTION.value());

		} else if (entity instanceof Comment) {
			Comment e = (Comment) entity;
			addString(doc, "type", PostType.COMMENT.value());
			doc.put("id", e.getCommentId());
			doc.put("user_id", e.getOwner().getUserId());
			addString(doc, "body", e.getBody());

			// Date di creazione
			addString(doc, "created_date", DateHandler.formatDate(e.getCreationDate(), formatTo));

			// link al post padre
			doc.put("parent_id", e.getPostId());
			addString(doc, "parent_type", e.getPostType().value());
		}
		return doc;
	}

	private byte[] serializeToAvro(Schema avroSchema, List<Record> docList) throws IOException {
		serializationBuffer.reset();
		dataFileWriter.create(avroSchema, serializationBuffer);
		for (Record doc2 : docList) {
			dataFileWriter.append(doc2);
		}
		dataFileWriter.close();
		return serializationBuffer.toByteArray();
	}

	private Schema createOptional(Schema schema) {
		return Schema.createUnion(Arrays.asList(new Schema[] { schema, Schema.create(Type.NULL) }));
	}

	private void addString(Record doc, String avroField, String val) {
		if (val == null) {
			return;
		}
		doc.put(avroField, val);
		totalTextIndexed += val.length();
	}

	private void logStats() {
		double mbIndexed = totalTextIndexed / (1024 * 1024.0);
		long seconds = (System.currentTimeMillis() - startTime) / 1000;
		seconds = Math.max(seconds, 1);
		LOGGER.info(String.format("Total docs indexed: %s, total skipped docs: %s", numFormatter.format(docCount),
				numFormatter.format(skippedDocs)));
		LOGGER.info(String.format("    %s docs/second", numFormatter.format(docCount / seconds)));
		LOGGER.info(String.format("Run took %s seconds and processed:", numFormatter.format(seconds)));
		LOGGER.info(String.format("    %s MB/sec sent to index",
				numFormatter.format(((float) totalTextIndexed / (1024 * 1024)) / seconds)));
		LOGGER.info(String.format("    %s MB text sent to index", numFormatter.format(mbIndexed)));
		LOGGER.info(String.format("There were %s exceptions ignored: ", numFormatter.format(exceptionCount)));
	}

}
