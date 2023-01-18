package org.acme.repositories;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.*;
import org.acme.events.TransactionCreated;
import org.acme.events.TransactionCustomerBankIDAdded;
import org.acme.events.TransactionMerchantBankIDAdded;

public class ReadModelRepository {

	private Map<String, Set<TransactionUserView>> merchantPayments = new HashMap<>();
	//private Map<String, Set<TransactionUserView>> customerPayments = new HashMap<>();
	private Map<String, TransactionManagerView> allPayments = new HashMap<>();


	public ReadModelRepository(MessageQueue eventQueue) {
		eventQueue.addHandler("TransactionCreated", this::handleTransactionCreated);
		eventQueue.addHandler("TransactionCustomerBankIDAdded",this::handleTransactionCustomerBankIDAdded);
		eventQueue.addHandler("TransactionMerchantBankIDAdded", this::handleTransactionMerchantBankIDAdded);
	}

//	public Set<TransactionUserView> getCustomerPayments(String customerId) {
//		return customerPayments.getOrDefault(customerId, new HashSet<TransactionUserView>());
//	}
	//commands
	public Set<TransactionUserView> getMerchantPayments(String merchantID) {
		return merchantPayments.getOrDefault(merchantID, new HashSet<TransactionUserView>());
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

		var transactionUserView = new TransactionUserView(customerToken,merchantId,amount,transactionId);
		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());
		transactionManagerView.setTransactionId(transactionId);
		transactionManagerView.setAmount(amount);
		transactionManagerView.setCustomerToken(customerToken);
		transactionManagerView.setMerchantId(merchantId);
		var paymentsByMerchant = merchantPayments.getOrDefault(merchantId, new HashSet<TransactionUserView>());
		paymentsByMerchant.add(transactionUserView);
		merchantPayments.put(merchantId, paymentsByMerchant);
		allPayments.put(transactionId,transactionManagerView);


	}

	public void handleTransactionMerchantBankIDAdded (Event event) {
		var transactionId = event.getArgument(0, String.class);
		var merchantBankId = event.getArgument(1, String.class);
		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());

		transactionManagerView.setMerchantBankId(merchantBankId);
		allPayments.put(transactionId,transactionManagerView);


	}
	public void handleTransactionCustomerBankIDAdded (Event event) {
		var transactionId = event.getArgument(0, String.class);
		var customerBankId = event.getArgument(1, String.class);
		var transactionManagerView = allPayments.getOrDefault(transactionId,new TransactionManagerView());

		transactionManagerView.setCustomerBankId(customerBankId);
		allPayments.put(transactionId,transactionManagerView);


	}



}
