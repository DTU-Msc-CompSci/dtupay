package org.acme.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportUserResponse {
    private Set<TransactionUserView> reports;

}
