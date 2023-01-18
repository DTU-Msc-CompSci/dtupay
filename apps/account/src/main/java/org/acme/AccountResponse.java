package org.acme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse implements Serializable {

    private static final long serialVersionUID = 4233248231244976610L;
    private DTUPayUser user;
    private String message;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountResponse)) {
            return false;
        }
        var c = (AccountResponse) o;
        return user != null && user.equals(c.getUser()) &&
                message != null && message.equals(c.getMessage());
    }
}