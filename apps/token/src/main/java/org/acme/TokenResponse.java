package org.acme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {

    private Set<Token> tokens;
    private String message;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TokenResponse)) {
            return false;
        }
        var c = (TokenResponse) o;
        return ((tokens == null && c.getTokens() == null) || tokens.equals(c.getTokens())) &&
                ((message == null && c.getMessage() == null) || message.equals(c.getMessage()));
    }

}
