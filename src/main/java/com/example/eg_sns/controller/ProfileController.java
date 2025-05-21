package com.example.eg_sns.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.eg_sns.core.AppConst;
import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.UsersService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/profile")
public class ProfileController {
	
	/* セッション情報 */
	@Autowired
	private HttpSession session;
	
	/* ユーザー関連サービスクラス。 */
	@Autowired
	private UsersService usersService;
	
	/**
	 * [GET]プロファイル画面のアクション。
	 * 
	 * @param model 画面にデータを送るためのオブジェクト
	 */
	@GetMapping(path = {"", "/"})
	public String index(Model model) {
		
		log.info("プロファイル画面のアクションが呼ばれました。");
		
		// ログインしていない場合、ログイン画面へリダイレクト
		Users users = (Users) session.getAttribute(AppConst.SESSION_KEY_LOGIN_INFO);
		if (users == null) {
			
			// ログイン画面へリダイレクト。
			return "redirect:/login";
		}
		return "profile/index";
	}
}