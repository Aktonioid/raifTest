package com.raifTest.core.responseModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class CustomerDeletionResponse {
    private boolean userNotFound;
    private boolean customerHasAccounts;
    private boolean deletiionError;
}
