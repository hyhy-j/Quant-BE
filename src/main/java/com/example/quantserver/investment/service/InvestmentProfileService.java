package com.example.quantserver.investment.service;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.investment.dto.InvestmentProfileRequest;
import com.example.quantserver.investment.dto.InvestmentProfileResponse;
import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.enums.ProfileType;
import com.example.quantserver.investment.repository.InvestmentProfileRepository;
import com.example.quantserver.user.entity.User;
import com.example.quantserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentProfileService {

    private final InvestmentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public InvestmentProfileResponse getCurrentProfile(Long userId) {
        InvestmentProfile profile = profileRepository.findByUserIdAndCurrentTrue(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        return InvestmentProfileResponse.from(profile);
    }

    @Transactional
    public InvestmentProfileResponse submitProfile(Long userId, InvestmentProfileRequest request) {
        if (profileRepository.existsByUserIdAndCurrentTrue(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }
        return save(userId, request);
    }

    @Transactional
    public InvestmentProfileResponse updateProfile(Long userId, InvestmentProfileRequest request) {
        InvestmentProfile current = profileRepository.findByUserIdAndCurrentTrue(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        current.deactivate();
        return save(userId, request);
    }

    private InvestmentProfileResponse save(Long userId, InvestmentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        InvestmentProfile profile = InvestmentProfile.builder()
                .user(user)
                .investmentGoal(request.investmentGoal())
                .riskTolerance((short) request.riskTolerance())
                .investmentPeriod(request.investmentPeriod())
                .investableAmount(request.investableAmount())
                .profileType(ProfileType.classify(request.riskTolerance()))
                .build();

        profileRepository.save(profile);
        return InvestmentProfileResponse.from(profile);
    }
}