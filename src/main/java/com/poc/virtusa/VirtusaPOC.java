package com.poc.virtusa;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VirtusaPOC {

	public static void main(String[] args) throws FileNotFoundException {

		Pattern p = Pattern.compile(
				"([0-9]*)([\\s+]*)([0-9]*)([\\s]*)([a-zA-Z\\s]*)([\\s+]+)([a-zA-Z+]*)([\\s]+)([a-zA-Z+]*)([\\s]+)([a-zA-Z+]*)([\\s]+)([-0-9.]*)");
		Matcher m = p.matcher("");
		Scanner sc = new Scanner(new File("src/main/resources/FILE.DAT"));
		List<CompanyData> list = new ArrayList<>();
		int count = 0;
		while (sc.hasNextLine()) {

			String s = sc.nextLine();

			if (count > 0) {
				if (m.reset(s).find()) {
					if (m.groupCount() == 13) {
						CompanyData data = new CompanyData();
						data.setCompany_code(String.valueOf(m.group(1)));
						data.setAccount(String.valueOf(m.group(3)));
						data.setAmount(String.valueOf(m.group(13)));
						data.setCity(String.valueOf(m.group(5)));
						data.setCountry(String.valueOf(m.group(7)));
						data.setCredit(String.valueOf(m.group(9)));
						data.setCurrency(String.valueOf(m.group(11)));

						list.add(data);
					}
				}
			}
			count++;
		}
		sc.close();

		// caluculating total amout in euros

		BigDecimal usd_factor = new BigDecimal("1.12");
		BigDecimal gbp_factor = new BigDecimal("1.26");
		BigDecimal chf_factor = new BigDecimal("1.04");

		BigDecimal gbp_eur_rate = gbp_factor.divide(usd_factor, 5, BigDecimal.ROUND_HALF_UP);
		BigDecimal chf_eur_rate = chf_factor.divide(usd_factor, 5, BigDecimal.ROUND_HALF_UP);

		List<BigDecimal> amountListInUsd = list.stream().map(company -> {
			BigDecimal amount = new BigDecimal(0);
			if (company.getCurrency().equalsIgnoreCase("GBP")) {
				amount = new BigDecimal(company.getAmount());
				amount = amount.multiply(gbp_eur_rate);
				amount = amount.setScale(5, BigDecimal.ROUND_HALF_UP);

			} else if (company.getCurrency().equalsIgnoreCase("CHF")) {
				amount = new BigDecimal(company.getAmount());
				amount = amount.multiply(chf_eur_rate);
				amount = amount.setScale(5, BigDecimal.ROUND_HALF_UP);
			}
			return amount;
		}).collect(Collectors.toList());

		BigDecimal totalAmoutInEuros = amountListInUsd.stream().reduce(new BigDecimal("0"),
				(amout1, amount2) -> amout1.add(amount2));

		// caluculating the avearge of the amount euros
		if (Optional.ofNullable(amountListInUsd).isPresent() && !amountListInUsd.isEmpty()) {
			BigDecimal totalSize = new BigDecimal(amountListInUsd.size());
			BigDecimal averageAmoutInEur = totalAmoutInEuros.divide(totalSize, 5, BigDecimal.ROUND_HALF_UP);
			System.out.println("The average amount in Euros (EUR) = " + averageAmoutInEur);
		}

	}
}
