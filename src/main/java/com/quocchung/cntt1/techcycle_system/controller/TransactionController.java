package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Transaction.MarkSoldRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Transation.TransactionResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.TransactionService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;
  private final ResponseUtils responseUtils;

  @PostMapping("/mark-sold")
  public ResponseEntity<APIResponse<TransactionResponse>> markAsSold(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody MarkSoldRequest request) {
    TransactionResponse result = transactionService.markAsSold(
        userPrincipal.getUserId(), request);
    return ResponseEntity.ok(responseUtils.success(result));
  }

  @GetMapping("/my")
  public ResponseEntity<APIResponse<TransactionResponse>> getMyTransactions(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<TransactionResponse> result = transactionService.getMyTransactions(
        userPrincipal.getUserId(), page, size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), result.getNumber() + 1, result.getTotalElements(), size));
  }
}
