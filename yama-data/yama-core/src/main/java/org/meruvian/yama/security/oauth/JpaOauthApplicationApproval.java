/**
 * Copyright 2014 Meruvian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meruvian.yama.security.oauth;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.meruvian.yama.core.DefaultJpaPersistence;
import org.meruvian.yama.core.JpaLogInformation;
import org.meruvian.yama.core.application.JpaApplication;

/**
 * @author Dian Aditya
 *
 */
@Entity
@Table(name = "yama_oauth_application_approval", uniqueConstraints = { @UniqueConstraint(columnNames = {"application_id", "create_by", "scope"}) })
public class JpaOauthApplicationApproval extends DefaultJpaPersistence implements OauthApplicationApproval {
	private JpaApplication application;
	private String scope;
	private ApprovalStatus status = ApprovalStatus.APPROVED;
	private Date expiresAt;

	@Override
	public void update(OauthApplicationApproval approval) {
		this.application = new JpaApplication(approval.getApplication());
		this.scope = approval.getScope();
		this.status = approval.getStatus();
		this.expiresAt = approval.getExpiresAt();
		this.logInformation = new JpaLogInformation(approval.getLogInformation());
	}

	@Override
	@ManyToOne
	@JoinColumn(name = "application_id")
	public JpaApplication getApplication() {
		return application;
	}
	
	public void setApplication(JpaApplication application) {
		this.application = application;
	}

	@Override
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public ApprovalStatus getStatus() {
		return status;
	}
	
	public void setStatus(ApprovalStatus status) {
		this.status = status;
	}

	@Override
	@Column(name = "expires_at")
	public Date getExpiresAt() {
		return expiresAt;
	}
	
	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
