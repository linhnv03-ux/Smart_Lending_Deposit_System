package com.bank.slds.assessment.model;

public enum DebtGroup {
    GROUP_1_CURRENT("Nợ nhóm 1 - Nợ đủ tiêu chuẩn"),
    GROUP_2_ATTENTION("Nợ nhóm 2 - Nợ cần chú ý"),
    GROUP_3_SUBSTANDARD("Nợ nhóm 3 - Nợ dưới tiêu chuẩn"),
    GROUP_4_DOUBTFUL("Nợ nhóm 4 - Nợ nghi ngờ"),
    GROUP_5_BAD_DEBT("Nợ nhóm 5 - Nợ có khả năng mất vốn (Nợ xấu)");

    private final String description;

    DebtGroup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
