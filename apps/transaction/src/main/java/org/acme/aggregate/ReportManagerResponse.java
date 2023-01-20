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
    private Set<TransactionManagerView> reports;

}
