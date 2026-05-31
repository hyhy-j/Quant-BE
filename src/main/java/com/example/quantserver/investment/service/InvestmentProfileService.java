package com.example.quantserver.investment.service;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.investment.dto.InvestmentProfileRequest;
import com.example.quantserver.investment.dto.InvestmentProfileResponse;
import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.enums.InvestmentPeriod;
import com.example.quantserver.investment.enums.ProfileType;
import com.example.quantserver.investment.repository.InvestmentProfileRepository;
import com.example.quantserver.user.entity.User;
import com.example.quantserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
        try {
            return save(userId, request);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }
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

        ProfileType profileType = classifyProfileType(
                request.riskTolerance(),
                request.investmentPeriod(),
                request.investableAmount()
        );

        InvestmentProfile profile = InvestmentProfile.builder()
                .user(user)
                .investmentGoal(request.investmentGoal())
                .riskTolerance((short) request.riskTolerance())
                .investmentPeriod(request.investmentPeriod())
                .investableAmount(request.investableAmount())
                .profileType(profileType)
                .build();

        profileRepository.save(profile);
        return InvestmentProfileResponse.from(profile);
    }

    private ProfileType classifyProfileType(int riskTolerance, InvestmentPeriod investmentPeriod, BigDecimal investableAmount) {
        int score = riskScore(riskTolerance) + periodScore(investmentPeriod) + amountScore(investableAmount);

        if (score <= 33) return ProfileType.STABLE;
        if (score <= 66) return ProfileType.NEUTRAL;
        return ProfileType.AGGRESSIVE;
    }

    private int riskScore(int riskTolerance) {
        return switch (riskTolerance) {
            case 1 -> 0;
            case 2 -> 12;
            case 3 -> 25;
            case 4 -> 37;
            default -> 50;
        };
    }

    private int periodScore(InvestmentPeriod investmentPeriod) {
        return switch (investmentPeriod) {
            case UNDER_1Y -> 0;
            case ONE_TO_3Y -> 10;
            case THREE_TO_5Y -> 20;
            case OVER_5Y -> 30;
        };
    }

    private int amountScore(BigDecimal investableAmount) {
        long amount = investableAmount.longValue();
        if (amount < 1_000_000) return 0;
        if (amount < 5_000_000) return 5;
        if (amount < 15_000_000) return 10;
        if (amount < 30_000_000) return 15;
        return 20;
    }
}