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
	private Long usersId;

	/** フレンドリクエストを受信したユーザーのID (外部キー) */
	private Long friendUsersId;

	/** 承認ステータス */
	private int approval_status;
}
