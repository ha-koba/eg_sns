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
	
	/** フレンド申請の状態 */
	private int approval_status;
}
