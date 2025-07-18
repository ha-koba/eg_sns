package com.example.eg_sns.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * トピックイメージEntityクラス。
 *
 * @author tomo-sato
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "topic_images")
public class TopicImages extends EntityBase {

	/** ID */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** トピックID */
	@Column(name = "topics_id", nullable = false)
	private Long topicsId;

	/** 画像URI */
	@Column(name = "image_uri", nullable = false)
	private String imageUri;
}
