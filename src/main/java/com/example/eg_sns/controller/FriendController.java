package com.example.eg_sns.controller;

import java.util.List;

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
	private static final int APPLYING = 1;			// 1. 申請中[自分]
	private static final int APPROVAL_PENDING = 2;	// 2. 承認待ち[相手]
	private static final int APPROVAL = 3;			// 3. 承認[自分]
	private static final int AGREEMENT = 4;			// 4. 承諾[相手]
	private static final int REJECTION = 5;			// 5. 却下[相手]
	private static final int DISMISSAL = 6;			// 6. 棄却[自分]
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private FriendsService friendsService;
	
	@GetMapping("/list")
	public String list(Model model) {
		// 全ユーザー情報の取得(プロフィール画像、名前、自己紹介、承認ステータス）
		List<Users> usersList = usersService.findAllUsers();
		model.addAttribute("loginUsers", getUsers());
		model.addAttribute("usersList", usersList);
		return "friend/list";
	}
	
	@PostMapping("/process")
	public String process(@Validated @ModelAttribute RequestFriend requestFriend,
			BindingResult result,  // BindingResultがバリデーション対象の直後にないとバリデーション結果として認識されない。
		    @RequestParam(USERS_ID) String usersId,
		    @RequestParam(FRIEND_USERS_ID) String friendUsersId,
		    @RequestParam(ACTION2) String action,
			RedirectAttributes redirectAttributes) {
		log.info("フレンド登録・更新処理のアクションが呼ばれました。：requestFriend={}, result={}", requestFriend, result);

		// バリデーション。
		if (result.hasErrors()) {
			// TODO: 一旦ログの表示のみ
			// ボタンを押した時にそのユーザーが削除されているケースなどが考えられる
			log.warn("バリデーションエラーが発生しました。 requestFriend={}, result={}", requestFriend, result);
			return "redirect:/friend/list";
		}
		
		// フレンドDBのユーザーIDカラムに、ログイン中のユーザーIDが存在するか確認。
		Friends uFriends = friendsService.findFriends(usersId);
		Friends fFriends = friendsService.findFriends(friendUsersId);
		
		if (uFriends == null) {
			// TODO: 新規作成せず、エラーのみにする。
			// もし存在しないなら例外エラーを出す。
			log.warn("uFriendsは更新するフレンドレコードが存在しないです。");
			log.info("フレンドレコードを新しく作成します。");
			uFriends = new Friends();
		}
		
		if (fFriends == null) {
			// TODO: 新規作成せず、エラーのみにする。
			// もし存在しないなら例外エラーを出す。
			log.warn("fFriendsは更新するフレンドレコードが存在しないです。");
			log.info("フレンドレコードを新しく作成します。");
			fFriends = new Friends();
		}
		
		// ユーザー情報(自分)をセット。
		uFriends.setUsersId(usersId);
		uFriends.setFriendUsersId(friendUsersId);
		
		// ユーザー情報(相手)をセット。
		fFriends.setUsersId(friendUsersId);
		fFriends.setFriendUsersId(usersId);
		
		// 承認 or 却下。
	    if ("approve".equals(action)) {
	        // 承認処理
	    	log.info("承認処理が呼ばれました。", action, uFriends, fFriends, requestFriend, result, usersId, friendUsersId);
	    	uFriends.setApprovalStatus(APPROVAL); // 3. 承認[自分]
	    	fFriends.setApprovalStatus(AGREEMENT); // 4. 承諾[相手]
	    } else if ("reject".equals(action)) {
	        // 却下処理
	    	log.info("却下処理が呼ばれました。", action, uFriends, fFriends, requestFriend, result, usersId, friendUsersId);
	    	uFriends.setApprovalStatus(REJECTION); // 5. 棄却[自分]
	    	fFriends.setApprovalStatus(DISMISSAL); // 6. 却下[相手]
	    } else {
	    	log.warn("予期しない値を受信しました。action={}", action);
	    }

	    // データ登録処理。
	    friendsService.save(uFriends);
	    friendsService.save(fFriends);

		// 不正アクセス防止のため、正常アクセスの時は"true"を入れる。
		redirectAttributes.addFlashAttribute("isSuccess", "true");
		return "redirect:/friend/list";
	}
}