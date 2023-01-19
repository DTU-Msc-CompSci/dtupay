package org.acme.repositories;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.*;
import org.acme.events.TransactionCreated;

public class ReadModelRepository {

	private Map<String, TransactionManagerView> allPayments = new HashMap<>();


	public ReadModelRepository(MessageQueue eventQueue) {
		eventQueue.addHandler("TransactionCreated", this::handleTransactionCreated);
		eventQueue.addHandler("TransactionCustomerInfoAdded",this::handleTransactionCustomerInfoAdded);
		eventQueue.addHandler("TransactionMerchantInfoAdded", this::handleTransactionMerchantInfoAdded);
	}

	//commands
	public Set<TransactionUserView> getMerchantPayment(String merchantID) {
		 return ( allPayments.values().stream()
				.filter((transactionManagerView)-> {
					return transactionManagerView.getMerchantId().equals(merchantID);
				}))
				.map(transactionManagerView -> {
					return transactionManagerView.toUserView();
				}).collect(Collectors.toSet());
	}
	public Set<TransactionUserView> getCustomerPayment(String customerID) {
		return ( allPayments.values().stream()
				.filter((transactionManagerView)-> {
					return transactionManagerView.getCustomerId().equals(customerID);
				}))
				.map(transactionManagerView -> {
					return transactionManagerView.toUserView();
				}).collect(Collectors.toSet());
	}
	public Set<TransactionManagerView> getAllPayments() {

		return new HashSet<TransactionManagerView>( allPayments.values());
	}



	//Event handlers
	public void handleTransactionCreated (Event event) {
		var transactionId = event.getArgument(0, String.class);
		var customerToken = event.getArgument(1, String.class);
		var merchantId = event.getArgument(2, String.class);
		var amount = event.getArgument(3, BigDecimal.class);

		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());
		transactionManagerView.setTransactionId(transactionId);
		transactionManagerView.setAmount(amount);
		transactionManagerView.setCustomerToken(customerToken);
		transactionManagerView.setMerchantId(merchantId);
		allPayments.put(transactionId,transactionManagerView);


	}

	public void handleTransactionMerchantInfoAdded (Event event) {
		var transactionId = event.getArgument(0, String.class);
		var merchantInfo = event.getArgument(1, DTUPayUser.class);
		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());
		transactionManagerView.setMerchant(merchantInfo);
		allPayments.put(transactionId,transactionManagerView);
	}
	public void handleTransactionCustomerInfoAdded (Event event) {
		var transactionId = event.getArgument(0, String.class);
		var customerInfo = event.getArgument(1, DTUPayUser.class);
		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());
		transactionManagerView.setCustomer(customerInfo);
		transactionManagerView.setCustomerId(customerInfo.getUniqueId());
		allPayments.put(transactionId,transactionManagerView);
	}



}
