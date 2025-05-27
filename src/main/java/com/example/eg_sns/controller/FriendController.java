package com.example.eg_sns.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.eg_sns.entity.Users;
import com.example.eg_sns.service.UsersService;

@Controller
@RequestMapping("/friend")
public class FriendController {
	
	@Autowired
	private UsersService service;

	@GetMapping("/list")
	public String list(Model model) {
		// 全ユーザー情報の取得(プロフィール画像、名前、自己紹介、承認ステータス）
		List<Users> usersList = service.findAllUsers();
		model.addAttribute("usersList", usersList);
		return "friend/list";
	}
}