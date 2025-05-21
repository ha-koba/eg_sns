package com.example.eg_sns.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.eg_sns.dto.RequestAccount;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.UsersService;
import com.example.eg_sns.util.StringUtil;

import lombok.extern.log4j.Log4j2;

/**
 * アカウント登録コントローラー。
 *
 * @author tomo-sato
 */
@Log4j2
@Controller
@RequestMapping("/account")
public class AccountController {

	/** ユーザー関連サービスクラス。 */
	@Autowired
	private UsersService usersService;

	/**
	 * [GET]アカウント作成入力フォームのアクション
	 * 
	 * @param model 入力フォームのオブジェクト
	 */
	@GetMapping(path = { "", "/" })
	public String index(Model model) {

		log.info("アカウント作成入力画面のアクションが呼ばれました。");

		if (!model.containsAttribute("requestAccount")) {
			model.addAttribute("requestAccount", new RequestAccount());
		}
		return "/account/index";
	}

	/**
	 * [POST]アカウント作成アクション
	 * 
	 * @param requestAccount 入力フォームの内容
	 * @param result バリデーション結果
	 * @param redirectAttributes リダイレクト時に使用するオブジェクト
	 */
	@PostMapping("/regist")
	public String regist(@Validated @ModelAttribute RequestAccount requestAccount,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		log.info("アカウント作成処理のアクションが呼ばれました。：requestAccount={}, result={}", requestAccount, result);

		if (result.hasErrors()) {
			// javaScriptのバリデーションを改ざんしてリクエストした場合に通る処理。
			log.warn("バリデーションエラーが発生しました。: requestAccount={}, result={}", requestAccount, result);

			redirectAttributes.addFlashAttribute("validationErrors", result);
			redirectAttributes.addFlashAttribute("requestAccount", requestAccount);
			
			// エラーメッセージのリストを渡す
			List<String> messages = result.getAllErrors()
					.stream()
					.map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			redirectAttributes.addFlashAttribute("errorMessages", messages);

			// 入力画面へリダイレクト。
			return "redirect:/account";
		}

		String loginId = requestAccount.getLoginId();

		// ユーザー検索を行う。（※「ログインID」で検索を行い、すでに登録済みの場合はエラー。）
		Users users = usersService.findUsers(loginId);

		if (users != null) {
			log.warn("すでに登録済みのユーザーです。: requestAccount={}", requestAccount);

			// エラーメッセージをセット。
			result.rejectValue("loginId", StringUtil.BLANK, "指定のログインIDは、すでに登録されています。");

			redirectAttributes.addFlashAttribute("validationErrors", result);
			redirectAttributes.addFlashAttribute("requestAccount", requestAccount);
			
			// エラーメッセージのリストを渡す
			List<String> messages = result.getAllErrors()
					.stream()
					.map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			redirectAttributes.addFlashAttribute("errorMessages", messages);
			
			// 入力画面へリダイレクト。
			return "redirect:/account";
		}

		// データ登録処理。
		usersService.save(requestAccount);

		// 不正アクセス防止のため、正常アクセスの時は"true"を入れる。
		redirectAttributes.addFlashAttribute("isSuccess", "true");

		// 完了画面へリダイレクト
		return "redirect:/account/complete";
	}

	/**
	 * [GET]完了アクション。
	 *
	 * @param requestAccount 入力フォームの内容
	 * @param isSuccess 正常の遷移であるか、否か。（true.正常、false.不正アクセス）
	 * @param result バリデーション結果
	 * @param redirectAttributes リダイレクト時に使用するオブジェクト
	 */
	@GetMapping("/complete")
	public String complete(@ModelAttribute("requestAccount") RequestAccount requestAccount,
			@ModelAttribute("isSuccess") String isSuccess,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		log.info("アカウント作成完了画面のアクションが呼ばれました。");

		// 不正アクセスはトップへリダイレクト。（直接URLを叩いての不正アクセスを制御。）
		if (!BooleanUtils.toBoolean(isSuccess)) {

			log.warn("", StringUtil.BLANK, "不正なアクセスがありました。");

			redirectAttributes.addFlashAttribute("validationErrors", result);

			// 入力画面へリダイレクト。
			return "redirect:/account";
		}

		log.info("リダイレクトパラメータ：requestAccount={}, isSuccess={}", requestAccount, isSuccess);
		return "account/complete";
	}
}
