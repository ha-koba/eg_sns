package com.example.eg_sns.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import com.example.eg_sns.core.annotation.LoginCheck;
import com.example.eg_sns.dto.RequestModifyAccount;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.StorageService;
import com.example.eg_sns.service.UsersService;
import com.example.eg_sns.util.StringUtil;

import lombok.extern.log4j.Log4j2;

/**
 * プロフィールコントローラー。
 *
 * @author tomo-sato
 */
@LoginCheck
@Log4j2
@Controller
@RequestMapping("/profile")
public class ProfileController extends AppController {
	/** ファイルアップロード関連サービスクラス。 */
	@Autowired
	private StorageService storageService;

	/** ユーザー関連サービスクラス。 */
	@Autowired
	private UsersService usersService;

	/**
	 * [GET]プロフィール画面のアクション。
	 * 
	 * @param model 入力フォームのオブジェクト
	 */
	@GetMapping(path = { "", "/" })
	public String index(Model model) {

		log.info("プロフィール画面のアクションが呼ばれました。");

		model.addAttribute("requestModifyAccount", getUsers());

		return "profile/index";
	}

	/**
	 * [POST]アカウント編集アクション。
	 *
	 * @param requestModifyAccount 入力フォームの内容
	 * @param profileFile プロフィール画像ファイル
	 * @param result バリデーション結果
	 * @param redirectAttributes リダイレクト時に使用するオブジェクト
	 */
	@PostMapping("/regist")
	public String regist(@Validated @ModelAttribute RequestModifyAccount requestModifyAccount,
			BindingResult result,  // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
			@RequestParam MultipartFile profileFile,
			RedirectAttributes redirectAttributes) {
		log.info("プロフィール編集処理のアクションが呼ばれました。");

		// バリデーション。
		if (result.hasErrors()) {
			// javascriptのバリデーションを改ざんしてリクエストした場合に通る処理。
			log.warn("バリデーションエラーが発生しました。：requestModifyAccount={}, result={}", requestModifyAccount, result);

			redirectAttributes.addFlashAttribute("validationErrors", result);

			// エラーメッセージのリストを渡す
			List<String> messages = result.getAllErrors()
					.stream()
					.map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			redirectAttributes.addFlashAttribute("errorMessages", messages);

			// 入力画面へリダイレクト。
			// TODO: プロフィール/プロフィール編集画面にリダイレクト。
			return "redirect:/profile";
		}

		// ファイルチェックを行う。
		if (!StorageService.isImageFile(profileFile)) {
			log.warn("指定されたファイルは、画像ファイルではありません。：requestModifyAccount={}", requestModifyAccount);

			// エラーメッセージをセット。
			result.rejectValue("profileFileHidden", StringUtil.BLANK, "画像ファイルを指定してください。");

			redirectAttributes.addFlashAttribute("validationErrors", result);

			// エラーメッセージのリストを渡す
			List<String> messages = result.getAllErrors()
					.stream()
					.map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			redirectAttributes.addFlashAttribute("errorMessages", messages);

			// 入力画面へリダイレクト。
			// TODO: プロフィール/プロフィール編集画面にリダイレクト。
			return "redirect:/profile";
		}

		// ユーザー検索を行う。
		Users users = getUsers();
		// ファイルアップロード処理。
		String fileUri = storageService.store(profileFile);

		// fileUriが取得できない且つ、hiddenの値にファイルが設定されている場合は「設定済みのファイルが変更されていない状態」である為、hiddenの値で更新する。
		if (StringUtils.isEmpty(fileUri) && !StringUtils.isEmpty(requestModifyAccount.getProfileFileHidden())) {
			fileUri = requestModifyAccount.getProfileFileHidden();
			// DBから取得したデータと比較し、改ざんされた値ではないことの確認。
			if (!fileUri.equals(users.getIconUri())) {
				// 改ざんの可能性がある場合はnullをセットしファイルをクリアする。
				fileUri = null;
			}
		}

		users.setName(requestModifyAccount.getName());
		users.setEmailAddress(requestModifyAccount.getEmailAddress());
		users.setAbout(requestModifyAccount.getAbout());
		users.setIconUri(fileUri);
		usersService.save(users);
		return "redirect:/profile";
	}
}
