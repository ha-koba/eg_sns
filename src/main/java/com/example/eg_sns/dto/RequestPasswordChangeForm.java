package com.example.eg_sns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestPasswordChangeForm extends DtoBase {

	/** 現在のパスワード */
	@NotBlank(message = "現在のパスワードを入力してください。")
	@Size(max = 32, message = "パスワードは最大32文字です。")
	private String currentPassword;

	/** 新しいパスワード */
	@NotBlank(message = "新しいパスワードを入力してください。")
	@Size(max = 32, message = "パスワードは最大32文字です。")
	private String newPassword;

	/** 新しいパスワード（再入力） */
	@NotBlank(message = "新しいパスワード（再入力）を入力してください。")
	@Size(max = 32, message = "パスワードは最大32文字です。")
	private String confirmPassword;
}
