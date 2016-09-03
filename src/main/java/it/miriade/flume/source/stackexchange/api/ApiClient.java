package it.miriade.flume.source.stackexchange.api;

import com.google.code.stackexchange.client.StackExchangeApiClient;
import com.google.code.stackexchange.client.StackExchangeApiClientFactory;
import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.Paging;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.StackExchangeSite;
import com.google.code.stackexchange.schema.Tag;
import com.google.code.stackexchange.schema.User;

import it.miriade.commons.utils.StringHandler;

/**
 * 
 * @author svaponi
 *
 */
public class ApiClient {

	/** The factory. */
	protected StackExchangeApiClientFactory factory;

	/** The client. */
	protected StackExchangeApiClient client;

	/** The query factory. */
	protected StackExchangeApiQueryFactory queryFactory;

	/** The Constant RESOURCE_MISSING_MESSAGE. */
	protected static final String RESOURCE_MISSING_MESSAGE = "Please define a %s in " + ApiConstants.PROPERTY_FILE
			+ " file.";

	public ApiClient() {
		super();
		this.setUp();
	}

	/**
	 * Get the client
	 * 
	 * @return
	 */
	public StackExchangeApiClient getClient() {
		return client;
	}

	/**
	 * Get the queryFactory
	 * 
	 * @return
	 */
	public StackExchangeApiQueryFactory getQueryFactory() {
		return queryFactory;
	}

	/**
	 * Initialize the object
	 */
	public void setUp() {
		if (StringHandler.noText(ApiConstants.STACK_OVERFLOW_API_KEY))
			throw new RuntimeException(String.format(RESOURCE_MISSING_MESSAGE, "Application Key"));

		if (StringHandler.noText(ApiConstants.STACK_EXCHANGE_SITE))
			throw new RuntimeException(String.format(RESOURCE_MISSING_MESSAGE, "Stack Exchange Site"));

		if (StringHandler.noText(ApiConstants.STACK_OVERFLOW_PAGE_SIZE))
			throw new RuntimeException(String.format(RESOURCE_MISSING_MESSAGE, "Default Page Size"));

		factory = StackExchangeApiClientFactory.newInstance(ApiConstants.STACK_OVERFLOW_API_KEY,
				StackExchangeSite.fromValue(ApiConstants.STACK_EXCHANGE_SITE));
		// ApiConstants.STACK_OVERFLOW_ACCESS_TOKEN,
		client = factory.createStackExchangeApiClient();
		queryFactory = StackExchangeApiQueryFactory.newInstance(ApiConstants.STACK_OVERFLOW_API_KEY,
				ApiConstants.STACK_OVERFLOW_ACCESS_TOKEN,
				StackExchangeSite.fromValue(ApiConstants.STACK_EXCHANGE_SITE));
	}

	/**
	 * Destroy the object
	 */
	public void tearDown() {
		factory = null;
		client = null;
		queryFactory = null;
	}

	/**
	 * Gets the paging.
	 * 
	 * @return the paging
	 */
	protected Paging getDefaultPaging() {
		return new Paging(Integer.parseInt(ApiConstants.STACK_OVERFLOW_PAGE_NO),
				Integer.parseInt(ApiConstants.STACK_OVERFLOW_PAGE_SIZE));
	}

	/*
	 * Public methods
	 */

	/**
	 * Get users.
	 */
	public PagedList<User> getUsers() {
		PagedList<User> users = client.getUsers();
		return users;
	}

	/**
	 * Get users paging.
	 */
	public PagedList<User> getUsersPaging() {
		PagedList<User> users = client.getUsers(getDefaultPaging());
		return users;
	}

	/**
	 * Get tags.
	 */
	public PagedList<Tag> getTags() {
		PagedList<Tag> tags = client.getTags();
		return tags;
	}

	/**
	 * Get tags paging.
	 */
	public PagedList<Tag> getTagsPaging() {
		PagedList<Tag> tags = client.getTags(getDefaultPaging());
		return tags;
	}

	/**
	 * Get questions.
	 */
	public PagedList<Question> getQuestions() {
		PagedList<Question> questions = client.getQuestions();
		return questions;
	}

	/**
	 * Get questions paging.
	 */
	public PagedList<Question> getQuestionsPaging() {
		PagedList<Question> questions = client.getQuestions(getDefaultPaging());
		return questions;
	}

	/**
	 * Get answers by question long.
	 */
	public PagedList<Answer> getAnswersByQuestion(long questionId) {
		PagedList<Answer> answers = client.getAnswersByQuestions(questionId);
		return answers;
	}

	/**
	 * Get answer.
	 */
	public Answer getAnswer(long answerId) {
		PagedList<Answer> answers = client.getAnswers(answerId);
		return answers == null ? null : answers.get(0);
	}

	/**
	 * Get Comments
	 */
	public Comment getComment(long conmmentId) {
		PagedList<Comment> comments = client.getComments(conmmentId);
		return comments == null ? null : comments.get(0);
	}

}
