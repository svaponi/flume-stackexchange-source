package it.miriade.flume.source.stackexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.flume.conf.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.Question;

import it.miriade.flume.source.stackexchange.api.ApiClient;

/**
 * @author svaponi
 */
public final class ApiScheduler extends TimerTask {

	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String FIXEDRATE = "poller.fixedrate";
	public static final String TIMEUNIT = "poller.timeunit";

	private long millisfixedrate;
	private long fixedrate;
	private String timeunit;
	private Timer timer;
	private ApiClient client;
	private List<Long> questionIds;
	private List<Long> answerIds;
	private List<Long> commentIds;
	private List<Listener> listeners;
	private boolean configured;

	public ApiScheduler() {
		super();
		this.client = new ApiClient();
		this.questionIds = new ArrayList<>();
		this.answerIds = new ArrayList<>();
		this.commentIds = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}

	public void setListener(Listener listener) {
		Assert.notNull(listener, "Listener should not be null");
		log.info("Add Listener {}", listener.getClass());
		if (this.listeners == null)
			this.listeners = new ArrayList<>();
		this.listeners.add(listener);
	}

	public void configure(Map<String, String> map) {

		log.info("Configuration properties: {}", map);

		if (!map.containsKey(FIXEDRATE))
			throw new ConfigurationException("Missing \"" + FIXEDRATE + "\" param");

		try {
			fixedrate = Long.parseLong(map.get(FIXEDRATE));
		} catch (Exception e) {
			throw new ConfigurationException("Invalid \"" + FIXEDRATE + "\" param: " + e.getMessage());
		}
		log.info("\"{}\": {}", FIXEDRATE, fixedrate);

		if (!map.containsKey(TIMEUNIT))
			throw new ConfigurationException("Missing \"" + TIMEUNIT + "\" param");

		try {
			timeunit = map.get(TIMEUNIT);
			TimeUnit unit = TimeUnit.valueOf(timeunit);
			millisfixedrate = unit.toMillis(fixedrate);
		} catch (Exception e) {
			throw new ConfigurationException("Invalid \"" + TIMEUNIT + "\" param: [" + timeunit + "] not in "
					+ Arrays.deepToString(TimeUnit.values()));
		}
		log.info("\"{}\": {}", TIMEUNIT, timeunit);

		configured = true;
		log.info("{} configured", getClass().getName());
	}

	public void start() {
		if (!configured)
			throw new ConfigurationException("Missing configuration");

		if (listeners.isEmpty())
			throw new RuntimeException("No listeners. Set at least a listener");

		if (timer == null)
			timer = new Timer();
		timer.scheduleAtFixedRate(this, new Date(), millisfixedrate);
		log.info("{} started", getClass().getName());
	}

	public void stop() {
		timer.cancel();
		log.info("{} stopped", getClass().getName());
	}

	/**
	 * Implements TimerTask's abstract run method.
	 */
	@Override
	public void run() {
		PagedList<Question> questions = client.getQuestions();
		Set<Long> conmmentsIds = new HashSet<>();
		for (Question q : questions) {
			long questionId = q.getQuestionId();
			if (!existsQ(questionId))
				try {
					for (Comment c : q.getComments())
						conmmentsIds.add(c.getCommentId());
					for (Listener listener : listeners)
						listener.send(q);
					sentQ(questionId);
				} catch (Exception e) {
					log.error("Exception while sending Question " + questionId, e);
				}

			// cerco le risposte per ogni domanda
			PagedList<Answer> answers = client.getAnswersByQuestion(questionId);
			for (Answer a : answers) {
				long answerId = a.getAnswerId();
				if (!existsA(answerId))
					try {
						for (Comment c : a.getComments())
							conmmentsIds.add(c.getCommentId());
						for (Listener listener : listeners)
							listener.send(a);
						sentA(answerId);
					} catch (Exception e) {
						log.error("Exception while sending Answer " + answerId, e);
					}
			}
		}

		// cerco le risposte per ogni domanda
		for (long commentId : conmmentsIds) {
			if (!existsC(commentId))
				try {
					Comment c = client.getComment(commentId);
					for (Listener listener : listeners)
						listener.send(c);
					sentC(commentId);
				} catch (Exception e) {
					log.error("Exception while sending Comment " + commentId, e);
				}
		}
	}

	/**
	 * Controlla se la question è già passata
	 * 
	 * @param feed
	 * @return
	 */
	private boolean existsQ(long id) {
		// TODO: aggiungere persistenza alla logica di controllo
		return questionIds.contains(id);
	}

	private boolean existsA(long id) {
		// TODO: aggiungere persistenza alla logica di controllo
		return answerIds.contains(id);
	}

	private boolean existsC(long id) {
		// TODO: aggiungere persistenza alla logica di controllo
		return commentIds.contains(id);
	}

	/**
	 * Registra che la question è stata inviata
	 * 
	 * @param feed
	 * @return
	 */
	private void sentQ(long id) {
		log.info("Sent question {}", id);
		questionIds.add(id);
	}

	private void sentA(long id) {
		log.info("Sent answer {}", id);
		answerIds.add(id);
	}

	private void sentC(long id) {
		log.info("Sent comment {}", id);
		commentIds.add(id);
	}
}