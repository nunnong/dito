package com.ssafy.Dito.domain.auth.dto.request;

import com.ssafy.Dito.domain.user.entity.Frequency;
import com.ssafy.Dito.domain.user.entity.Gender;
import com.ssafy.Dito.domain.user.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignUpReq(
        String personalId,
        String password,
        @NotBlank @Size(max = 15)
        String nickname,
        LocalDate birth,
        Gender gender,
        Job job,
        Frequency frequency
) {

}
