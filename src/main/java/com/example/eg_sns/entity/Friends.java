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
 * フレンドEntityクラス。
 *
 * @author ha-koba
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "friends")
public class Friends extends EntityBase {

	/** ID */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** フレンドリクエストを送信したユーザーのID (外部キー) */
	@Column(name = "users_id", nullable = false)
	private String usersId;

	/** フレンドリクエストを受信したユーザーのID (外部キー) */
	@Column(name = "friend_users_id", nullable = false)
	private String friendUsersId;
	
	/** 承認ステータス(1.申請中[自分]、2.承認待ち[相手]、3.承認[自分]、4.承諾[相手] */
	@Column(name = "approval_status", nullable = false)
	private int approvalStatus;
}
