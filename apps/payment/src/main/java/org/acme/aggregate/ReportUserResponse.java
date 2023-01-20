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
    // Might need to change the number depending on the User being referenced
    private Set<TransactionUserView> reports;

}
