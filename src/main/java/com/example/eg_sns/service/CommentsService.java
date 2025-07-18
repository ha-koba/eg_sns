package com.example.eg_sns.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.eg_sns.dto.RequestTopicComment;
import com.example.eg_sns.entity.Comments;
import com.example.eg_sns.repository.CommentsRepository;

import lombok.extern.log4j.Log4j2;

/**
 * コメント関連サービスクラス。
 *
 * @author tomo-sato
 */
@Log4j2
@Service
public class CommentsService {

	/** リポジトリインターフェース。 */
	@Autowired
	private CommentsRepository repository;

	/**
	 * コメント登録処理を行う。
	 *
	 * @param requestTopicComment コメントDTO
	 * @param usersId ユーザーID
	 * @param topicsId トピックID
	 */
	public Comments save(RequestTopicComment requestTopicComment, Long usersId, Long topicsId) {
		log.info("コメントを登録します。：requestTopicComment={}, usersId={}, topicsId={}", requestTopicComment, usersId, topicsId);
		Comments topics = new Comments();
		topics.setUsersId(usersId);
		topics.setTopicsId(topicsId);
		topics.setBody(requestTopicComment.getBody());
		return repository.save(topics);
	}

	/**
	 * コメントの削除処理を行う。
	 *
	 * @param id コメントID
	 * @param usersId ユーザーID
	 * @param topicsId トピックID
	 */
	public void delete(Long id, Long usersId, Long topicsId) {
		log.info("コメントを削除します。：id={}, usersId={}, topicsId={}", id, usersId, topicsId);

		repository.deleteByIdAndUsersIdAndTopicsId(id, usersId, topicsId);
	}

	/**
	 * コメントの削除処理を行う。
	 *
	 * @param commentsList コメントリスト
	 */
	public void delete(List<Comments> commentsList) {
		repository.deleteAll(commentsList);
	}
}
