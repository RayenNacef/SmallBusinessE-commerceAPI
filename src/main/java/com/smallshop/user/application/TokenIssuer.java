package com.smallshop.user.application;

import com.smallshop.user.domain.User;

public interface TokenIssuer {

    AccessToken issueFor(User user);
}
