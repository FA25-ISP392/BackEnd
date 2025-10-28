package com.isp392.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityLevel {
    SEDENTARY(1.2),           // Ít vận động (làm việc văn phòng)
    LIGHTLY_ACTIVE(1.375),    // Vận động nhẹ (tập 1-3 ngày/tuần)
    MODERATELY_ACTIVE(1.55),  // Vận động vừa (tập 3-5 ngày/tuần)
    VERY_ACTIVE(1.725),       // Vận động nhiều (tập 6-7 ngày/tuần)
    EXTRA_ACTIVE(1.9);        // Vận động rất nhiều (vận động viên, lao động nặng)

    private final double multiplier;
}