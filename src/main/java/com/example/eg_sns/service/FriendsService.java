package com.example.eg_sns.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * @param usersId ユーザーID
	 * @return フレンド情報を返す。
	 */
	public Friends findFriends(String usersId) {
		log.info("フレンド情報を検索します。：usersId={}", usersId);
		
		Friends friends = repository.findByUsersId(usersId);
		log.info("フレンド情報の検索結果。：usersId={}, friends={}", usersId, friends);
		
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
}
