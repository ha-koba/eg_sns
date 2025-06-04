package com.example.eg_sns.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.eg_sns.entity.Friends;

/**
 * フレンド関連リポジトリインターフェース。
 *
 * @author ha-koba
 */
public interface FriendsRepository extends PagingAndSortingRepository<Friends, Long>, CrudRepository<Friends, Long> {

	/**
	 * フレンド情報の検索を行う。
	 * ログインIDを指定し、フレンド情報を検索する。
	 *
	 * @param usersId ユーザーテーブルのID
	 * @return フレンド情報を返す。
	 */
	Friends findByUsersId(Long usersId);

	/**
	 * フレンド情報の検索を行う。
	 * ログインIDとフレンドIDを指定し、フレンド情報を検索する。
	 * 
	 * @param usersId
	 * @param friendUsersId
	 * @return
	 */
	Friends findByUsersIdAndFriendUsersId(Long usersId, Long friendUsersId);
}
