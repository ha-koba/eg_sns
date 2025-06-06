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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.eg_sns.constants.UsersAndApprovalStatus;
import com.example.eg_sns.dto.RequestFriend;
import com.example.eg_sns.entity.Friends;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.FriendsService;
import com.example.eg_sns.service.UsersService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/friend")
public class FriendController extends AppController {

	private static final String USERS_ID = "usersId";
	private static final String FRIEND_USERS_ID = "friendUsersId";
	private static final String ACTION2 = "action";

	@Autowired
	private UsersService usersService;

	@Autowired
	private FriendsService friendsService;

	@GetMapping("/list")
	public String list(Model model) {
		// 全ユーザー情報の取得(プロフィール画像、名前、自己紹介、承認ステータス）
		List<Users> usersList = usersService.findAllUsers();
		Users loginUsers = getUsers();
		Long loginUsersId = loginUsers.getId();

		model.addAttribute("loginUsers", loginUsers);
		model.addAttribute("usersList", usersList); // TODO: 後で削除
		
		// フレンドユーザーとその承認ステータスをセットで保存するリスト
		List<UsersAndApprovalStatus> usersAndStatusList = friendsService.getUsersAndApprovalStatus(usersList, loginUsersId);
		
		// ログインユーザーを取り除く処理
		List<UsersAndApprovalStatus> filteredUsers = usersAndStatusList.stream()
			    .filter(user -> !user.getUsers().getId().equals(loginUsersId))
			    .collect(Collectors.toList());
		model.addAttribute("usersAndStatusList", filteredUsers);
		return "friend/list";
	}

	@PostMapping("/process")
	public String process(@Validated @ModelAttribute RequestFriend requestFriend,
			BindingResult result, // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
			@RequestParam(USERS_ID) Long usersId,
			@RequestParam(FRIEND_USERS_ID) Long friendUsersId,
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
		
		// 申請 or 承認 or 却下 / 自分 or 他人。
		uFriends = friendsService.createOrUpdateFriends(usersId, friendUsersId, action, uFriends, true);
		fFriends = friendsService.createOrUpdateFriends(friendUsersId, usersId, action, fFriends, false);

		// データ登録処理。
		friendsService.save(uFriends);
		friendsService.save(fFriends);

		// 不正アクセス防止のため、正常アクセスの時は"true"を入れる。
		redirectAttributes.addFlashAttribute("isSuccess", "true");
		return "redirect:/friend/list";
	}
}