/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.perm;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.AddAccountsToWhitelistResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.io.IOException;
import java.util.List;

public class PermAddAccountsToWhitelistTransaction implements Transaction<Boolean> {

  private final List<String> accounts;

  public PermAddAccountsToWhitelistTransaction(final List<String> accounts) {
    this.accounts = accounts;
  }

  @Override
  public Boolean execute(final PantheonWeb3j node) {
    try {
      AddAccountsToWhitelistResponse response = node.addAccountsToWhitelist(accounts).send();
      assertThat(response.getResult()).isTrue();
      return response.getResult();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
