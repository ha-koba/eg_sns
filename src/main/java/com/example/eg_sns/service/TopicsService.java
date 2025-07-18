package com.example.eg_sns.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.eg_sns.core.AppNotFoundException;
import com.example.eg_sns.dto.RequestTopic;
import com.example.eg_sns.entity.Comments;
import com.example.eg_sns.entity.TopicImages;
import com.example.eg_sns.entity.Topics;
import com.example.eg_sns.repository.TopicsRepository;
import com.example.eg_sns.util.CollectionUtil;

import lombok.extern.log4j.Log4j2;

/**
 * トピック関連サービスクラス。
 *
 * @author tomo-sato
 */
@Log4j2
@Service
public class TopicsService {

	/** リポジトリインターフェース。 */
	@Autowired
	private TopicsRepository repository;

	/** コメント関連サービスクラス。 */
	@Autowired
	private CommentsService commentsService;

	/** コメント関連サービスクラス。 */
	@Autowired
	private TopicImagesService topicImagesService;

	/**
	 * トピック全件取得する。
	 *
	 * @return トピックを全件取得する。
	 */
	public List<Topics> findAllTopics() {
		return (List<Topics>) repository.findByOrderByIdDesc();
	}

	/**
	 * トピック検索を行う。
	 * トピックIDと、ログインIDを指定し、トピックを検索する。
	 *
	 * @param id トピックID
	 * @return トピック情報を返す。
	 */
	public Topics findTopics(Long id) {
		log.info("トピックを検索します。：id={}", id);

		Topics topics = repository.findById(id).orElse(null);
		log.info("ユーザー検索結果。：id={}, topics={}", id, topics);

		return topics;
	}

	/**
	 * トピック登録処理を行う。
	 *
	 * @param requestTopic トピックDTO
	 * @param usersId ユーザーID
	 */
	public Topics save(RequestTopic requestTopic, Long usersId) {
		Topics topics = new Topics();
		topics.setUsersId(usersId);
		topics.setTitle(requestTopic.getTitle());
		topics.setBody(requestTopic.getBody());
		return repository.save(topics);
	}

	/**
	 * トピックの削除処理を行う。
	 *
	 * @param topicsId トピックID
	 * @param usersId ユーザーID
	 */
	public void delete(Long topicsId, Long usersId) {
		log.info("トピックを削除します。：topicsId={}, usersId={}", topicsId, usersId);

		// 対象のトピックを検索。
		Topics topics = repository.findByIdAndUsersId(topicsId, usersId).orElse(null);
		if (topics == null) {
			// データが取得できない場合は不正操作の為エラー。（404エラーとする。）
			throw new AppNotFoundException();
		}

		// トピックにぶら下がってるコメントを削除。
		List<Comments> commentsList = topics.getCommentsList();
		if (CollectionUtil.isNotEmpty(commentsList)) {
			commentsService.delete(commentsList);
		}

		// トピックにぶら下がってる画像を削除。
		List<TopicImages> topicImagesList = topics.getTopicImagesList();
		if (CollectionUtil.isNotEmpty(topicImagesList)) {
			topicImagesService.delete(topicImagesList);
		}

		// トピックを削除。
		repository.delete(topics);
	}
}
