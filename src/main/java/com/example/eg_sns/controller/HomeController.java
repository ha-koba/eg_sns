package com.example.eg_sns.controller;

import java.util.List;

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

import com.example.eg_sns.dto.RequestTopic;
import com.example.eg_sns.dto.RequestTopicComment;
import com.example.eg_sns.entity.TopicImages;
import com.example.eg_sns.entity.Topics;
import com.example.eg_sns.service.CommentsService;
import com.example.eg_sns.service.StorageService;
import com.example.eg_sns.service.StorageService.FileType;
import com.example.eg_sns.service.TopicImagesService;
import com.example.eg_sns.service.TopicsService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/home")
public class HomeController extends AppController {

	/** トピック関連サービスクラス。 */
	@Autowired
	private TopicsService topicsService;

	/** コメント関連サービスクラス。 */
	@Autowired
	private CommentsService commentsService;

	/** トピック関連サービスクラス。 */
	@Autowired
	private TopicImagesService topicImagesService;

	/** ファイルアップロード関連サービスクラス。 */
	@Autowired
	private StorageService storageService;

	@GetMapping(path = { "", "/" })
	public String index(Model model) {

		log.info("トピック作成入力画面のアクションが呼ばれました。");

		if (!model.containsAttribute("requestTopic")) {
			model.addAttribute("requestTopic", new RequestTopic());
		}

		log.info("トピック詳細画面のアクションが呼ばれました。");

		if (!model.containsAttribute("requestTopicComment")) {
			model.addAttribute("requestTopicComment", new RequestTopicComment());
		}

		List<Topics> allTopics = topicsService.findAllTopics();
		model.addAttribute("allTopics", allTopics);
		log.info("全てのトピック allTopics={}", allTopics);
		return "home/index";
	}

	/**
	 * トピック関連処理
	 */

	/**
	 * [POST]トピック作成アクション。
	 *
	 * @param requestTopic 入力フォームの内容
	 * @param result バリデーション結果
	 * @param redirectAttributes リダイレクト時に使用するオブジェクト
	 */
	@PostMapping("/topic/regist")
	public String regist(@Validated @ModelAttribute RequestTopic requestTopic,
			BindingResult result,
			@RequestParam(required = false) MultipartFile topicFile,
			RedirectAttributes redirectAttributes) {

		log.info("トピック作成処理のアクションが呼ばれました。：requestTopic={}", requestTopic);

		// バリデーション。
		if (result.hasErrors()) {
			log.warn("バリデーションエラーが発生しました。：requestTopic={}, result={}", requestTopic, result);

			redirectAttributes.addFlashAttribute("topicValidationErrors", result);
			redirectAttributes.addFlashAttribute("requestTopic", requestTopic);

			// 入力画面へリダイレクト。
			return "redirect:/home";
		}

		// ログインユーザー情報取得
		Long usersId = getUsersId();

		// 画像ファイルの保存処理（画像がある場合のみ）
		String fileUri = null;
		if (topicFile != null && !topicFile.isEmpty()) {
			if (!StorageService.isImageFile(topicFile)) {
				result.rejectValue("topicFileHidden", "error.invalidFile", "画像ファイルを指定してください。");

				redirectAttributes.addFlashAttribute("topicValidationErrors", result);
				redirectAttributes.addFlashAttribute("requestTopic", requestTopic);
				return "redirect:/home";
			}
			fileUri = storageService.store(topicFile, FileType.TOPIC_IMG);
		}

		// トピック本体の保存
		Topics topics = topicsService.save(requestTopic, usersId);
		log.info("トピックを登録しました。topics id={}, topics={}", topics.getId(), topics);

		// 画像がある場合のみトピックイメージDBへ保存
		if (fileUri != null && !fileUri.isEmpty()) {
			TopicImages topicImages = topicImagesService.save(topics.getId(), fileUri);
			log.info("トピックイメージを登録しました。topics id={}, topicImages={}", topics.getId(), topicImages);
		}

		redirectAttributes.addFlashAttribute("isSuccess", "true");
		return "redirect:/home";
	}

	/**
	 * [GET]トピック削除アクション。
	 *
	 * @param topicsId トピックID
	 */
	@GetMapping("/topic/delete/{topicsId}")
	public String delete(@PathVariable Long topicsId) {

		log.info("トピック削除処理のアクションが呼ばれました。：topicsId={}", topicsId);

		// ログインユーザー情報取得（※自分が投稿したコメント以外を削除しない為の制御。）
		Long usersId = getUsersId();

		// コメント削除処理
		topicsService.delete(topicsId, usersId);

		// ホーム画面へリダイレクト。
		return "redirect:/home";
	}

	/**
	 * [POST]コメント投稿アクション。
	 *
	 * @param topicsId トピックID
	 * @param requestTopicComment 入力フォームの内容
	 * @param result バリデーション結果
	 * @param redirectAttributes リダイレクト時に使用するオブジェクト
	 */
	@PostMapping("/topic/comment/regist/{topicsId}")
	public String commentRegist(@PathVariable Long topicsId,
			@Validated @ModelAttribute RequestTopicComment requestTopicComment,
			BindingResult result,
			RedirectAttributes redirectAttributes) {

		log.info("コメント投稿処理のアクションが呼ばれました。：topicsId={}, requestTopicComment={}", topicsId, requestTopicComment);

		// バリデーション。
		if (result.hasErrors()) {
			log.warn("バリデーションエラーが発生しました。：topicsId={}, requestTopicComment={}, result={}", topicsId, requestTopicComment,
					result);

			redirectAttributes.addFlashAttribute("commentValidationErrors", result);
			redirectAttributes.addFlashAttribute("requestTopicComment", requestTopicComment);

			// 入力画面へリダイレクト。
			return "redirect:/home";
		}

		// ログインユーザー情報取得
		Long usersId = getUsersId();

		// コメント登録処理
		commentsService.save(requestTopicComment, usersId, topicsId);

		return "redirect:/home";
	}

	/**
	 * [GET]コメント削除アクション。
	 *
	 * @param topicsId トピックID
	 * @param commentsId コメントID
	 */
	@GetMapping("/topic/comment/delete/{topicsId}/{commentsId}")
	public String commentDelete(@PathVariable Long topicsId, @PathVariable Long commentsId) {

		log.info("コメント削除処理のアクションが呼ばれました。：topicsId={}, commentsId={}", topicsId, commentsId);

		// ログインユーザー情報取得（※自分が投稿したコメント以外を削除しない為の制御。）
		Long usersId = getUsersId();

		// コメント削除処理
		commentsService.delete(commentsId, usersId, topicsId);

		// 入力画面へリダイレクト。
		return "redirect:/home";
	}
}
