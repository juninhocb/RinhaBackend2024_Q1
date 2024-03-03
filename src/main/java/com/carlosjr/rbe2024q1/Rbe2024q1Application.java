package com.carlosjr.rbe2024q1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class Rbe2024q1Application {

	public static void main(String[] args) {
		SpringApplication.run(Rbe2024q1Application.class, args);
	}

}

@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes/{id}")
class CrebitoController{

	private final CrebitoService crebitoService;
	@PostMapping("/transacoes")
	ResponseEntity<CustomerBalance> doTransaction(@PathVariable Integer id,
												  @RequestBody CustomerTransaction transaction){
		return ResponseEntity
				.ok()
				.body(crebitoService.doTransaction(id, transaction));
	}
	@GetMapping("/extrato")
	ResponseEntity<BankStatement> getBankStatement(@PathVariable Integer id){
		return ResponseEntity
				.ok()
				.body(crebitoService.getBankStatement(id));
	}
}

@ControllerAdvice
class CrebitoControllerExceptionHandler{
	static int times = 0;
	@ExceptionHandler(CustomerNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	void handleNotFound(){/* ignore*/}

	@ExceptionHandler(InsufficientResourceException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	void handleInsufficientResource(){/* ignore*/}

	@ExceptionHandler(UnexpectedServerBehaviour.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	int handleUnexpectedServerBehaviour(){
		times += times;
		return times; }
}

class CustomerNotFoundException extends RuntimeException{
}

class InsufficientResourceException extends RuntimeException{
}

class UnexpectedServerBehaviour extends RuntimeException{
}

@Service
@RequiredArgsConstructor
class CrebitoService {
	private final CrebitoRepository crebitoRepository;

	CustomerBalance doTransaction(Integer id, CustomerTransaction transaction){

		findCustomerByIdCompareLimit(id, transaction.value());
		var balance =  crebitoRepository
				.getAmountByValue(id, transaction.value());
		if (balance.isEmpty()){
			throw new InsufficientResourceException();
		}
		var newBalance = CustomerBalance
				.builder()
				.customerId(id)
				.limit(balance.get().limit())
				.value(balance.get().value() + transaction.value())
				.build();

		var rowsAffected = crebitoRepository.saveNewTransaction(newBalance.value(), transaction);

		if ( rowsAffected < 2){
			throw new UnexpectedServerBehaviour();
		}

		return newBalance;
	}

	BankStatement getBankStatement(Integer id){
		findCustomerByIdCompareLimit(id, 0);
		return crebitoRepository.getBankStatement(id);
	}

	void findCustomerByIdCompareLimit(Integer id, Integer value){
		if ( id < 0 || id > 5 ){
			throw new CustomerNotFoundException();
		}
		var c =crebitoRepository.getCustomerById(id);
		if ( value > c.limit()){
			throw new InsufficientResourceException();
		}
	}
}

@Repository
@RequiredArgsConstructor
class CrebitoRepository{

	final Set<Customer> mockedCustomers = Set.of(
			Customer.builder().id(1).limit(100000).build(),
			Customer.builder().id(2).limit(80000).build(),
			Customer.builder().id(3).limit(1000000).build(),
			Customer.builder().id(4).limit(10000000).build(),
			Customer.builder().id(5).limit(500000).build());

	final String GET_SALDO_IF_CUSTOMER_HAS_LIMIT = "SELECT cliente_id, limite, valor FROM `saldos` " +
			"WHERE cliente_id = ? AND ? + valor < limite";

	final String UPDATE_SALDO_ONLY_VALOR = "UPDATE `saldos` SET valor = ? WHERE cliente_id = ?";

	final String SAVE_NEW_TRANSACAO = "INSERT INTO `transacoes` (cliente_id, valor, tipo, descricao) " +
			"VALUES ( ? , ? , ? , ? )";

	final String GET_ALL_CUSTOMER_TRANSACTIONS_BY_ID = "SELECT cliente_id, valor, tipo, descricao " +
			"FROM `transacoes` WHERE cliente_id = ? ";

	final JdbcTemplate jdbcTemplate;

	final CustomerBalanceRowMapper customerBalanceRowMapper;
	final CustomerTransactionRowMapper customerTransactionRowMapper;

	Optional<CustomerBalance> getAmountByValue(Integer id, Integer value){
		return Optional
				.ofNullable(jdbcTemplate
						.queryForObject(GET_SALDO_IF_CUSTOMER_HAS_LIMIT, customerBalanceRowMapper, id, value));
	}

	int saveNewTransaction(Integer newValue, CustomerTransaction transaction){

		var rowsBalance = jdbcTemplate.update(UPDATE_SALDO_ONLY_VALOR, newValue, transaction.customerId());

		var rowsTransaction = jdbcTemplate.update(SAVE_NEW_TRANSACAO, transaction.customerId(), transaction.value()
			,transaction.type() ,transaction.description());

		return rowsTransaction + rowsBalance;

	}

	BankStatement getBankStatement(Integer id){
		var customerBalance = getAmountByValue(id, 0);

		if ( customerBalance.isEmpty()){
			throw new UnexpectedServerBehaviour();
		}

		var transactions = jdbcTemplate.query(GET_ALL_CUSTOMER_TRANSACTIONS_BY_ID, customerTransactionRowMapper, id);

		if (transactions.isEmpty()){
			throw new UnexpectedServerBehaviour();
		}

		return BankStatement.builder()
				.customerBalance(customerBalance.get())
				.transactions(new HashSet<>(transactions))
				.build();

	}

	Customer getCustomerById(Integer id){
		return mockedCustomers.stream().filter(c -> Objects.equals(c.id(), id)).findAny().get();
	}

}

@Component
class CustomerTransactionRowMapper implements RowMapper<CustomerTransaction>{

	@Override
	public CustomerTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
		return CustomerTransaction.builder()
				.customerId(rs.getInt("cliente_id"))
				.value(rs.getInt("valor"))
				.type(TransactionType.toChar(rs.getString("tipo")))
				.description(rs.getString("descricao"))
				.timestamp(rs.getTimestamp("realizada_em"))
				.build();
	}
}

@Component
class CustomerBalanceRowMapper implements RowMapper<CustomerBalance>{

	@Override
	public CustomerBalance mapRow(ResultSet rs, int rowNum) throws SQLException {
		return CustomerBalance.builder()
				.customerId(rs.getInt("cliente_id"))
				.limit(rs.getInt("limite"))
				.value(rs.getInt("valor"))
				.timestamp(LocalDateTime.now().withNano(0))
				.build();
	}
}

@Builder
record CustomerBalance(@JsonIgnore Integer id,
					   @JsonIgnore Integer customerId,
					   @JsonProperty("limite") Integer limit,
					   @JsonProperty("total") Integer value,
					   @JsonProperty("data_extrato") @Null LocalDateTime timestamp){}
@Builder
record CustomerTransaction(@Null @JsonIgnore Integer id,
						   @JsonIgnore @NotNull Integer customerId,
						   @JsonProperty("valor") @NotNull @Positive Integer value,
						   @JsonProperty("tipo") @NotNull @Pattern(regexp = "[CD]") TransactionType type,
						   @JsonProperty("descricao") @NotBlank @Size(min = 1, max = 10) String description,
						   @JsonProperty("realizada_em") @Null Timestamp timestamp){}

@Builder
record BankStatement(@JsonProperty("saldo") CustomerBalance customerBalance,
					 @JsonProperty("ultimas_transacoes") Set<CustomerTransaction> transactions){}

@Builder
record Customer(Integer id, Integer limit){}
enum TransactionType{
	C, D;

	static TransactionType toChar(String str){
		switch (str){
            case "C" -> {
                return TransactionType.C;
            }
            case "D" -> {
                return TransactionType.D;
            }
		}
		throw new IllegalArgumentException();
	}
}

