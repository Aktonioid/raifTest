package com.raifTest.core.responseModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class CustomerCreationResponse {
    private boolean usernameExist;
    private boolean creationError;
}
