package com.carlosjr.rbe2024q1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Rbe2024q1ApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;
	List<CustomerTransaction> transactions = getTransactions();
	final String BASE_URL = "/clientes/";
	@BeforeEach
	void setUp(){
		if ( restTemplate == null){
				fail("Bean not injected..");
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateTransactionAndHandleInvalidStuff(){
		int id = new Random().nextInt(1,6);
		String customerUrlSaveTransaction = BASE_URL + id + "/transacoes";

		ResponseEntity<CustomerBalanceRetriever> pr1=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.getFirst()), CustomerBalanceRetriever.class);

		assertThat(pr1.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(pr1.getBody());

		ResponseEntity<CustomerBalanceRetriever> pr2=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(1)), CustomerBalanceRetriever.class);

		assertThat(pr2.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(pr2.getBody());

		ResponseEntity<CustomerBalanceRetriever> pr3=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(2)), CustomerBalanceRetriever.class);

		assertThat(pr3.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(pr3.getBody());

		ResponseEntity<CustomerBalanceRetriever> pr4=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(3)), CustomerBalanceRetriever.class);

		assertThat(pr4.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(pr4.getBody());

		ResponseEntity<CustomerBalanceRetriever> pr5=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(4)), CustomerBalanceRetriever.class);

		assertThat(pr5.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<CustomerBalanceRetriever> pr6=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(5)), CustomerBalanceRetriever.class);

		assertThat(pr6.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		ResponseEntity<CustomerBalanceRetriever> pr7=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(6)), CustomerBalanceRetriever.class);

		assertThat(pr7.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<CustomerBalanceRetriever> pr8=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(7)), CustomerBalanceRetriever.class);

		assertThat(pr8.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<CustomerBalanceRetriever> pr9=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(8)), CustomerBalanceRetriever.class);

		assertThat(pr9.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<CustomerBalanceRetriever> pr10=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(9)), CustomerBalanceRetriever.class);

		assertThat(pr10.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<CustomerBalanceRetriever> pr11=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(10)), CustomerBalanceRetriever.class);

		assertThat(pr11.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(pr11.getBody());

		ResponseEntity<CustomerBalanceRetriever> pr12=  restTemplate
				.postForEntity(customerUrlSaveTransaction, new HttpEntity<>(transactions.get(11)), CustomerBalanceRetriever.class);

		assertThat(pr12.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	List<CustomerTransaction> getTransactions(){
		CustomerTransaction ct1 = CustomerTransaction.builder()
				.value(200)
				.type(TransactionType.c)
				.description("C lang ")
				.build();

		CustomerTransaction ct2 = CustomerTransaction.builder()
				.value(405)
				.type(TransactionType.d)
				.description("C++ Tips ")
				.build();

		CustomerTransaction ct3 = CustomerTransaction.builder()
				.value(1200)
				.type(TransactionType.d)
				.description("Whatever ")
				.build();

		CustomerTransaction ct4BadIdButSuccessByJsonIgnore = CustomerTransaction.builder()
				.id(423)
				.value(405)
				.type(TransactionType.d)
				.description("C++ Tips ")
				.build();
		CustomerTransaction ct5BadDescriptionSize = CustomerTransaction.builder()
				.value(405)
				.type(TransactionType.d)
				.description("C++ Tips ASDFHUWEFSAFUSDFU")
				.build();
		CustomerTransaction ct6NotHasThisAwesomeAmount = CustomerTransaction.builder()
				.value(405000000)
				.type(TransactionType.d)
				.description("C++ Tips ")
				.build();
		CustomerTransaction ct7NegativeValue = CustomerTransaction.builder()
				.value(-405)
				.type(TransactionType.d)
				.description("C++ Tips ")
				.build();
		CustomerTransaction ct8MissingValue = CustomerTransaction.builder()
				.type(TransactionType.d)
				.description("C++ Tips ")
				.build();
		CustomerTransaction ct9MissingType = CustomerTransaction.builder()
				.value(405)
				.description("C++ Tips ")
				.build();
		CustomerTransaction ct10MissingDescription = CustomerTransaction.builder()
				.value(405)
				.type(TransactionType.d)
				.build();
		CustomerTransaction ct11BadCustomerIdButWillPass = CustomerTransaction.builder()
				.value(1200)
				.type(TransactionType.d)
				.description("Whatever ")
				.customerId(1)
				.build();
		CustomerTransaction ct12BadTimestamp = CustomerTransaction.builder()
				.value(1200)
				.type(TransactionType.d)
				.description("Whatever ")
				.timestamp(Timestamp.from(Instant.now()))
				.build();
		return List.of(ct1,ct2,ct3,ct4BadIdButSuccessByJsonIgnore,ct5BadDescriptionSize, ct6NotHasThisAwesomeAmount,
				ct7NegativeValue,ct8MissingValue,ct9MissingType,ct10MissingDescription,
				ct11BadCustomerIdButWillPass, ct12BadTimestamp);


	}

}
