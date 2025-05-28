package com.example.eg_sns.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フレンドリストのDTOクラス。
 *
 * @author ha-koba
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestFriend extends DtoBase {

	/** フレンドリクエストを送信したユーザーのID (外部キー) */
	private String usersId;

	/** フレンドリクエストを受信したユーザーのID (外部キー) */
	private String friendUsersId;
	
	/** 承認ステータス(1.申請中[自分]、2.承認待ち[相手]、3.承認[自分]、4.承諾[相手] */
	private int approval_status;
}
