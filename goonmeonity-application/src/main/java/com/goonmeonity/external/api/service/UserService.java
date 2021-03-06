package com.goonmeonity.external.api.service;

import com.goonmeonity.domain.entity.user.User;
import com.goonmeonity.domain.entity.user.UserDischargeInfo;
import com.goonmeonity.domain.entity.user.UserInstallmentSavings;
import com.goonmeonity.domain.repository.user.UserDischargeInfoRepository;
import com.goonmeonity.domain.repository.user.UserInstallmentSavingsRepository;
import com.goonmeonity.domain.repository.user.UserRepository;
import com.goonmeonity.domain.service.user.dto.InstallmentSavingsIdAndUserId;
import com.goonmeonity.domain.service.user.dto.SimpleInstallmentSavings;
import com.goonmeonity.domain.service.user.error.UserDoesNotHaveDischargeInfoError;
import com.goonmeonity.domain.service.user.function.*;
import com.goonmeonity.external.api.request.user.CreateDischargeRequest;
import com.goonmeonity.external.api.request.user.SaveInstallmentSavingsRequest;
import com.goonmeonity.external.api.response.user.DeleteInstallmentSavingsResponse;
import com.goonmeonity.external.api.response.user.DischargeInfoResponse;
import com.goonmeonity.external.api.response.user.InstallmentSavingsResponse;
import com.goonmeonity.external.api.response.user.InstallmentSavingsListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserDischargeInfoRepository userDischargeInfoRepository;
    private final UserInstallmentSavingsRepository userInstallmentSavingsRepository;

    private final SaveUserDischargeInfoFunction saveUserDischargeInfoFunction;
    private final FindUserDischargeInfoByUserIdFunction findUserDischargeInfoByUserIdFunction;
    private final SaveUserInstallmentSavingsFunction saveUserInstallmentSavingsFunction;
    private final FindUserInstallmentSavingsByUserIdFunction findUserInstallmentSavingsByUserIdFunction;
    private final FindUserInstallmentSavingsByIdAndUserIdFunction findUserInstallmentSavingsByIdAndUserIdFunction;
    private final DeleteInstallmentSavingsByIdAndUserIdFunction deleteInstallmentSavingsByIdAndUserIdFunction;

    public UserService(
            UserRepository userRepository,
            UserDischargeInfoRepository userDischargeInfoRepository,
            UserInstallmentSavingsRepository userInstallmentSavingsRepository
    ) {
        this.userRepository = userRepository;
        this.userDischargeInfoRepository = userDischargeInfoRepository;
        this.userInstallmentSavingsRepository = userInstallmentSavingsRepository;

        this.saveUserDischargeInfoFunction = new SaveUserDischargeInfoFunction(userDischargeInfoRepository);
        this.findUserDischargeInfoByUserIdFunction = new FindUserDischargeInfoByUserIdFunction(userDischargeInfoRepository);
        this.saveUserInstallmentSavingsFunction = new SaveUserInstallmentSavingsFunction(userInstallmentSavingsRepository);
        this.findUserInstallmentSavingsByUserIdFunction = new FindUserInstallmentSavingsByUserIdFunction(userInstallmentSavingsRepository);
        this.findUserInstallmentSavingsByIdAndUserIdFunction = new FindUserInstallmentSavingsByIdAndUserIdFunction(userInstallmentSavingsRepository);
        this.deleteInstallmentSavingsByIdAndUserIdFunction = new DeleteInstallmentSavingsByIdAndUserIdFunction(userInstallmentSavingsRepository);
    }

    public DischargeInfoResponse registerDischargeInfo(CreateDischargeRequest createDischargeRequest, User user){
        UserDischargeInfo dischargeInfo;

        try {
            dischargeInfo = findUserDischargeInfoByUserIdFunction.apply(user.getId());
            dischargeInfo.setDischargeDate(createDischargeRequest.getDischargeDate());
            dischargeInfo.setEnlistmentDate(createDischargeRequest.getEnlistmentDate());
            dischargeInfo.setMilitaryAffiliate(createDischargeRequest.getMilitaryAffiliate());
            dischargeInfo.setMilitaryStatus(createDischargeRequest.getMilitaryStatus());
        } catch (UserDoesNotHaveDischargeInfoError error){
            dischargeInfo = UserDischargeInfo.builder()
                    .user(user)
                    .dischargeDate(createDischargeRequest.getDischargeDate())
                    .enlistmentDate(createDischargeRequest.getEnlistmentDate())
                    .militaryAffiliate(createDischargeRequest.getMilitaryAffiliate())
                    .militaryStatus(createDischargeRequest.getMilitaryStatus())
                    .build();
        }
        UserDischargeInfo userDischargeInfo = saveUserDischargeInfoFunction.apply(dischargeInfo);


        return new DischargeInfoResponse(userDischargeInfo);
    }

    public DischargeInfoResponse getDischargeInfo(User user){
        UserDischargeInfo dischargeInfo = findUserDischargeInfoByUserIdFunction.apply(user.getId());

        return new DischargeInfoResponse(dischargeInfo);
    }

    public InstallmentSavingsResponse registerInstallmentSavings(User user, SaveInstallmentSavingsRequest request){
        UserInstallmentSavings userInstallmentSavings = saveUserInstallmentSavingsFunction.apply(
                UserInstallmentSavings.builder()
                .user(user)
                .name(request.getName())
                .payment(request.getPayment())
                .amount(request.getAmount())
                .interestRate(request.getInterestRate())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .paymentDay(request.getPaymentDay())
                .build()
        );

        return new InstallmentSavingsResponse(new SimpleInstallmentSavings(userInstallmentSavings));
    }

    public InstallmentSavingsListResponse getInstallmentSavingsList(User user){
        List<UserInstallmentSavings> userInstallmentSavingsList = findUserInstallmentSavingsByUserIdFunction.apply(user.getId());
        List<SimpleInstallmentSavings> simpleInstallmentSavingsList = new ArrayList<>();

        userInstallmentSavingsList.forEach(it -> simpleInstallmentSavingsList.add(new SimpleInstallmentSavings(it)));

        return new InstallmentSavingsListResponse(simpleInstallmentSavingsList);
    }

    public InstallmentSavingsResponse getInstallmentSavings(User user, long installmentSavingsId){
        UserInstallmentSavings userInstallmentSavings = findUserInstallmentSavingsByIdAndUserIdFunction.apply(
                new InstallmentSavingsIdAndUserId(installmentSavingsId, user.getId())
        );

        return new InstallmentSavingsResponse(new SimpleInstallmentSavings(userInstallmentSavings));
    }

    public InstallmentSavingsResponse updateInstallmentSavings(User user, long installmentSavingsId, SaveInstallmentSavingsRequest request){
        UserInstallmentSavings userInstallmentSavings = findUserInstallmentSavingsByIdAndUserIdFunction.apply(
                new InstallmentSavingsIdAndUserId(installmentSavingsId, user.getId())
        );
        userInstallmentSavings.setAmount(request.getAmount());
        userInstallmentSavings.setName(request.getName());
        userInstallmentSavings.setPayment(request.getPayment());
        userInstallmentSavings.setInterestRate(request.getInterestRate());
        userInstallmentSavings.setPaymentDay(request.getPaymentDay());
        userInstallmentSavings.setStartDate(request.getStartDate());
        userInstallmentSavings.setDueDate(request.getDueDate());

        UserInstallmentSavings updatedInstallmentSavings = saveUserInstallmentSavingsFunction.apply(userInstallmentSavings);

        return new InstallmentSavingsResponse(new SimpleInstallmentSavings(userInstallmentSavings));
    }

    @Transactional
    public DeleteInstallmentSavingsResponse deleteInstallmentSavings(User user, long installmentSavingsId){
        InstallmentSavingsIdAndUserId installmentSavingsIdAndUserId = new InstallmentSavingsIdAndUserId(installmentSavingsId, user.getId());
        UserInstallmentSavings userInstallmentSavings = findUserInstallmentSavingsByIdAndUserIdFunction.apply(installmentSavingsIdAndUserId);
        deleteInstallmentSavingsByIdAndUserIdFunction.accept(installmentSavingsIdAndUserId);

        return new DeleteInstallmentSavingsResponse(true);
    }
}
