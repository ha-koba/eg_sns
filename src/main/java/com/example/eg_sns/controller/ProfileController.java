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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import com.example.eg_sns.constants.AppConstants;
import com.example.eg_sns.core.annotation.LoginCheck;
import com.example.eg_sns.dto.RequestModifyAccount;
import com.example.eg_sns.entity.Friends;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.FriendsService;
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

	private static final String TARGET_LOGIN_ID = "targetLoginId";

	/** ファイルアップロード関連サービスクラス。 */
	@Autowired
	private StorageService storageService;

	/** ユーザー関連サービスクラス。 */
	@Autowired
	private UsersService usersService;

	/** ユーザー関連サービスクラス。 */
	@Autowired
	private FriendsService friendsService;

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

        Long loginUsersId = getUsersId();			// ログイン中のユーザーのID
        Long targetUsersId = targetUser.getId();	// 検索対象のユーザーのID
        log.info("loginUsersId: " + loginUsersId + ", targetUsersId: " + targetUsersId);

		// 自身のプロフィールページか
		boolean isMyProfile = false;

		// 呼ばれたページが自身のユーザーIDと一致するならtrue
		if (targetUsersId.equals(loginUsersId)) {
			log.warn("自分のログインIDのページが指定されました。");
			isMyProfile = true;
		} else {
			log.warn("他者のログインIDのページが指定されました。");
			log.warn("フレンド情報を検索します。");
			Friends friend = friendsService.findFriends(loginUsersId, targetUsersId);
			log.info("friend：{}", friend);

			// TODO: ▼▼▼承認ステータス判定の処理を切り出す▼▼▼
			// デフォルトはリジェクトとし、友達申請ボタンが表示されている状態。拒否したときも同様の表示である。
			approvalStatus as = approvalStatus.REJECTION;
			if (friend != null) {
				// 申請履歴がある

				switch (friend.getApprovalStatus()) {

				// 申請ボタン押下(自分) 申請済み画面
				case AppConstants.APPLYING:
					// 申請済みの表示
					as = approvalStatus.APPLYING;
					break;

				// 申請ボタン押下された(相手)
				case AppConstants.APPROVAL_PENDING:
					// 承認(申請中の表示) [承認 or 拒否の待機画面]
					as = approvalStatus.APPROVAL_PENDING;
					break;

				// 承認ボタン押下(相手)
				case AppConstants.APPROVAL, AppConstants.AGREEMENT:
					// 承諾(フレンドになった表示で登録解除できるボタンの表示)
					as = approvalStatus.AGREEMENT;
					break;

				// 拒否ボタン押下(相手)
				case AppConstants.REJECTION, AppConstants.DISMISSAL:
					// 拒否(申請ボタンを表示)(初期値と同じ)
					as = approvalStatus.REJECTION;
					break;
				}
			}
			// TODO: ▲▲▲ここまで切り出す▲▲▲

			log.info("approvalStatus：{}", as);
			model.addAttribute("approvalStatus", as); // 承認ステータス
		}

		model.addAttribute("isMyProfile", isMyProfile); // 自身のプロフィールではない画面
		model.addAttribute("requestModifyAccount", targetUser); // TODO: プロフィールは編集できないようにしたいため初期化

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

	//	@PostMapping("/apply")
	//	public String apply(@Validated @ModelAttribute RequestFriend requestFriend,
	//			BindingResult result,  // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
	//		    @RequestParam(FRIEND_USERS_ID) Long friendUsersId,
	//		    @RequestParam(ACTION2) String action,
	//			RedirectAttributes redirectAttributes) {
	//		log.info("フレンド登録・更新処理のアクションが呼ばれました。：requestFriend={}, result={}", requestFriend, result);
	//		log.info("usersId={}, friendUsersId={}", usersId, friendUsersId);
	//		return "";
	//	}
}
