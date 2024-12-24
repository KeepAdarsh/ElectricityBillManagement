package com.jts.bill.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jts.bill.entity.BillDetails;
import com.jts.bill.entity.GenerateBillRequest;
import com.jts.bill.repository.BillDetailsRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BillDetailsService {

	private final BillDetailsRepository billDetailsRepository;
	private static final Logger logger = LoggerFactory.getLogger(BillDetailsService.class);

	// Constants for rates and penalties
	private static final double RATE_BELOW_100 = 7.0;
	private static final double RATE_100_TO_500 = 9.0;
	private static final double RATE_ABOVE_500 = 12.0;
	private static final double LATE_PAYMENT_PENALTY = 10.0;

	/**
	 * Fetches unpaid bill details for the given service request number.
	 *
	 * @param serviceRequestNo - The service request number
	 * @return List of unpaid bill details
	 */
	public List<BillDetails> getBillDetails(String serviceRequestNo) {
		logger.info("Fetching bill details for service request: {}", serviceRequestNo);
		return billDetailsRepository.findBilDetails(serviceRequestNo, "No");
	}

	/**
	 * Processes payment for the bill with the given ID and updates the payment status.
	 *
	 * @param id     - The bill ID
	 * @param amount - The payment amount
	 * @return Payment success message
	 */
	@Transactional
	public String doPayment(Long id, String amount) {
		logger.info("Processing payment for bill ID: {}", id);

		BillDetails billDetails = billDetailsRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Bill Details does not exist for ID: " + id));

		billDetails.setBillPayment(amount);
		billDetails.setPaymentDate(LocalDate.now());
		billDetails.setPaymentDone("Yes");

		billDetailsRepository.save(billDetails);
		logger.info("Payment successfully processed for bill ID: {}", id);
		return "Bill payment is done successfully";
	}

	/**
	 * Generates bills for the given service request and total unit consumption.
	 *
	 * @param request - The bill generation request
	 * @return Bill generation success message
	 */
	@Transactional
	public String generateBill(GenerateBillRequest request) {
		int totalUnit = request.getTotalUnit();
		double totalBill = 0;

		// Calculate the bill based on total units
		if (totalUnit < 100) {
			totalBill = totalUnit * 7;
		} else if (totalUnit >= 100 && totalUnit < 500) {
			totalBill = totalUnit * 9;
		} else if (totalUnit >= 500) {
			totalBill = totalUnit * 12;
		}

		// Generate a single bill entry
		Random random = new Random();
		BillDetails billDetail = new BillDetails();
		billDetail.setBillAmount(totalBill);
		billDetail.setBillAmountAfterDueDate(totalBill + 10);
		billDetail.setBillCreationDate(LocalDate.now());
		billDetail.setBillDueDate(LocalDate.now().plusMonths(1));
		billDetail.setBillNo(String.format("B_%03d", random.nextInt(1000))); // Generate bill number in desired format
		billDetail.setServiceRequestNo(request.getServiceRequestNo());
		billDetail.setPaymentDone("No");

		// Save the single bill entry
		billDetailsRepository.save(billDetail);

		return "Congratulations! Your Bill is Generated Successfully.";
	}
}