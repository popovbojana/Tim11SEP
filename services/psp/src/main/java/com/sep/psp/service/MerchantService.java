package com.sep.psp.service;

import com.sep.psp.dto.merchant.MerchantCreateRequest;
import com.sep.psp.dto.merchant.MerchantResponse;
import com.sep.psp.dto.merchant.MerchantUpdateRequest;
import com.sep.psp.entity.Merchant;

import java.util.Set;

public interface MerchantService {

    MerchantResponse create(MerchantCreateRequest request);
    Merchant getByMerchantKey(String merchantKey);
    MerchantResponse update(String merchantKey, MerchantUpdateRequest request);
    MerchantResponse getMerchant(String merchantKey);
    Set<MerchantResponse> getAll();
    void remove(Long id);
    Set<String> getActiveMethods(String merchantKey);

}
