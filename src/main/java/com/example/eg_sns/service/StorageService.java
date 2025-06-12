package com.example.eg_sns.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * ファイルアップロード関連サービスクラス。
 *
 * @author tomo-sato
 */
@Log4j2
@Service
public class StorageService {

	public enum FileType {
		PROFILE_IMG("profileimg", "/assets/profileimg/"), TOPIC_IMG("topicimg", "/assets/topicimg/");

		private final String dirName;
		private final String webPath;

		FileType(String dirName, String webPath) {
			this.dirName = dirName;
			this.webPath = webPath;
		}
	}

	@Value("${upload.base.dir}")
	private String baseDir;

	/**
	 * ファイルアップロード処理
	 * 
	 * @param topicFile アップロードファイル
	 * @param fileType ファイル種別（プロフィール画像/トピック画像）
	 * @return Webアクセス用パス（例：/assets/topicimg/xxx.jpg）
	 */
	public String store(MultipartFile topicFile, FileType fileType) {
		if (topicFile.isEmpty()) {
			return null;
		}

		// ファイル名生成（UUID + 元ファイル名）
		String fileName = UUID.randomUUID() + "_" + topicFile.getOriginalFilename();

		try {
			// 保存先ディレクトリのパス作成
			Path storageDir = Paths.get(baseDir, fileType.dirName);
			Files.createDirectories(storageDir);

			// ファイル保存
			Path targetPath = storageDir.resolve(fileName);
			Files.copy(topicFile.getInputStream(), targetPath);

			return fileType.webPath + fileName;

		} catch (IOException e) {
			log.error("ファイルアップロードエラー: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * ファイルチェック処理。
	 * 画像ファイルであるか、否かのチェックを行う。
	 *
	 * @param multipartFile マルチパートで受信したファイル。
	 * @return true.画像ファイルである。false.画像ファイルではない。（または、処理中にエラーが発生した場合。）
	 */
	public static boolean isImageFile(MultipartFile multipartFile) {
		// ファイル名取得
		String fileName = multipartFile.getOriginalFilename();

		if (StringUtils.isEmpty(fileName)) {
			// 画像ファイル名称が取得できない場合は、デフォルト画像を設定するため「true」を返す。
			return true;
		}

		FileOutputStream fos = null;
		try {
			File file = new File("src/main/resources/static/assets/profileimg/" + fileName);
			// 拡張子を除いたファイル名を取得
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();

			if (file != null && file.isFile()) {
				BufferedImage bi = ImageIO.read(file);
				if (bi != null) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("ファイルチェック中にエラーが発生しました。", e);
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("ファイルチェック処理中のクローズ処理でエラーが発生しました。", e);
				}
			}
		}
	}

	// TODO: 画像のファイルアップロードサイズでエラーを出力する

	/**
	 * 画像のバイナリ情報を返す。
	 * ※アップロードではビルドパスに画像情報を配置している為、アップロード直後にリビルドがかからず画像がデプロイされない。
	 * 　その為リンク切れが発生していた。
	 * 　直接ファイルを指定して、バイナリ情報を画面に送ることで解消。
	 *
	 * @param imageUri 画像URI
	 * @return 画像のバイナリ情報。
	 */
	public static String getDataUri(String imageUri) {
		String base64EncodedImage = "";
		try {
			byte[] imageBytes = Files.readAllBytes(Paths.get("src/main/resources/static" + imageUri));

			// バイト配列をBase64エンコード
			base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "data:image/jpeg;base64," + base64EncodedImage;
	}
}