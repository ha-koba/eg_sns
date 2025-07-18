package com.example.eg_sns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * アカウントDTOクラス。
 *
 * @author tomo-sato
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestAccount extends DtoBase {

	/** お名前 */
	@NotBlank(message = "お名前を入力してください。")
	@Size(max = 32, message = "お名前は最大32文字です。")
	private String name;

	/** ログインID */
	@NotBlank(message = "ログインIDを入力してください。")
	@Size(max = 32, message = "ログインIDは最大32文字です。")
	private String loginId;

	/** パスワード */
	@NotBlank(message = "パスワードを入力してください。")
	@Size(max = 32, message = "パスワードは最大32文字です。")
	private String password;

	/** メールアドレス */
	@NotBlank(message = "メールアドレスを入力してください。")
	@Size(max = 32, message = "メールアドレスは最大32文字です。")
	private String emailAddress;
}
