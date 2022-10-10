package com.inventi.bankstatementsapi.entity;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_statement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "operation_date", nullable = false)
    private LocalDateTime operationDate;

    @Column(name = "beneficiary", nullable = false, length = 50)
    private String beneficiary;

    @Column(name = "comment")
    private String comment;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    public boolean isValid() {
        return StringUtils.isNoneBlank(accountNumber, beneficiary, currency) &&
                amount != null && operationDate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BankStatement that = (BankStatement) o;

        return new EqualsBuilder().append(id, that.id).append(accountNumber, that.accountNumber).append(operationDate, that.operationDate).append(beneficiary, that.beneficiary).append(comment, that.comment).append(amount, that.amount).append(currency, that.currency).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(accountNumber).append(operationDate).append(beneficiary).append(comment).append(amount).append(currency).toHashCode();
    }
}