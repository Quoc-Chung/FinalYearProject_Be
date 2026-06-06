package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Transaction.MarkSoldRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Transation.TransactionResponse;
import org.springframework.data.domain.Page;

public interface TransactionService {
  TransactionResponse markAsSold(Long sellerId, MarkSoldRequest request);

  Page<TransactionResponse> getMyTransactions(Long userId, int page, int size);

  void cancelTransaction(Long transactionId);
}
