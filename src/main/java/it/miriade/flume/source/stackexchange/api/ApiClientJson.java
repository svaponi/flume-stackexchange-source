package it.miriade.flume.source.stackexchange.api;

import java.util.Calendar;
import java.util.Date;

import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.PostTimeline;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.Tag;
import com.google.code.stackexchange.schema.TimePeriod;

/**
 * 
 * @author svaponi
 *
 */
public class ApiClientJson extends ApiClient {

	public ApiClientJson() throws Exception {
		super();
	}

	/**
	 * Gets the time period.
	 * 
	 * @return the time period
	 */
	protected TimePeriod getDefaultTimePeriod() {
		return new TimePeriod(getLastWeekDate(), new Date());
	}

	/**
	 * Gets the current date.
	 * 
	 * @return the current date
	 */
	protected Date getCurrentDate() {
		return new Date();
	}

	/**
	 * Gets the last week date.
	 * 
	 * @return the last week date
	 */
	protected Date getLastWeekDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		return calendar.getTime();
	}

	/**
	 * Get favorite questions by user long paging.
	 * 
	 * @param userIds
	 * 
	 * @return
	 */
	public PagedList<Question> getFavoriteQuestionsByUserLongPaging(long[] userIds) {
		PagedList<Question> favoriteQuestionsByUser = client.getFavoriteQuestionsByUsers(getDefaultPaging(), userIds);
		return favoriteQuestionsByUser;
	}

	/**
	 * Get favorite questions by user long time period.
	 */
	public PagedList<Question> getFavoriteQuestionsByUserLongTimePeriod(long[] userIds) {
		PagedList<Question> favoriteQuestionsByUser = client.getFavoriteQuestionsByUsers(getDefaultTimePeriod(),
				userIds);
		return favoriteQuestionsByUser;
	}

	/**
	 * Get question long paging.
	 */
	public PagedList<Question> getQuestionLongPaging(long[] questionIds) {
		PagedList<Question> questions = client.getQuestions(getDefaultPaging(), questionIds);
		return questions;
	}

	/**
	 * Get question timeline long time period.
	 */
	public PagedList<PostTimeline> getQuestionTimelineLongTimePeriod(long[] questionIds) {
		PagedList<PostTimeline> questions = client.getQuestionsTimeline(getDefaultTimePeriod(), questionIds);
		return questions;
	}

	/**
	 * Get questions time period.
	 */
	public PagedList<Question> getQuestionsTimePeriod() {
		PagedList<Question> questions = client.getQuestions(getDefaultTimePeriod());
		return questions;
	}

	/**
	 * Get questions by user long paging.
	 */
	public PagedList<Question> getQuestionsByUser(long userId) {
		PagedList<Question> questions = client.getQuestionsByUsers(getDefaultPaging(), userId);
		return questions;
	}

	/**
	 * Get questions by user long time period.
	 */
	public PagedList<Question> getQuestionsByUserLongTimePeriod(long userId) {
		PagedList<Question> questions = client.getQuestionsByUsers(getDefaultTimePeriod(), userId);
		return questions;
	}

	/**
	 * Get tags paging.
	 */
	public PagedList<Tag> getTagsPaging() {
		PagedList<Tag> tags = client.getTags(getDefaultPaging());
		return tags;
	}

	/**
	 * Get tags tag sort order paging.
	 */
	public PagedList<Tag> getTagsPaging(Tag.SortOrder order) {
		PagedList<Tag> tags = client.getTags(order, getDefaultPaging());
		return tags;
	}

	/**
	 * Get answers by user long.
	 */
	public PagedList<Answer> getAnswersByUserLong(long userId) {
		PagedList<Answer> answers = client.getAnswersByUsers(userId);
		return answers;
	}

	/**
	 * Get answers by question long.
	 */
	public PagedList<Answer> getAnswersByQuestionLong(long questionId) {
		PagedList<Answer> answers = client.getAnswersByQuestions(questionId);
		return answers;
	}

	/**
	 * Get user comments long.
	 */
	public PagedList<Comment> getUserCommentsLong(long userId) {
		PagedList<Comment> comments = client.getUsersComments(userId);
		return comments;
	}

}
