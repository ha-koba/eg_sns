package com.example.eg_sns.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.eg_sns.entity.TopicImages;
import com.example.eg_sns.repository.TopicImagesRepository;

import lombok.extern.log4j.Log4j2;

/**
 * トピックイメージ関連サービスクラス。
 *
 * @author ha-koba
 */
@Log4j2
@Service
public class TopicImagesService {

	/** リポジトリインターフェース。 */
	@Autowired
	private TopicImagesRepository repository;

	/**
	 * トピックイメージ登録処理を行う。
	 *
	 * @param imageUri イメージURI
	 * @param topicsId トピックID
	 */
	public TopicImages save(Long topicsId, String imageUri) {
		TopicImages topicImages = new TopicImages();
		topicImages.setTopicsId(topicsId);
		topicImages.setImageUri(imageUri);
		return repository.save(topicImages);
	}

	/**
	 * トピックイメージの削除処理を行う。
	 *
	 * @param topicImagesList トピックイメージのリスト
	 */
	public void delete(List<TopicImages> topicImagesList) {
		repository.deleteAll(topicImagesList);
	}
}
