package com.example.eg_sns.controller;

import org.apache.commons.lang3.BooleanUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.eg_sns.core.AppNotFoundException;
import com.example.eg_sns.dto.RequestTopic;
import com.example.eg_sns.dto.RequestTopicComment;
import com.example.eg_sns.entity.Topics;
import com.example.eg_sns.service.CommentsService;
import com.example.eg_sns.service.TopicsService;
import com.example.eg_sns.util.StringUtil;

import lombok.extern.log4j.Log4j2;

/**
 * ※TODO 適宜実装を入れてください。
 */
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

	@GetMapping(path = { "", "/" })
	public String index(Model model) {

		log.info("トピック作成入力画面のアクションが呼ばれました。");

		if (!model.containsAttribute("requestTopic")) {
			model.addAttribute("requestTopic", new RequestTopic());
		}
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
			RedirectAttributes redirectAttributes) {

		log.info("トピック作成処理のアクションが呼ばれました。：requestTopic={}", requestTopic);

		// バリデーション。
		if (result.hasErrors()) {
			log.warn("バリデーションエラーが発生しました。：requestTopic={}, result={}", requestTopic, result);

			redirectAttributes.addFlashAttribute("validationErrors", result);
			redirectAttributes.addFlashAttribute("requestTopic", requestTopic);

			// 入力画面へリダイレクト。
			return "redirect:/home";
		}

		// ログインユーザー情報取得
		Long usersId = getUsersId();

		// データ登録処理
		Topics topics = topicsService.save(requestTopic, usersId);

		redirectAttributes.addFlashAttribute("isSuccess", "true");
		log.info("トピックを登録しました。topics id={}, topics={}", topics.getId(), topics);
		return "redirect:/home/topic/detail/" + StringUtil.toString(topics.getId(), StringUtil.BLANK);
	}

	/**
	 * [GET]トピック詳細アクション。
	 *
	 * @param topicsId トピックID
	 * @param isSuccess コメント投稿完了からの正常の遷移であるか、否か。（true.正常）
	 * @param model 画面にデータを送るためのオブジェクト
	 */
	@GetMapping("/topic/detail/{topicsId}")
	public String detail(@PathVariable Long topicsId,
			@ModelAttribute("isSuccess") String isSuccess,
			Model model) {

		log.info("トピック詳細画面のアクションが呼ばれました。");

		if (!model.containsAttribute("requestTopicComment")) {
			model.addAttribute("requestTopicComment", new RequestTopicComment());
		}

		// トピック情報を取得する。
		Topics topics = topicsService.findTopics(topicsId);

		if (topics == null) {
			// トピックが取得できない場合は、Not Found。
			throw new AppNotFoundException();
		}

		model.addAttribute("topics", topics);
		model.addAttribute("isSuccess", BooleanUtils.toBoolean(isSuccess));

		return "home/index";
	}
}
