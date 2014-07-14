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
package org.meruvian.yama.service.jpa.security.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.meruvian.yama.repository.application.OauthApplicationApproval.ApprovalStatus;
import org.meruvian.yama.repository.jpa.application.JpaOauthApplicationApproval;
import org.meruvian.yama.repository.jpa.application.JpaOauthApplicationApprovalRepository;
import org.meruvian.yama.repository.jpa.user.JpaUserRepository;
import org.meruvian.yama.service.security.oauth2.OauthApplicationApprovalManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Dian Aditya
 *
 */
@Service
@Transactional(readOnly = true)
public class JpaOauthApplicationApprovalManager implements OauthApplicationApprovalManager {
	private JpaOauthApplicationApprovalRepository approvalRepository;
	private JpaUserRepository userRepository;
	
	@Inject
	public void setApprovalRepository(JpaOauthApplicationApprovalRepository approvalRepository) {
		this.approvalRepository = approvalRepository;
	}

	@Inject
	public void setUserRepository(JpaUserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	@Transactional
	public boolean addApprovals(Collection<Approval> approvals) {
		for (Approval approval : approvals) {
			JpaOauthApplicationApproval a = getApprovalByUserAndClientAndScope(approval.getUserId(), approval.getClientId(), approval.getScope());
			if (a != null) {
				a.setStatus(approval.getStatus() == null ? ApprovalStatus.APPROVED : ApprovalStatus.valueOf(approval.getStatus().name()));
				a.setExpiresAt(approval.getExpiresAt());
			} else {
				a = new JpaOauthApplicationApproval();
				a.setClientId(approval.getClientId());
				a.setExpiresAt(approval.getExpiresAt());
				a.setScope(approval.getScope());
				a.setStatus(approval.getStatus() == null ? ApprovalStatus.APPROVED : ApprovalStatus.valueOf(approval.getStatus().name()));
			}
			
			approvalRepository.save(a);
		}
		
		return true;
	}

	@Override
	public boolean revokeApprovals(Collection<Approval> approvals) {
		for (Approval approval : approvals) {
			JpaOauthApplicationApproval a = getApprovalByUserAndClientAndScope(approval.getUserId(), approval.getClientId(), approval.getScope());
			if (a != null) {
				approvalRepository.delete(a);
			}
		}
		
		return true;
	}

	@Override
	public Collection<Approval> getApprovals(String userId, String clientId) {
		List<Approval> approvals = new ArrayList<Approval>();
		userId = userRepository.findByUsername(userId).getId();
		
		for (JpaOauthApplicationApproval a : approvalRepository
				.findByLogInformationCreateByAndClientId(userId, clientId, null).getContent()) {
			userId = a.getLogInformation().getCreateBy();
			clientId = a.getClientId();
			String scope = a.getScope();
			Date expiresAt = a.getExpiresAt();
			org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus status = 
					org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus.valueOf(a.getStatus().name());
			Date lastUpdatedAt = a.getLogInformation().getLastUpdateDate();
			
			Approval approval = new Approval(userId, clientId, scope, expiresAt, status, lastUpdatedAt);
			approvals.add(approval);
		}
		
		return approvals;
	}

	@Override
	public JpaOauthApplicationApproval getApprovalById(String id) {
		return approvalRepository.findOne(id);
	}

	@Override
	public JpaOauthApplicationApproval getApprovalByUserAndClientAndScope(String userId, String clientId, String scope) {
		return approvalRepository.findByLogInformationCreateByAndClientIdAndScope(userId, clientId, scope);
	}

	@Override
	public Page<JpaOauthApplicationApproval> findApprovalByUserAndClient(String userId, String clientId, Pageable pageable) {
		return approvalRepository.findByLogInformationCreateByAndClientId(userId, clientId, pageable);
	}

	@Override
	public Page<JpaOauthApplicationApproval> findApprovalByClient(String clientId, Pageable pageable) {
		return approvalRepository.findByClientId(clientId, pageable);
	}

	@Override
	public Page<JpaOauthApplicationApproval> findApprovalByUser(String userId, Pageable pageable) {
		return approvalRepository.findByLogInformationCreateBy(userId, pageable);
	}
}
