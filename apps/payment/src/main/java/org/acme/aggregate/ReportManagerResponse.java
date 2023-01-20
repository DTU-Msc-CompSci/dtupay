package org.acme.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportManagerResponse {
    // Might need to change the number depending on the User being referenced
    private Set<TransactionManagerView> reports;

}
