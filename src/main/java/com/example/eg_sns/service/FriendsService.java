package com.example.eg_sns.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.eg_sns.constants.AppConstants;
import com.example.eg_sns.controller.ProfileController.approvalStatus;
import com.example.eg_sns.entity.Friends;
import com.example.eg_sns.repository.FriendsRepository;

import lombok.extern.log4j.Log4j2;

/**
 * フレンド関連サービスクラス。
 *
 * @author ha-koba
 */
@Log4j2
@Service
public class FriendsService {

	/** リポジトリインターフェース。 */
	@Autowired
	private FriendsRepository repository;

	/**
	 * フレンド情報検索を行う。
	 * ユーザーIDを指定し、フレンドを検索する。
	 *
	 * @param id ユーザーテーブルのID
	 * @return フレンド情報を返す。
	 */
	public Friends findFriends(Long id) {
		log.info("フレンド情報を検索します。：id={}", id);

		Friends friends = repository.findByUsersId(id);
		log.info("フレンド情報の検索結果。：friends={}", friends);

		return friends;
	}

	public Friends findFriends(Long usersId, Long friendUsersId) {
		log.info("フレンド情報を検索します。：usersId={}, friendUsersId={}", usersId, friendUsersId);

		Friends friends = repository.findByUsersIdAndFriendUsersId(usersId, friendUsersId);
		log.info("フレンド情報の検索結果。：friends={}", friends);

		return friends;
	}

	/**
	 * フレンド登録処理を行う。
	 *
	 * @param requestAccount フレンドDTO
	 */
	public void save(Friends friends) {
		log.info("フレンドを登録します。friends={}", friends);
		repository.save(friends);
	}
	
	/**
	 * 指定された2人のユーザー間の承認ステータスを返す。
	 * 
	 * @param usersId: ログイン中のユーザーID
	 * @param friendUsersId: ターゲットとなるフレンドのユーザーID
	 * @return フレンド承認ステータスを返す。
	 */
	public approvalStatus usersIds2ApprovalStatus(Long usersId, Long friendUsersId) {
		log.info("承認ステータス判定処理が呼ばれました。");
		log.warn("フレンド情報を検索します。usersId={}, friendUsersId={}",usersId, friendUsersId);
		Friends friend = findFriends(usersId, friendUsersId);
		log.info("検索結果。friend：{}", friend);
		approvalStatus as = approvalStatus.REJECTION;
		if (friend != null) {
			// 申請履歴がある
			log.info("friend.getApprovalstatus()={}", friend.getApprovalStatus());
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
		log.info("承認ステータスはこちらでした。approvalStatus：{}", as);
		return as;
	}
	
	/**
	 * 
	 * @param usersId: ユーザーID
	 * @param friendUsersId: フレンドID
	 * @param action: なんの処理をするか
	 * @param friends: 処理対象
	 * @param isLoginUsers: 自身のレコードか(loginしている人のユーザーIDのレコードならtrue)
	 * @return
	 */
	public Friends createOrUpdateFriends(Long usersId, Long friendUsersId, String action, Friends friends, boolean isLoginUsers) {
		// Create処理
		if (friends == null) {
			// もし存在しないなら新規作成
			log.info("Create処理が呼ばれました。フレンドが存在しないため、フレンドインスタンスを新しく作成します。");
			friends = new Friends();
			
			// ユーザー情報をセット。
			friends.setUsersId(usersId);
			friends.setFriendUsersId(friendUsersId);
		}
		
		// Update処理
		log.info("Update処理が呼ばれました。action={}, friends={}, usersId={}, friendUsersId={}", action, friends, usersId, friendUsersId);
		int ac = AppConstants.DISMISSAL;
		if ("apply".equals(action)) {
			// 1. 申請中[自分] | 2. 承認待ち[相手]
			log.info("申請処理が呼ばれました。");
			ac = (isLoginUsers) ? AppConstants.APPLYING : AppConstants.APPROVAL_PENDING;

		} else if ("approve".equals(action)) {
			// 3. 承認[自分] | 4. 承諾[相手]
			log.info("承認処理が呼ばれました。");
			ac = (isLoginUsers) ? AppConstants.APPROVAL : AppConstants.AGREEMENT;

		} else if ("reject".equals(action)) {
			// 5. 棄却[自分] | 6. 却下[相手]
			log.info("却下処理が呼ばれました。");
			ac = (isLoginUsers) ? AppConstants.REJECTION : AppConstants.DISMISSAL;

		} else {
			log.warn("予期しない値を受信しました。action={}", action);
			return null;
		}
		log.info("actionの判定結果 AppConstants ac={}", ac);
		friends.setApprovalStatus(ac);
		log.info("friendsにセットされたusersId={}, friendUsersId={}, ApprovalStatus={}", friends.getUsersId(), friends.getFriendUsersId(), ac);
		return friends;
	}
}
