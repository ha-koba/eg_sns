package com.example.eg_sns.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import com.example.eg_sns.core.annotation.LoginCheck;
import com.example.eg_sns.dto.RequestFriend;
import com.example.eg_sns.dto.RequestModifyAccount;
import com.example.eg_sns.dto.RequestPasswordChangeForm;
import com.example.eg_sns.entity.Friends;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.FriendsService;
import com.example.eg_sns.service.StorageService;
import com.example.eg_sns.service.StorageService.FileType;
import com.example.eg_sns.service.UsersService;
import com.example.eg_sns.util.StringUtil;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProfileController extends AppController {

	private static final String TARGET_LOGIN_ID = "targetLoginId";
	private static final String USERS_ID = "usersId";
	private static final String FRIEND_USERS_ID = "friendUsersId";
	private static final String FRIEND_USERS_LOGIN_ID = "friendUsersLoginId";
	private static final String ACTION2 = "action";

	/** ファイルアップロード関連サービスクラス。 */
	private final StorageService storageService;

	/** ユーザー関連サービスクラス。 */
	private final UsersService usersService;

	/** フレンド関連サービスクラス。 */
	private final FriendsService friendsService;

	public enum approvalStatus {
		APPLYING, // 申請済み
		APPROVAL_PENDING, // 承諾待ち[承認 or 拒否]のボタン表示
		AGREEMENT, // 承認、承諾
		REJECTION, // 拒否、申請前
		;
	}

	/**
	 * [GET]プロフィール画面のアクション。
	 * 
	 * @param model 入力フォームのオブジェクト
	 */
	@GetMapping(path = { "", "/" })
	public String index(Model model) {

		log.info("プロフィール画面のアクションが呼ばれました。");
		model.addAttribute("isMyProfile", true);
		model.addAttribute("requestModifyAccount", getUsers());
		model.addAttribute("requestPasswordChangeForm", new RequestPasswordChangeForm());

		return "profile/index";
	}

	/**
	 * [GET]ユーザー毎のプロフィール画面のアクション。
	 * 
	 * @param model 入力フォームのオブジェクト
	 */
	@GetMapping("/{targetLoginId}")
	public String userProfile(@PathVariable(TARGET_LOGIN_ID) String targetLoginId, Model model) {
		// ユーザーテーブルのログインIDから、IDを取得する
		// ユーザー情報をDBから取得。
		Users targetUser = usersService.findUsers(targetLoginId);

		if (targetUser == null) {
			log.error("ユーザーが存在しません。users={}", targetUser);
			return "error/404"; // ユーザーが存在しない場合
		}
		log.info("targetUser：{}", targetUser);

		Long loginUsersId = getUsersId(); // ログイン中のユーザーのID
		Long targetUsersId = targetUser.getId(); // 検索対象のユーザーのID
		log.info("loginUsersId: " + loginUsersId + ", targetUsersId: " + targetUsersId);

		// 自身のプロフィールページか
		boolean isMyProfile = false;
		// 承認ステータス
		approvalStatus as = null;

		// 呼ばれたページが自身のユーザーIDと一致するならtrue
		if (targetUsersId.equals(loginUsersId)) {
			log.warn("自分のログインIDのページが指定されました。");
			isMyProfile = true;
		} else {
			log.info("他者のログインIDのページが指定されました。");
			log.info("承認ステータス判定処理を呼びます。");
			as = friendsService.usersIds2ApprovalStatus(loginUsersId, targetUsersId);
		}

		log.info("承認ステータスを格納します。approvalStatus：{}", as);
		model.addAttribute("approvalStatus", as); // 承認ステータス
		model.addAttribute("isMyProfile", isMyProfile); // 自身のプロフィールではない画面
		model.addAttribute("requestModifyAccount", targetUser); // TODO: プロフィールは編集できないようにしたいため初期化
		model.addAttribute("loginUsers", getUsers());

		return "profile/index";
	}

	/**
	 * [POST]ユーザー毎のプロフィール画面のアクション。
	 * 
	 * @param model 入力フォームのオブジェクト
	 */
	@PostMapping("/process")
	public String process(@Validated @ModelAttribute RequestFriend requestFriend,
			BindingResult result, // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
			@RequestParam(USERS_ID) Long usersId,
			@RequestParam(FRIEND_USERS_ID) Long friendUsersId,
			@RequestParam(FRIEND_USERS_LOGIN_ID) String friendUsersLoginId,
			@RequestParam(ACTION2) String action,
			RedirectAttributes redirectAttributes) {
		log.info("フレンド登録・更新処理のアクションが呼ばれました。：requestFriend={}, result={}", requestFriend, result);
		log.info("usersId={}, friendUsersId={}", usersId, friendUsersId);

		// バリデーション。
		if (result.hasErrors()) {
			// TODO: 一旦ログの表示のみ
			// ボタンを押した時にそのユーザーが削除されているケースなどが考えられる
			log.warn("バリデーションエラーが発生しました。 requestFriend={}, result={}", requestFriend, result);
			return "redirect:/friend/list";
		}

		// フレンドDBのユーザーIDカラムに、ログイン中のユーザーIDが存在するか確認。
		Friends uFriends = friendsService.findFriends(usersId, friendUsersId);
		Friends fFriends = friendsService.findFriends(friendUsersId, usersId);

		// 承認 or 却下。
		uFriends = friendsService.createOrUpdateFriends(usersId, friendUsersId, action, uFriends, true);
		fFriends = friendsService.createOrUpdateFriends(friendUsersId, usersId, action, fFriends, false);

		// データ登録処理。
		friendsService.save(uFriends);
		friendsService.save(fFriends);

		// 不正アクセス防止のため、正常アクセスの時は"true"を入れる。
		redirectAttributes.addFlashAttribute("isSuccess", "true");
		redirectAttributes.addAttribute("friendUsersLoginId", friendUsersLoginId);
		return "redirect:/profile/{friendUsersLoginId}";
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
			BindingResult result, // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
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
		String fileUri = storageService.store(profileFile, FileType.PROFILE_IMG);

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

	/**
	 * パスワード変更処理
	 * @param form パスワード変更フォームの入力値
	 * @param redirectAttributes リダイレクト先にメッセージを渡すための属性
	 * @return リダイレクト先
	 */
	@PostMapping("/password/change")
	public String changePassword(@Validated @ModelAttribute RequestPasswordChangeForm requestPasswordChangeForm,
			BindingResult result, // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
			@ModelAttribute RequestPasswordChangeForm form,
			RedirectAttributes redirectAttributes) {

		log.info("パスワード変更処理を開始します。form={}", form);

		// バリデーション。
		if (result.hasErrors()) {
			// javascriptのバリデーションを改ざんしてリクエストした場合に通る処理。
			log.warn("バリデーションエラーが発生しました。：requestPasswordChangeForm={}, result={}", requestPasswordChangeForm, result);

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

		// 1. 新しいパスワードと確認用パスワードの一致チェック
		if (!form.getNewPassword().equals(form.getConfirmPassword())) {
			log.info("新しいパスワードが一致しません。form.getNewPassword()={}, form.getConfirmPassword()", form.getNewPassword(),
					form.getConfirmPassword());
			redirectAttributes.addFlashAttribute("errMsg", "新しいパスワードが一致しません");
			return "redirect:/profile";
		}

		// 2. 現在ログイン中のユーザー名を取得
		Users user = getUsers();
		String loginId = user.getLoginId();
		log.info("ログイン中のログインIDを取得します。user={}, loginId={}", user, loginId);

		// 3. 現在のパスワードが正しいかチェック
		if (!usersService.checkPassword(loginId, form.getCurrentPassword())) {
			redirectAttributes.addFlashAttribute("errMsg", "現在のパスワードが違います");
			return "redirect:/profile";
		}

		// 4. 新しいパスワードを更新
		usersService.updatePassword(loginId, form.getNewPassword());

		// 5. 成功メッセージを表示
		redirectAttributes.addFlashAttribute("infoMsg", "パスワードを変更しました");
		redirectAttributes.addFlashAttribute("isSuccess", true);
		return "redirect:/profile";
	}
}
