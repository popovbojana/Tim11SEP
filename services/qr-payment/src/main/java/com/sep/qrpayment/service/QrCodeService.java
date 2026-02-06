package com.sep.qrpayment.service;

import com.sep.qrpayment.dto.GenerateQrRequest;
import com.sep.qrpayment.dto.GenerateQrResponse;
import com.sep.qrpayment.dto.ValidateQrRequest;
import com.sep.qrpayment.dto.ValidateQrResponse;

public interface QrCodeService {

    GenerateQrResponse generate(GenerateQrRequest request);
    ValidateQrResponse validate(ValidateQrRequest request);

}
