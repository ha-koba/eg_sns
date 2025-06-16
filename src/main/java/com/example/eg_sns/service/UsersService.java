package com.example.eg_sns.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.eg_sns.dto.RequestAccount;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * ユーザー関連サービスクラス。
 *
 * @author tomo-sato
 */
@Log4j2
@Service
@RequiredArgsConstructor // final修飾子が付いたフィールドを引数にとるコンストラクタを自動生成
public class UsersService {

	/** リポジトリインターフェース。 */
	private final UsersRepository repository; // Springにより自動でインスタンスが注入される

	/** パスワードのハッシュ化や照合を行うエンコーダ。 */
//	private final PasswordEncoder passwordEncoder; // Springにより自動でインスタンスが注入される

	/**
	 * ユーザー検索を行う。
	 * ログインIDを指定し、ユーザーを検索する。
	 *
	 * @param loginId ログインID
	 * @return ユーザー情報を返す。
	 */
	public Users findUsers(String loginId) {
		log.info("ユーザーを検索します。：loginId={}", loginId);

		Users users = repository.findByLoginId(loginId);
		log.info("ユーザー検索結果。：loginId={}, users={}", loginId, users);

		return users;
	}

	/**
	 * ユーザー検索を行う。
	 * ログインID、パスワードを指定し、ユーザーを検索する。
	 *
	 * @param loginId ログインID
	 * @param password パスワード
	 * @return ユーザー情報を返す。
	 */
	public Users findUsers(String loginId, String password) {
		log.info("ユーザーを検索します。：loginId={}, password={}", loginId, password);

		Users users = repository.findByLoginIdAndPassword(loginId, password);
		log.info("ユーザー検索結果。：loginId={}, password={}, users={}", loginId, password, users);

		return users;
	}

	/**
	 * 全ユーザーの検索を行う。
	 * ログインIDを指定し、ユーザーを検索する。
	 *
	 * @param loginId ログインID
	 * @return ユーザー情報を返す。
	 */
	public List<Users> findAllUsers() {
		log.info("全ユーザーを探索します。");

		List<Users> users = (List<Users>) repository.findAll();
		log.info("ユーザー検索結果。：users={}", users);

		return users;
	}

	/**
	 * ユーザー登録処理を行う。
	 *
	 * @param requestAccount ユーザーDTO
	 */
	public void save(RequestAccount requestAccount) {
		Users users = new Users();
		users.setLoginId(requestAccount.getLoginId());
		users.setPassword(requestAccount.getPassword());
		users.setName(requestAccount.getName());
		users.setEmailAddress(requestAccount.getEmailAddress());
		repository.save(users);
	}

	/**
	 * ユーザー登録処理を行う。
	 *
	 * @param requestAccount ユーザーDTO
	 */
	public void save(Users users) {
		repository.save(users);
	}

	/**
	 * 指定されたユーザーのパスワードを新しいパスワードに変更します。
	 * 
	 * このメソッドはトランザクション管理下で動作し、パスワードの更新処理が失敗した場合も安全にロールバックされます。
	 * パスワードはハッシュ化して保存されないため注意。
	 *
	 * @param loginId パスワードを変更するユーザーのログインID
	 * @param newPassword 新しいパスワード（ハッシュ化前の生パスワード）
	 * @throws RuntimeException 指定されたログインIDのユーザーが見つからない場合
	 */
	@Transactional
	public void updatePassword(String loginId, String newPassword) {
		// ユーザーをログインIDで検索
		Users users = findUsers(loginId);
		// 新しいパスワードを設定
		users.setPassword(newPassword);
		// 変更内容をデータベースに保存
		repository.save(users);
	}

	/**
	 * ユーザー名とパスワードが一致するかどうかを検証します。
	 * @param loginId ユーザー名
	 * @param rawPassword 検証対象の生パスワード
	 * @return パスワードが一致すればtrue、不一致またはユーザーが存在しなければfalse
	 */
	public boolean checkPassword(String loginId, String rawPassword) {
		Users users = findUsers(loginId);
		if (users == null) {
			return false; // ユーザーが存在しない場合
		}
		return rawPassword.equals(users.getPassword());
	}
}

