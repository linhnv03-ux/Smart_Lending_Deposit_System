package com.bank.slds.loan.model;

public enum LoanStatus {
    SUBMITTED,      // Hồ sơ đã khởi tạo
    ASSESSING,      // Đang thẩm định (ActiveMQ Event Processing)
    APPROVED,       // Đã phê duyệt khoản vay
    REJECTED,       // Từ chối phê duyệt
    DISBURSED,      // Đã giải ngân thành công
    REPAYING,       // Đang trong kỳ trả nợ
    CLOSED,         // Đã tất toán khoản vay
    DEFAULTED       // Nợ xấu / Quá hạn
}
