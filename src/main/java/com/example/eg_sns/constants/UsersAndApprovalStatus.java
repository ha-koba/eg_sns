package com.example.eg_sns.constants;

import com.example.eg_sns.controller.ProfileController.approvalStatus;
import com.example.eg_sns.entity.Users;

public class UsersAndApprovalStatus {
	private Users users;
	private approvalStatus approvalStatus;

	public UsersAndApprovalStatus(Users users, approvalStatus approvalStatus) {
		super();
		this.users = users;
		this.approvalStatus = approvalStatus;
	}

	public UsersAndApprovalStatus() {}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public approvalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(approvalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
}
