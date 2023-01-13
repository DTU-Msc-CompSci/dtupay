package org.acme;

import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement // Needed for XML serialization and deserialization
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // TODO: Create a unique transaction ID
    private String cid;
    private String mid;
    private int amount;
}
