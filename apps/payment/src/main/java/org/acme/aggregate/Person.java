package org.acme.aggregate;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    String firstName;

    String lastName;

    String cprNumber;
}
