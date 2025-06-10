package com.example.eg_sns.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestPasswordChangeForm extends DtoBase {

	/** 現在のパスワード */
	private String currentPassword;

	/** 新しいパスワード */
	private String newPassword;

	/** 新しいパスワード（再入力） */
	private String confirmPassword;
}
