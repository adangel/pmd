/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices.unusedprivatemethod;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class User {
    private Optional<User> userOpt;

    public User methodA(String email, String firstName, String lastName) {
        User user = userOpt.orElseGet(() -> {
            try {
                return registerUser(email, firstName, lastName);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register user for " + email, e);
            }
        });
        // ...
        return user;
    }

    private User registerUser(String email, String firstName, String lastName) throws Exception {
        // register user logic here...
        return null;
    }

    public void methodB() {
        User someVariable = new User();
        Optional.ofNullable(someVariable)
                .map(this::somePrivateFunction)  // this private function
                .map(Stream::of)
                .get()
                .collect(Collectors.toList());
    }

    private String somePrivateFunction(User u) {
        return u.toString();
    }

    private String reallyUnusedMethod() {
        return "unused";
    }
}
