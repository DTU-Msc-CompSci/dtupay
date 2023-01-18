package org.acme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTUPayUser implements Serializable {
    // Might need to change the number depending on the User being referenced
    private static final long serialVersionUID = 9023222981284806610L;

    Person person;

    BankId bankId;

    String uniqueId;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DTUPayUser)) {
            return false;
        }
        var c = (DTUPayUser) o;
        return person.getFirstName() != null && person.getFirstName().equals(c.getPerson().getFirstName()) &&
                person.getLastName() != null && person.getLastName().equals(c.getPerson().getLastName()) &&
                person.getCprNumber() != null && person.getCprNumber().equals(c.getPerson().getCprNumber()) &&
                bankId != null && bankId.equals(c.getBankId()) &&
                ( (uniqueId == null && c.getUniqueId() == null) || (uniqueId != null && uniqueId.equals(c.getUniqueId()) ));
    }

    @Override
    public int hashCode() {
        return  bankId.getBankAccountId()== null ? 0 : bankId.getBankAccountId().hashCode();
    }

    @Override
    public String toString() {
        // uniqueId could potentially be null
        return String.format("DTU Pay User id: %s", uniqueId);
    }
}
