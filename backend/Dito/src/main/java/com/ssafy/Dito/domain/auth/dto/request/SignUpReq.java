package com.ssafy.Dito.domain.auth.dto.request;

import com.ssafy.Dito.domain.auth.constraint.nickname.ValidNickname;
import com.ssafy.Dito.domain.auth.constraint.password.ValidPassword;
import com.ssafy.Dito.domain.auth.constraint.personalId.ValidPersonalId;
import com.ssafy.Dito.domain.user.entity.Frequency;
import com.ssafy.Dito.domain.user.entity.Gender;
import com.ssafy.Dito.domain.user.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignUpReq(
        @ValidPersonalId
        String personalId,
        @ValidPassword
        String password,
        @ValidNickname
        String nickname,
        LocalDate birth,
        Gender gender,
        Job job,
        Frequency frequency
) {

}
